package com.company.app.modules.subscription.webhook

import com.company.app.modules.subscription.client.AppleReceiptClient
import com.company.app.modules.subscription.service.SubscriptionStateMachine
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Base64

@Component
class AppStoreWebhookHandler(
    private val stateMachine: SubscriptionStateMachine,
    private val appleClient: AppleReceiptClient,
    private val objectMapper: ObjectMapper,
    @Value("\${app.subscription.grace-period-days:3}") private val gracePeriodDays: Long
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class NotificationPayload(
        val notificationType: String? = null,
        val subtype: String? = null,
        val data: NotificationData? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class NotificationData(
        val signedTransactionInfo: String? = null,
        val signedRenewalInfo: String? = null
    )

    fun handle(signedPayload: String) {
        val payloadJson = try {
            decodeJwsPayloadUnsafe(signedPayload)
        } catch (e: Exception) {
            log.error("App Store webhook: failed to decode outer JWS payload", e)
            return
        }

        val notification = objectMapper.readValue(payloadJson, NotificationPayload::class.java)
        val notificationType = notification.notificationType ?: return
        val subtype = notification.subtype
        val signedTxInfo = notification.data?.signedTransactionInfo ?: return

        val txResult = try {
            appleClient.validate(signedTxInfo)
        } catch (e: Exception) {
            log.error("App Store webhook: transaction JWS verification failed", e)
            return
        }

        val originalTxId = txResult.originalTransactionId
        log.info("App Store ASN: type=$notificationType subtype=$subtype originalTxId=$originalTxId")

        val gracePeriodEnd = LocalDateTime.now().plusDays(gracePeriodDays)
        val newPeriodEnd = txResult.currentPeriodEnd

        when (notificationType) {
            "SUBSCRIBED" -> when (subtype) {
                "INITIAL_BUY" -> log.debug("App Store SUBSCRIBED/INITIAL_BUY — handled by /verify endpoint")
                "RESUBSCRIBE" -> stateMachine.onRenewed(originalTxId, "APP_STORE", newPeriodEnd, signedPayload)
                else -> log.debug("App Store SUBSCRIBED/$subtype — no action")
            }
            "DID_RENEW" -> stateMachine.onRenewed(originalTxId, "APP_STORE", newPeriodEnd, signedPayload)
            "DID_FAIL_TO_RENEW" -> when (subtype) {
                "GRACE_PERIOD" -> stateMachine.onPaymentFailed(originalTxId, "APP_STORE", gracePeriodEnd, signedPayload)
                else -> stateMachine.onExpired(originalTxId, "APP_STORE", signedPayload)
            }
            "GRACE_PERIOD_EXPIRED" -> stateMachine.onExpired(originalTxId, "APP_STORE", signedPayload)
            "EXPIRED" -> when (subtype) {
                "VOLUNTARY" -> stateMachine.onCancelled(originalTxId, "APP_STORE", signedPayload)
                else -> stateMachine.onExpired(originalTxId, "APP_STORE", signedPayload)
            }
            "REVOKE" -> stateMachine.onExpired(originalTxId, "APP_STORE", signedPayload)
            else -> log.debug("App Store unhandled notificationType=$notificationType")
        }
    }

    // Only decodes the payload portion of a JWS without full cert verification.
    // The inner signedTransactionInfo JWS is verified by AppleReceiptClient.
    private fun decodeJwsPayloadUnsafe(jws: String): String {
        val parts = jws.split(".")
        if (parts.size != 3) error("Invalid JWS format")
        return Base64.getUrlDecoder().decode(parts[1]).toString(Charsets.UTF_8)
    }
}
