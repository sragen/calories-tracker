package com.company.app.modules.subscription.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "subscription_plans")
class SubscriptionPlan(
    @Column(nullable = false)
    var name: String,

    @Column(name = "price_idr", nullable = false)
    var priceIdr: Long,

    @Column(name = "interval_days", nullable = false)
    var intervalDays: Int = 30,

    @Column(name = "trial_days", nullable = false)
    var trialDays: Int = 7,

    @Column(name = "platform_product_id_android")
    var platformProductIdAndroid: String? = null,

    @Column(name = "platform_product_id_ios")
    var platformProductIdIos: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    fun platformProductId(platform: String): String = when (platform) {
        "GOOGLE_PLAY" -> platformProductIdAndroid ?: error("No Android product ID for plan $id")
        "APP_STORE"   -> platformProductIdIos ?: error("No iOS product ID for plan $id")
        else -> error("Unknown platform: $platform")
    }
}
