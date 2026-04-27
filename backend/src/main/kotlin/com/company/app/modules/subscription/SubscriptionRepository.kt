package com.company.app.modules.subscription

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface SubscriptionPlanRepository : JpaRepository<SubscriptionPlan, Long> {
    fun findByIsActiveTrue(): List<SubscriptionPlan>
}

interface UserSubscriptionRepository : JpaRepository<UserSubscription, Long> {
    @Query("""
        SELECT s FROM UserSubscription s
        WHERE s.userId = :userId
          AND s.status = 'ACTIVE'
          AND s.expiresAt > :now
        ORDER BY s.expiresAt DESC
    """)
    fun findActiveByUserId(userId: Long, now: LocalDateTime = LocalDateTime.now()): List<UserSubscription>

    fun findTopByUserIdOrderByCreatedAtDesc(userId: Long): UserSubscription?

    fun findByUserId(userId: Long): List<UserSubscription>
}
