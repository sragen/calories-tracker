package com.company.app.modules.subscription.entity

import com.company.app.common.crud.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "subscriptions")
class Subscription(
    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "plan_id", nullable = false)
    var planId: Long,

    @Column(nullable = false)
    var status: String,

    @Column(nullable = false)
    var platform: String,

    @Column(name = "platform_purchase_token", length = 1000)
    var platformPurchaseToken: String? = null,

    @Column(name = "platform_original_transaction_id")
    var platformOriginalTransactionId: String? = null,

    @Column(name = "platform_order_id", unique = true)
    var platformOrderId: String? = null,

    @Column(name = "trial_ends_at")
    var trialEndsAt: LocalDateTime? = null,

    @Column(name = "current_period_start")
    var currentPeriodStart: LocalDateTime? = null,

    @Column(name = "current_period_end")
    var currentPeriodEnd: LocalDateTime? = null,

    @Column(name = "grace_period_ends_at")
    var gracePeriodEndsAt: LocalDateTime? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null
) : BaseEntity()
