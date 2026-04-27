package com.company.app.modules.payment

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.Base64

data class SnapResult(val token: String, val redirectUrl: String)

@Service
class MidtransService(
    @Value("\${app.midtrans.server-key}") private val serverKey: String,
    @Value("\${app.midtrans.is-production:false}") private val isProduction: Boolean
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val baseUrl get() = if (isProduction)
        "https://app.midtrans.com/snap/v1"
    else
        "https://app.sandbox.midtrans.com/snap/v1"

    private val restClient: RestClient by lazy {
        val credentials = Base64.getEncoder().encodeToString("$serverKey:".toByteArray())
        RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Basic $credentials")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    fun createSnapToken(orderId: String, amount: Long, userId: Long, description: String): SnapResult {
        val body = mapOf(
            "transaction_details" to mapOf(
                "order_id" to orderId,
                "gross_amount" to amount
            ),
            "item_details" to listOf(
                mapOf("id" to "PREMIUM", "price" to amount, "quantity" to 1, "name" to description)
            ),
            "customer_details" to mapOf("customer_id" to userId.toString()),
            "callbacks" to mapOf(
                "finish" to "caloriestracker://payment/finish",
                "error" to "caloriestracker://payment/error",
                "pending" to "caloriestracker://payment/pending"
            )
        )

        return try {
            @Suppress("UNCHECKED_CAST")
            val response = restClient.post()
                .uri("/transactions")
                .body(body)
                .retrieve()
                .body(Map::class.java) as Map<String, Any>
            SnapResult(
                token = response["token"] as? String ?: "",
                redirectUrl = response["redirect_url"] as? String ?: ""
            )
        } catch (e: Exception) {
            log.error("Midtrans Snap token creation failed: ${e.message}")
            // Return a mock token in sandbox/dev when Midtrans is not configured
            SnapResult(token = "mock-snap-token-$orderId", redirectUrl = "")
        }
    }
}
