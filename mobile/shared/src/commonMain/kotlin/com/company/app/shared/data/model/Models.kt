package com.company.app.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Auth ──────────────────────────────────────────────────────────

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val email: String, val password: String, val name: String)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 86400
)

@Serializable
data class UserResponse(
    val id: Long,
    val email: String? = null,
    val phone: String? = null,
    val name: String,
    val role: String,
    val status: String,
    val avatarUrl: String? = null,
    val createdAt: String
)

// ── Remote Config ─────────────────────────────────────────────────

@Serializable
data class RemoteConfig(val key: String, val value: String, val type: String)

@Serializable
data class AppConfigs(
    val maintenanceMode: Boolean = false,
    val forceUpdate: Boolean = false,
    val minAppVersion: String = "1.0.0",
    val pushNotification: Boolean = true,
    val promoBannerUrl: String = "",
    val maxRetryLogin: Int = 5,
    val aiScanEnabled: Boolean = true,
    val freeDailyLogLimit: Int = 10,
    val barcodeScanEnabled: Boolean = true
) {
    companion object {
        fun from(configs: List<RemoteConfig>): AppConfigs {
            val map = configs.associate { it.key to it.value }
            return AppConfigs(
                maintenanceMode    = map["maintenance_mode"]?.toBooleanStrictOrNull() ?: false,
                forceUpdate        = map["force_update"]?.toBooleanStrictOrNull() ?: false,
                minAppVersion      = map["min_app_version"] ?: "1.0.0",
                pushNotification   = map["push_notification"]?.toBooleanStrictOrNull() ?: true,
                promoBannerUrl     = map["promo_banner_url"] ?: "",
                maxRetryLogin      = map["max_retry_login"]?.toIntOrNull() ?: 5,
                aiScanEnabled      = map["ai_scan_enabled"]?.toBooleanStrictOrNull() ?: true,
                freeDailyLogLimit  = map["free_daily_log_limit"]?.toIntOrNull() ?: 10,
                barcodeScanEnabled = map["barcode_scan_enabled"]?.toBooleanStrictOrNull() ?: true
            )
        }
    }
}

// ── Food ──────────────────────────────────────────────────────────

@Serializable
data class FoodCategory(
    val id: Long,
    val name: String,
    val nameEn: String? = null,
    val icon: String? = null
)

@Serializable
data class FoodItem(
    val id: Long,
    val name: String,
    val nameEn: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fiberPer100g: Double? = null,
    val defaultServingG: Double = 100.0,
    val servingDescription: String? = null,
    val barcode: String? = null,
    val source: String = "ADMIN",
    val isVerified: Boolean = false,
    val createdAt: String = ""
) {
    fun caloriesForServing(quantityG: Double): Double =
        (caloriesPer100g * quantityG / 100.0 * 10).toLong() / 10.0

    fun proteinForServing(quantityG: Double): Double =
        (proteinPer100g * quantityG / 100.0 * 10).toLong() / 10.0

    fun carbsForServing(quantityG: Double): Double =
        (carbsPer100g * quantityG / 100.0 * 10).toLong() / 10.0

    fun fatForServing(quantityG: Double): Double =
        (fatPer100g * quantityG / 100.0 * 10).toLong() / 10.0
}

@Serializable
data class FoodPage(
    val content: List<FoodItem>,
    val page: PageMeta
)

@Serializable
data class PageMeta(val totalPages: Int = 0, val number: Int = 0, val totalElements: Long = 0)

@Serializable
data class FoodSubmitRequest(
    val name: String,
    val nameEn: String? = null,
    val categoryId: Long? = null,
    val caloriesPer100g: Double,
    val proteinPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val fiberPer100g: Double? = null,
    val defaultServingG: Double = 100.0,
    val barcode: String? = null
)

// ── Body Profile ──────────────────────────────────────────────────

@Serializable
data class BodyProfileRequest(
    val heightCm: Double,
    val weightKg: Double,
    val birthDate: String,      // YYYY-MM-DD
    val gender: String,         // MALE | FEMALE
    val activityLevel: String,  // SEDENTARY | LIGHTLY_ACTIVE | MODERATELY_ACTIVE | VERY_ACTIVE | EXTRA_ACTIVE
    val goal: String,           // LOSE | MAINTAIN | GAIN
    val targetWeightKg: Double? = null
)

