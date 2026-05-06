package com.company.app.modules.userfood

import com.company.app.common.exception.AppException
import com.company.app.modules.food.FoodItem
import com.company.app.modules.food.FoodItemRepository
import com.company.app.modules.meal.MealLog
import com.company.app.modules.meal.MealLogRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

private val VALID_MEAL_TYPES = setOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")

@Service
class UserFoodLibraryService(
    private val repository: UserFoodLibraryRepository,
    private val foodItemRepo: FoodItemRepository,
    private val mealLogRepo: MealLogRepository
) {

    fun list(userId: Long, sort: String, search: String?, pageable: PageRequest): Page<UserFoodResponse> {
        val sanitizedSearch = search?.takeIf { it.isNotBlank() } ?: ""
        return when (sort.uppercase()) {
            "MOST_USED"    -> repository.findByUserIdOrderByUseCount(userId, sanitizedSearch, pageable)
            "ALPHABETICAL" -> repository.findByUserIdOrderByName(userId, sanitizedSearch, pageable)
            else           -> repository.findByUserIdOrderByLastUsed(userId, sanitizedSearch, pageable)
        }.map { it.toResponse() }
    }

    fun save(userId: Long, req: SaveUserFoodRequest): UserFoodResponse {
        repository.findByUserIdAndFoodNameIgnoreCaseAndDeletedAtIsNull(userId, req.foodName)
            ?.let { return it.toResponse() }

        return repository.save(
            UserFoodLibrary(
                userId = userId,
                foodName = req.foodName,
                caloriesPer100g = req.caloriesPer100g,
                proteinPer100g = req.proteinPer100g,
                carbsPer100g = req.carbsPer100g,
                fatPer100g = req.fatPer100g,
                servingSizeG = req.servingSizeG,
                imageUrl = req.imageUrl,
                source = req.source,
                originalFoodId = req.originalFoodId
            )
        ).toResponse()
    }

    @Transactional
    fun log(userId: Long, id: Long, req: QuickLogRequest): QuickLogResponse {
        val mealType = req.mealType.uppercase()
        if (mealType !in VALID_MEAL_TYPES) {
            throw AppException.badRequest("mealType harus salah satu dari: ${VALID_MEAL_TYPES.joinToString()}")
        }

        val userFood = repository.findByUserIdAndIdAndDeletedAtIsNull(userId, id)
            ?: throw AppException.notFound("Saved food not found")

        val portionG = req.portionG ?: userFood.servingSizeG
        val ratio = portionG / 100.0

        val caloriesLogged = (userFood.caloriesPer100g * ratio).round1()
        val proteinGLogged  = (userFood.proteinPer100g  * ratio).round1()
        val carbsGLogged    = (userFood.carbsPer100g    * ratio).round1()
        val fatGLogged      = (userFood.fatPer100g      * ratio).round1()

        val foodItem = userFood.originalFoodId
            ?.let { foodItemRepo.findById(it).orElse(null) }
            ?: foodItemRepo.save(
                FoodItem(
                    name = userFood.foodName,
                    caloriesPer100g = userFood.caloriesPer100g,
                    proteinPer100g = userFood.proteinPer100g,
                    carbsPer100g = userFood.carbsPer100g,
                    fatPer100g = userFood.fatPer100g,
                    source = "USER_FOOD",
                    isVerified = false
                )
            )

        val mealLog = mealLogRepo.save(
            MealLog(
                userId = userId,
                foodItem = foodItem,
                quantityG = portionG,
                mealType = mealType,
                caloriesSnapshot = caloriesLogged,
                proteinGSnapshot = proteinGLogged,
                carbsGSnapshot = carbsGLogged,
                fatGSnapshot = fatGLogged,
                aiScanPhotoUrl = userFood.imageUrl,
                loggedAt = LocalDate.parse(req.loggedAt)
            )
        )

        userFood.useCount++
        userFood.lastUsedAt = LocalDateTime.now()
        repository.save(userFood)

        return QuickLogResponse(
            mealLogId = mealLog.id,
            logged = true,
            foodName = userFood.foodName,
            mealType = mealType,
            portionG = portionG,
            caloriesLogged = caloriesLogged,
            proteinGLogged = proteinGLogged,
            carbsGLogged = carbsGLogged,
            fatGLogged = fatGLogged
        )
    }

    fun delete(userId: Long, id: Long) {
        val userFood = repository.findByUserIdAndIdAndDeletedAtIsNull(userId, id)
            ?: throw AppException.notFound("Saved food not found")
        userFood.deletedAt = LocalDateTime.now()
        repository.save(userFood)
    }

    private fun Double.round1() = Math.round(this * 10) / 10.0
}
