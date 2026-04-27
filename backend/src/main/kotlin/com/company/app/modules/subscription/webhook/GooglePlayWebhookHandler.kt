package com.company.app.modules.subscription.webhook

import com.company.app.modules.subscription.client.GooglePlayReceiptClient
import com.company.app.modules.subscription.service.SubscriptionStateMachine
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.Base64

@Component
class GooglePlayWebhookHandler(
    private val stateMachine: SubscriptionStateMachine,
    private val googlePlayClient: GooglePlayReceiptClient,
    private val objectMapper: ObjectMapper,
    @Value("\${app.subscription.grace-period-days:3}") private val gracePeriodDays: Long
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val RECOVERED = 1
    private val RENEWED   = 2
    private val CANCELED  = 3
    private val PURCHASED = 4
    private val ON_HOLD   = 5
    private val IN_GRACE  = 6
    private val RESTARTED = 7
    private val REVOKED   = 12
    private val EXPIRED   = 13

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PubSubBody(val message: PubSubMessage? = null)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PubSubMessage(val data: String? = null)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DeveloperNotification(
        val subscriptionNotification: SubscriptionNotification? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SubscriptionNotification(
        val notificationType: Int? = null,
        val purchaseToken: String? = null,
        val subscriptionId: String? = null  // product ID, e.g. "premium_monthly"
    )

    fun handle(body: Map<String, Any>) {
        val raw = objectMapper.writeValueAsString(body)
        val pubSub = objectMapper.readValue(raw, PubSubBody::class.java)
        val encodedData = pubSub.message?.data ?: run {
            log.warn("Google Play webhook: missing message.data")
            return
        }

        val decoded = Base64.getDecoder().decode(encodedData).toString(Charsets.UTF_8)
        val notification = objectMapper.readValue(decoded, DeveloperNotification::class.java)
        val sub = notification.subscriptionNotification ?: run {
            log.debug("Google Play webhook: no subscriptionNotification — skipping")
            return
        }

        val token = sub.purchaseToken ?: return
        val productId = sub.subscriptionId ?: ""
        val type = sub.notificationType ?: return

        log.info("Google Play RTDN: type=$type productId=$productId")

        when (type) {
            RENEWED, RESTARTED, RECOVERED -> {
                val periodEnd = resolveNewPeriodEnd(token, productId)
                if (type == RECOVERED) {
                    stateMachine.onRecovered(token, "GOOGLE_PLAY", periodEnd, decoded)
                } else {
                    stateMachine.onRenewed(token, "GOOGLE_PLAY", periodEnd, decoded)
                }
            }
            ON_HOLD, IN_GRACE -> {
                val gracePeriodEnd = LocalDateTime.now().plusDays(gracePeriodDays)
                stateMachine.onPaymentFailed(token, "GOOGLE_PLAY", gracePeriodEnd, decoded)
            }
            CANCELED  -> stateMachine.onCancelled(token, "GOOGLE_PLAY", decoded)
            EXPIRED, REVOKED -> stateMachine.onExpired(token, "GOOGLE_PLAY", decoded)
            PURCHASED -> log.debug("Google Play PURCHASED — handled by /api/subscription/verify")
            else      -> log.warn("Google Play unhandled notificationType=$type")
        }
    }

    private fun resolveNewPeriodEnd(purchaseToken: String, productId: String): LocalDateTime =
        try {
            googlePlayClient.validate(purchaseToken, productId).currentPeriodEnd
        } catch (e: Exception) {
            log.error("Could not fetch new period end from Google Play API — falling back to +30d", e)
            LocalDateTime.now().plusDays(30)
        }
}