@Serializable
data class BodyProfileResponse(
    val heightCm: Double,
    val weightKg: Double,
    val birthDate: String,
    val gender: String,
    val activityLevel: String,
    val goal: String,
    val targetWeightKg: Double? = null,
    val bmrKcal: Double? = null,
    val tdeeKcal: Double? = null,
    val recommendedCalories: Double? = null,
    val updatedAt: String = ""
)

@Serializable
data class BmrPreviewResponse(
    val bmrKcal: Double,
    val tdeeKcal: Double,
    val recommendedCalories: Double,
    val recommendedProteinG: Double,
    val recommendedCarbsG: Double,
    val recommendedFatG: Double
)

@Serializable
data class HasProfileResponse(val hasProfile: Boolean)

// ── Daily Goal ────────────────────────────────────────────────────

@Serializable
data class DailyGoalResponse(
    val targetCalories: Double,
    val targetProteinG: Double,
    val targetCarbsG: Double,
    val targetFatG: Double,
    val autoCalculated: Boolean,
    val updatedAt: String = ""
)

// ── Meal Log ──────────────────────────────────────────────────────

@Serializable
data class MealLogRequest(
    val foodItemId: Long,
    val quantityG: Double,
    val mealType: String,
    val loggedAt: String,       // YYYY-MM-DD
    val aiScanPhotoUrl: String? = null
)

@Serializable
data class MealLogUpdateRequest(
    val quantityG: Double? = null,
    val mealType: String? = null
)

@Serializable
data class MealLogEntry(
    val id: Long,
    val foodItem: FoodItem,
    val quantityG: Double,
    val mealType: String,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val aiScanPhotoUrl: String? = null,
    val loggedAt: String,
    val createdAt: String
)

@Serializable
data class NutritionSummary(
    val totalCalories: Double,
    val totalProteinG: Double,
    val totalCarbsG: Double,
    val totalFatG: Double,
    val targetCalories: Double,
    val remainingCalories: Double,
    val caloriesPercent: Int
)

@Serializable
data class DailySummary(
    val date: String,
    val summary: NutritionSummary,
    val meals: Map<String, List<MealLogEntry>>
)

// ── Analytics ─────────────────────────────────────────────────────

@Serializable
data class DailyRangeSummary(
    val date: String,
    val totalCalories: Double,
    val totalProteinG: Double,
    val totalCarbsG: Double,
    val totalFatG: Double
)

// ── Subscription (IAP) ───────────────────────────────────────────

@Serializable
data class SubscriptionPlanResponse(
    val id: Long,
    val name: String,
    val priceIdr: Long,
    val intervalDays: Int,
    val trialDays: Int,
    val productIdAndroid: String? = null,
    val productIdIos: String? = null,
)

@Serializable
data class EntitlementResponse(
    val entitled: Boolean,
    val source: String? = null,
    val status: String? = null,
    val expiresAt: String? = null,
    val gracePeriodEndsAt: String? = null
)

@Serializable
data class VerifyPurchaseRequest(
    val platform: String,
    val planId: Long,
    val purchaseToken: String,
    val orderId: String
)

@Serializable
data class RestorePurchaseRequest(
    val platform: String,
    val originalTransactionId: String
)

@Serializable
data class SubscriptionStatusResponse(
    val subscriptionId: Long,
    val planName: String,
    val priceIdr: Long,
    val status: String,
    val platform: String,
    val trialEndsAt: String? = null,
    val currentPeriodStart: String? = null,
    val currentPeriodEnd: String? = null,
    val cancelledAt: String? = null
)

// ── AI Scan ───────────────────────────────────────────────────────

@Serializable
data class AiDetectedFood(
    val name: String,
    val portionG: Double,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val matchedFoodId: Long? = null,
    val totalCalories: Double = 0.0
)

@Serializable
data class AiScanResponse(
    val scanLogId: Long,
    val imageUrl: String? = null,
    val detectedFoods: List<AiDetectedFood>
)

@Serializable
data class AiScanConfirmRequest(
    val scanLogId: Long,
    val selectedFoods: List<AiDetectedFood>,
    val mealType: String,
    val loggedAt: String
)

// ── Error ─────────────────────────────────────────────────────────

@Serializable
data class ApiError(
    val status: Int = 0,
    val error: String = "",
    val message: String = "Unknown error"
)
