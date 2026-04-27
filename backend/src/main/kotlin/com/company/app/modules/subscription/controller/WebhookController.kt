package com.company.app.modules.subscription.controller

import com.company.app.modules.subscription.webhook.AppStoreWebhookHandler
import com.company.app.modules.subscription.webhook.GooglePlayWebhookHandler
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/webhook")
@Tag(name = "Webhooks")
class WebhookController(
    private val googlePlayHandler: GooglePlayWebhookHandler,
    private val appStoreHandler: AppStoreWebhookHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // Always return 200 — prevents retry storms from both platforms.
    // Errors are logged internally; the event is recorded in payment_events.

    @PostMapping("/google-play")
    fun googlePlay(@RequestBody body: Map<String, Any>): ResponseEntity<Void> {
        runCatching { googlePlayHandler.handle(body) }
            .onFailure { log.error("Google Play webhook processing error", it) }
        return ResponseEntity.ok().build()
    }

    @PostMapping("/app-store")
    fun appStore(@RequestBody body: Map<String, String>): ResponseEntity<Void> {
        val signedPayload = body["signedPayload"] ?: run {
            log.warn("App Store webhook: missing signedPayload field")
            return ResponseEntity.ok().build()
        }
        runCatching { appStoreHandler.handle(signedPayload) }
            .onFailure { log.error("App Store webhook processing error", it) }
        return ResponseEntity.ok().build()
    }
}
