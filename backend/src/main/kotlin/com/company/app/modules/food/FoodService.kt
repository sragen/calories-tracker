package com.company.app.modules.food

import com.company.app.common.exception.AppException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime

@Service
class FoodService(
    private val foodItemRepository: FoodItemRepository,
    private val foodCategoryRepository: FoodCategoryRepository,
    private val openFoodFactsClient: OpenFoodFactsClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun search(q: String?, categoryId: Long?, pageable: Pageable): Page<FoodItemResponse> =
        foodItemRepository.search(q?.takeIf { it.isNotBlank() }, categoryId, pageable)
            .map { it.toResponse() }

    fun findById(id: Long): FoodItemResponse =
        (foodItemRepository.findActiveById(id) ?: throw AppException.notFound("Food not found")).toResponse()

    fun findByBarcode(barcode: String): FoodItemResponse {
        // 1. Check local DB first
        foodItemRepository.findByBarcodeAndDeletedAtIsNull(barcode)?.let { return it.toResponse() }

        // 2. Fallback to Open Food Facts
        val off = openFoodFactsClient.findByBarcode(barcode)
            ?: throw AppException.notFound("Barcode not found in local DB or Open Food Facts")

        // 3. Auto-cache to local DB for future lookups
        val cached = createFromSource(
            name = off.name,
            nameEn = off.nameEn,
            caloriesPer100g = off.caloriesPer100g,
            proteinPer100g = off.proteinPer100g,
            carbsPer100g = off.carbsPer100g,
            fatPer100g = off.fatPer100g,
            fiberPer100g = off.fiberPer100g,
            barcode = off.barcode,
            source = "OPEN_FOOD_FACTS",
            isVerified = false
        )
        log.info("Cached barcode={} from OpenFoodFacts as food.id={}", barcode, cached.id)
        return cached.toResponse()
    }

    fun getCategories(): List<FoodCategoryResponse> =
        foodCategoryRepository.findAllByOrderBySortOrderAsc().map { it.toResponse() }

    fun create(req: FoodItemRequest, userId: Long, isAdmin: Boolean = false): FoodItemResponse {
        val category = req.categoryId?.let {
            foodCategoryRepository.findById(it).orElseThrow { AppException.notFound("Category not found") }
        }
        val item = FoodItem(
            name = req.name,
            nameEn = req.nameEn,
            category = category,
            caloriesPer100g = req.caloriesPer100g,
            proteinPer100g = req.proteinPer100g,
            carbsPer100g = req.carbsPer100g,
            fatPer100g = req.fatPer100g,
            fiberPer100g = req.fiberPer100g,
            defaultServingG = req.defaultServingG,
            servingDescription = req.servingDescription,
            barcode = req.barcode,
            source = if (isAdmin) "ADMIN" else "USER_SUBMISSION",
            isVerified = isAdmin,
            createdBy = userId
        )
        return foodItemRepository.save(item).toResponse()
    }

    private fun createFromSource(
        name: String, nameEn: String? = null, categoryId: Long? = null,
        caloriesPer100g: Double, proteinPer100g: Double = 0.0,
        carbsPer100g: Double = 0.0, fatPer100g: Double = 0.0,
        fiberPer100g: Double? = null, defaultServingG: Double = 100.0,
        barcode: String? = null, source: String = "ADMIN", isVerified: Boolean = false
    ): FoodItem {
        val category = categoryId?.let { foodCategoryRepository.findById(it).orElse(null) }
        val item = FoodItem(
            name = name, nameEn = nameEn, category = category,
            caloriesPer100g = caloriesPer100g, proteinPer100g = proteinPer100g,
            carbsPer100g = carbsPer100g, fatPer100g = fatPer100g, fiberPer100g = fiberPer100g,
            defaultServingG = defaultServingG, barcode = barcode, source = source, isVerified = isVerified
        )
        return foodItemRepository.save(item)
    }

    fun update(id: Long, req: FoodItemRequest): FoodItemResponse {
        val item = foodItemRepository.findActiveById(id) ?: throw AppException.notFound("Food not found")
        val category = req.categoryId?.let {
            foodCategoryRepository.findById(it).orElseThrow { AppException.notFound("Category not found") }
        }
        item.name = req.name
        item.nameEn = req.nameEn
        item.category = category
        item.caloriesPer100g = req.caloriesPer100g
        item.proteinPer100g = req.proteinPer100g
        item.carbsPer100g = req.carbsPer100g
        item.fatPer100g = req.fatPer100g
        item.fiberPer100g = req.fiberPer100g
        item.defaultServingG = req.defaultServingG
        item.servingDescription = req.servingDescription
        item.barcode = req.barcode
        item.updatedAt = LocalDateTime.now()
        return foodItemRepository.save(item).toResponse()
    }

    fun delete(id: Long) {
        val item = foodItemRepository.findActiveById(id) ?: throw AppException.notFound("Food not found")
        item.deletedAt = LocalDateTime.now()
        foodItemRepository.save(item)
    }

    fun verify(id: Long): FoodItemResponse {
        val item = foodItemRepository.findActiveById(id) ?: throw AppException.notFound("Food not found")
        item.isVerified = true
        item.updatedAt = LocalDateTime.now()
        return foodItemRepository.save(item).toResponse()
    }

    fun reject(id: Long) {
        val item = foodItemRepository.findActiveById(id) ?: throw AppException.notFound("Food not found")
        item.deletedAt = LocalDateTime.now()
        foodItemRepository.save(item)
    }

    fun getPendingReview(pageable: Pageable): Page<FoodItemResponse> =
        foodItemRepository.findPendingReview(pageable).map { it.toResponse() }

    fun importCsv(file: MultipartFile): ImportResult {
        val reader = BufferedReader(InputStreamReader(file.inputStream))
        val lines = reader.readLines()
        if (lines.isEmpty()) return ImportResult(0, 0, listOf("File is empty"))

        var imported = 0
        var skipped = 0
        val errors = mutableListOf<String>()

        // Skip header row
        lines.drop(1).forEachIndexed { idx, line ->
            val row = idx + 2 // 1-based, accounting for header
            if (line.isBlank()) { skipped++; return@forEachIndexed }

            try {
                val cols = line.split(",").map { it.trim().removeSurrounding("\"") }
                if (cols.size < 5) {
                    errors.add("Row $row: insufficient columns (need at least 5)")
                    skipped++
                    return@forEachIndexed
                }

                val name = cols[0].takeIf { it.isNotBlank() } ?: run {
                    errors.add("Row $row: name is required")
                    skipped++
                    return@forEachIndexed
                }
                val calories = cols.getOrNull(3)?.toDoubleOrNull() ?: run {
                    errors.add("Row $row: invalid calories")
                    skipped++
                    return@forEachIndexed
                }

                // Check for duplicate barcode
                val barcode = cols.getOrNull(9)?.takeIf { it.isNotBlank() }
                if (barcode != null && foodItemRepository.findByBarcodeAndDeletedAtIsNull(barcode) != null) {
                    skipped++
                    return@forEachIndexed
                }

                createFromSource(
                    name = name,
                    nameEn = cols.getOrNull(1)?.takeIf { it.isNotBlank() },
                    categoryId = cols.getOrNull(2)?.toLongOrNull(),
                    caloriesPer100g = calories,
                    proteinPer100g = cols.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                    carbsPer100g = cols.getOrNull(5)?.toDoubleOrNull() ?: 0.0,
                    fatPer100g = cols.getOrNull(6)?.toDoubleOrNull() ?: 0.0,
                    fiberPer100g = cols.getOrNull(7)?.toDoubleOrNull(),
                    defaultServingG = cols.getOrNull(8)?.toDoubleOrNull() ?: 100.0,
                    barcode = barcode,
                    source = "TKPI",
                    isVerified = true
                )
                imported++
            } catch (e: Exception) {
                errors.add("Row $row: ${e.message}")
                skipped++
            }
        }

        return ImportResult(imported, skipped, errors)
    }

    data class ImportResult(
        val imported: Int,
        val skipped: Int,
        val errors: List<String>
    )
}
