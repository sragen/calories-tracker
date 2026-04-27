package com.company.app.modules.subscription

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "subscription_plans")
class SubscriptionPlan(
    @Column(nullable = false)
    var name: String,

    @Column
    var description: String? = null,

    @Column(name = "price_idr", nullable = false)
    var priceIdr: Long,

    @Column(name = "duration_days", nullable = false)
    var durationDays: Int,

    @Column(columnDefinition = "TEXT")
    var features: String? = null,   // JSON array string

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
