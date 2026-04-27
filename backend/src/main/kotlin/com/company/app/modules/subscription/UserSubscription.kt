package com.company.app.modules.subscription

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_subscriptions")
class UserSubscription(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    var plan: SubscriptionPlan,

    @Column(nullable = false)
    var status: String = "PENDING",     // PENDING | ACTIVE | EXPIRED | CANCELLED

    @Column(name = "started_at")
    var startedAt: LocalDateTime? = null,

    @Column(name = "expires_at")
    var expiresAt: LocalDateTime? = null,

    @Column(name = "payment_id")
    var paymentId: String? = null,

    @Column(name = "payment_method")
    var paymentMethod: String? = null,

    @Column(name = "snap_token")
    var snapToken: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
