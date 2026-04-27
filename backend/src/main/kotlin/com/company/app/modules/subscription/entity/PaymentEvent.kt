package com.company.app.modules.subscription.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "payment_events")
class PaymentEvent(
    @Column(name = "subscription_id")
    val subscriptionId: Long? = null,

    @Column(name = "user_id")
    val userId: Long? = null,

    @Column(nullable = false)
    val platform: String,

    @Column(name = "event_type", nullable = false)
    val eventType: String,

    @Column(name = "platform_order_id")
    val platformOrderId: String? = null,

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    val rawPayload: String? = null,

    @Column(name = "processed_at")
    val processedAt: LocalDateTime? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
}
