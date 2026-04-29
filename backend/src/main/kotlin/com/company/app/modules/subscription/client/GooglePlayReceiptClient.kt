package com.company.app.modules.subscription.client

import com.company.app.common.exception.AppException
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.ServiceAccountCredentials
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.ByteArrayInputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class GooglePlayReceiptClient(
    @Value("\${app.subscription.google-play.package-name:\${GOOGLE_PLAY_PACKAGE_NAME:com.company.app}}") private val packageName: String,
    @Value("\${app.subscription.google-play.service-account-json:}") private val serviceAccountJson: String,
    private val objectMapper: ObjectMapper
) {
    private val restTemplate = RestTemplate()
    private val baseUrl = "https://androidpublisher.googleapis.com/androidpublisher/v3"

    data class ValidationResult(
        val orderId: String,
        val status: String,
        val currentPeriodStart: LocalDateTime,
        val currentPeriodEnd: LocalDateTime,
        val trialEndsAt: LocalDateTime?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SubscriptionV2Response(
        val startTime: String? = null,
        val subscriptionState: String? = null,
        val latestOrderId: String? = null,
        val lineItems: List<LineItem> = emptyList()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class LineItem(
        val expiryTime: String? = null,
        val offerDetails: OfferDetails? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OfferDetails(
        val offerType: Int? = null
    )

    fun validate(purchaseToken: String, productId: String): ValidationResult {
        val accessToken = getAccessToken()
        val url = "$baseUrl/applications/$packageName/purchases/subscriptionsv2/tokens/$purchaseToken"

        val headers = HttpHeaders().apply { setBearerAuth(accessToken) }
        val response = try {
            restTemplate.exchange(url, HttpMethod.GET, HttpEntity<Unit>(headers), String::class.java)
        } catch (e: Exception) {
            throw AppException.badRequest("Google Play receipt validation failed: ${e.message}")
        }

        val body = objectMapper.readValue(response.body, SubscriptionV2Response::class.java)

        val expiryTime = body.lineItems.firstOrNull()?.expiryTime
            ?: throw AppException.badRequest("Invalid Google Play receipt: no expiry time")

        val periodEnd = Instant.parse(expiryTime).toLocalDateTime()
        val periodStart = body.startTime?.let { Instant.parse(it).toLocalDateTime() }
            ?: LocalDateTime.now()

        val isInTrial = body.lineItems.firstOrNull()?.offerDetails?.offerType == 2
        val status = when {
            isInTrial -> "TRIAL"
            body.subscriptionState == "SUBSCRIPTION_STATE_ACTIVE" -> "ACTIVE"
            else -> "ACTIVE"
        }

        return ValidationResult(
            orderId = body.latestOrderId ?: purchaseToken.take(50),
            status = status,
            currentPeriodStart = periodStart,
            currentPeriodEnd = periodEnd,
            trialEndsAt = if (isInTrial) periodEnd else null
        )
    }

    private fun getAccessToken(): String {
        if (serviceAccountJson.isBlank()) throw AppException.badRequest("Google Play service account not configured")
        val credentials = ServiceAccountCredentials
            .fromStream(ByteArrayInputStream(serviceAccountJson.toByteArray()))
            .createScoped("https://www.googleapis.com/auth/androidpublisher")
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    private fun Instant.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(this, ZoneId.of("UTC"))
}
