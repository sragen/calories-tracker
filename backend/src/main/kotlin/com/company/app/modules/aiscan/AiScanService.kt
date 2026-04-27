package com.company.app.modules.aiscan

import com.company.app.common.exception.AppException
import com.company.app.modules.food.FoodItem
import com.company.app.modules.food.FoodItemRepository
import com.company.app.modules.meal.MealLog
import com.company.app.modules.meal.MealLogRepository
import com.company.app.modules.storage.MinioService
import com.company.app.modules.subscription.SubscriptionService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime

private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/png", "image/webp")
private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L   // 5 MB
private const val FREE_DAILY_SCAN_LIMIT = 20

@Service
class AiScanService(
    private val geminiService: GeminiService,
    private val minioService: MinioService,
    private val subscriptionService: SubscriptionService,
    private val scanLogRepo: AiScanLogRepository,
    private val foodItemRepo: FoodItemRepository,
    private val mealLogRepo: MealLogRepository
) {

    fun analyze(userId: Long, imageFile: MultipartFile): AiScanResponse {
        if (!subscriptionService.isPremium(userId)) {
            throw AppException.forbidden("Fitur AI Scan memerlukan langganan Premium")
        }

        // File validation
        val contentType = imageFile.contentType ?: "application/octet-stream"
        if (contentType !in ALLOWED_CONTENT_TYPES) {
            throw AppException.badRequest("Hanya JPG, PNG, atau WebP yang diizinkan")
        }
        if (imageFile.size > MAX_FILE_SIZE_BYTES) {
            throw AppException.badRequest("Ukuran file maksimal 5 MB")
        }

        // Rate limiting: max FREE_DAILY_SCAN_LIMIT scans per day
        val todayStart = LocalDateTime.now().toLocalDate().atStartOfDay()
        val scansToday = scanLogRepo.countByUserIdSince(userId, todayStart)
        if (scansToday >= FREE_DAILY_SCAN_LIMIT) {
            throw AppException.badRequest("Batas $FREE_DAILY_SCAN_LIMIT scan per hari telah tercapai")
        }

        // Upload to MinIO for storage (non-blocking failure)
        val imageUrl = runCatching { minioService.uploadImage(imageFile, "ai-scans/$userId") }.getOrNull()
        val imageBytes = minioService.readBytes(imageFile)

        val detected = geminiService.analyzeImage(imageBytes, imageFile.contentType ?: "image/jpeg")

        val log = scanLogRepo.save(AiScanLog(
            userId = userId,
            imageUrl = imageUrl,
            detectedCount = detected.size
        ))

        val detectedFoods = detected.map { raw ->
            val matched = findFoodByName(raw.name)
            AiDetectedFood(
                name = raw.name,
                portionG = raw.portionG,
                caloriesPer100g = if (matched != null) matched.caloriesPer100g else raw.caloriesPer100g,
                proteinPer100g = if (matched != null) matched.proteinPer100g else raw.proteinPer100g,
                carbsPer100g = if (matched != null) matched.carbsPer100g else raw.carbsPer100g,
                fatPer100g = if (matched != null) matched.fatPer100g else raw.fatPer100g,
                matchedFoodId = matched?.id
            )
        }

        return AiScanResponse(
            scanLogId = log.id,
            imageUrl = imageUrl,
            detectedFoods = detectedFoods
        )
    }

    @Transactional
    fun confirm(userId: Long, request: AiScanConfirmRequest): Int {
        if (!subscriptionService.isPremium(userId)) {
            throw AppException.forbidden("Fitur AI Scan memerlukan langganan Premium")
        }

        val date = LocalDate.parse(request.loggedAt)
        var count = 0

        for (item in request.selectedFoods) {
            val food = if (item.matchedFoodId != null) {
                foodItemRepo.findById(item.matchedFoodId).orElse(null)
            } else null

            val foodItem = food ?: foodItemRepo.save(FoodItem(
                name = item.name,
                caloriesPer100g = item.caloriesPer100g,
                proteinPer100g = item.proteinPer100g,
                carbsPer100g = item.carbsPer100g,
                fatPer100g = item.fatPer100g,
                source = "AI_SCAN",
                isVerified = false
            ))

            val ratio = item.portionG / 100.0
            mealLogRepo.save(MealLog(
                userId = userId,
                foodItem = foodItem,
                quantityG = item.portionG,
                mealType = request.mealType,
                caloriesSnapshot = (foodItem.caloriesPer100g * ratio).round1(),
                proteinGSnapshot = (foodItem.proteinPer100g * ratio).round1(),
                carbsGSnapshot = (foodItem.carbsPer100g * ratio).round1(),
                fatGSnapshot = (foodItem.fatPer100g * ratio).round1(),
                loggedAt = date
            ))
            count++
        }

        return count
    }

    private fun findFoodByName(name: String): FoodItem? {
        val results = foodItemRepo.search(name, null, PageRequest.of(0, 1))
        return results.content.firstOrNull()
    }

    private fun Double.round1() = Math.round(this * 10) / 10.0
}
