package com.company.app.modules.payment

import com.company.app.modules.subscription.SubscriptionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.MessageDigest

@RestController
@RequestMapping("/api/payment")
class MidtransController(
    private val subscriptionService: SubscriptionService,
    @Value("\${app.midtrans.server-key}") private val serverKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/notification")
    fun notification(@RequestBody payload: Map<String, Any>): ResponseEntity<Void> {
        if (!verifySignature(payload)) {
            log.warn("Midtrans webhook signature mismatch — rejecting notification")
            return ResponseEntity.status(403).build()
        }
        subscriptionService.handlePaymentNotification(payload)
        return ResponseEntity.ok().build()
    }

    private fun verifySignature(payload: Map<String, Any>): Boolean {
        val orderId = payload["order_id"] as? String ?: return false
        val statusCode = payload["status_code"] as? String ?: return false
        val grossAmount = payload["gross_amount"] as? String ?: return false
        val signatureKey = payload["signature_key"] as? String ?: return false

        val raw = "$orderId$statusCode$grossAmount$serverKey"
        val digest = MessageDigest.getInstance("SHA-512").digest(raw.toByteArray())
        val expected = digest.joinToString("") { "%02x".format(it) }

        return MessageDigest.isEqual(expected.toByteArray(), signatureKey.toByteArray())
    }
}
