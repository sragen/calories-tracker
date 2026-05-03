package com.company.app.shared.data.network

import com.company.app.shared.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiService(
    private val baseUrl: String,
    private val tokenProvider: () -> String?
) {
    private val client = HttpClient(platformHttpClient().engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(Logging) { level = LogLevel.INFO }
        expectSuccess = true
    }

    private fun HttpRequestBuilder.auth() {
        tokenProvider()?.let { bearerAuth(it) }
    }

    // ── Auth ──────────────────────────────────────────────────────

    suspend fun register(email: String, password: String, name: String): AuthResponse =
        client.post("$baseUrl/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password, name))
        }.body()

    suspend fun login(email: String, password: String): AuthResponse =
        client.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()

    suspend fun googleLogin(idToken: String): AuthResponse =
        client.post("$baseUrl/api/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleLoginRequest(idToken))
        }.body()

    suspend fun getMe(): UserResponse =
        client.get("$baseUrl/api/auth/me") { auth() }.body()

    // ── Config ────────────────────────────────────────────────────

    suspend fun getConfigs(): List<RemoteConfig> =
        client.get("$baseUrl/api/config").body()

    // ── Food ──────────────────────────────────────────────────────

    suspend fun searchFoods(q: String? = null, categoryId: Long? = null, page: Int = 0): FoodPage =
        client.get("$baseUrl/api/foods") {
            auth()
            q?.let { parameter("q", it) }
            categoryId?.let { parameter("categoryId", it) }
            parameter("page", page)
            parameter("size", 20)
        }.body()

    suspend fun getFoodById(id: Long): FoodItem =
        client.get("$baseUrl/api/foods/$id") { auth() }.body()

    suspend fun getFoodByBarcode(barcode: String): FoodItem =
        client.get("$baseUrl/api/foods/barcode/$barcode") { auth() }.body()

    suspend fun getFoodCategories(): List<FoodCategory> =
        client.get("$baseUrl/api/foods/categories") { auth() }.body()

    suspend fun submitFood(req: FoodSubmitRequest): FoodItem =
        client.post("$baseUrl/api/foods/submit") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    // ── Body Profile ──────────────────────────────────────────────

    suspend fun getBodyProfile(): BodyProfileResponse =
        client.get("$baseUrl/api/profile/body") { auth() }.body()

    suspend fun hasBodyProfile(): HasProfileResponse =
        client.get("$baseUrl/api/profile/body/exists") { auth() }.body()

    suspend fun saveBodyProfile(req: BodyProfileRequest): BodyProfileResponse =
        client.post("$baseUrl/api/profile/body") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun previewBmr(req: BodyProfileRequest): BmrPreviewResponse =
        client.post("$baseUrl/api/profile/body/bmr-preview") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    // ── Daily Goal ────────────────────────────────────────────────

    suspend fun getDailyGoal(): DailyGoalResponse =
        client.get("$baseUrl/api/goals/daily") { auth() }.body()

    // ── Meal Logs ─────────────────────────────────────────────────

    suspend fun getDiary(date: String): DailySummary =
        client.get("$baseUrl/api/meal-logs") {
            auth()
            parameter("date", date)
        }.body()

    suspend fun addMealLog(req: MealLogRequest): MealLogEntry =
        client.post("$baseUrl/api/meal-logs") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun updateMealLog(id: Long, req: MealLogUpdateRequest): MealLogEntry =
        client.put("$baseUrl/api/meal-logs/$id") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun deleteMealLog(id: Long) =
        client.delete("$baseUrl/api/meal-logs/$id") { auth() }

    suspend fun getMealSummaryRange(from: String, to: String): List<DailyRangeSummary> =
        client.get("$baseUrl/api/meal-logs/summary") {
            auth()
            parameter("from", from)
            parameter("to", to)
        }.body()

    suspend fun getStreak(): Int {
        val result: Map<String, Int> = client.get("$baseUrl/api/meal-logs/streak") { auth() }.body()
        return result["streak"] ?: 0
    }

    // ── Subscriptions (IAP) ───────────────────────────────────────

    suspend fun getSubscriptionPlans(): List<SubscriptionPlanResponse> =
        client.get("$baseUrl/api/subscription/plans").body()

    suspend fun getEntitlement(): EntitlementResponse =
        client.get("$baseUrl/api/subscription/entitlement") { auth() }.body()

    suspend fun verifyPurchase(req: VerifyPurchaseRequest): EntitlementResponse =
        client.post("$baseUrl/api/subscription/verify") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun restorePurchase(req: RestorePurchaseRequest): EntitlementResponse =
        client.post("$baseUrl/api/subscription/restore") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun getSubscriptionStatus(): SubscriptionStatusResponse =
        client.get("$baseUrl/api/subscription/status") { auth() }.body()

    // ── AI Scan ───────────────────────────────────────────────────

    suspend fun analyzeAiScan(imageBytes: ByteArray, mimeType: String = "image/jpeg"): AiScanResponse =
        client.post("$baseUrl/api/ai-scan/analyze") {
            auth()
            setBody(MultiPartFormDataContent(formData {
                append("image", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"scan.jpg\"")
                })
            }))
        }.body()

    suspend fun confirmAiScan(req: AiScanConfirmRequest): Map<String, Int> =
        client.post("$baseUrl/api/ai-scan/confirm") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
}
