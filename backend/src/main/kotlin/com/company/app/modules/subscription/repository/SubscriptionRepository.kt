package com.company.app.modules.subscription.repository

import com.company.app.common.crud.BaseRepository
import com.company.app.modules.subscription.entity.Subscription
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SubscriptionRepository : BaseRepository<Subscription, Long> {

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.userId = :userId
          AND s.status IN ('TRIAL', 'ACTIVE', 'PAST_DUE')
          AND s.deletedAt IS NULL
    """)
    fun findActiveByUserId(userId: Long): Subscription?

    fun findByPlatformOrderId(orderId: String): Subscription?

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.platformPurchaseToken = :token
          AND s.platform = :platform
          AND s.deletedAt IS NULL
    """)
    fun findByTokenAndPlatform(token: String, platform: String): Subscription?

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.platformOriginalTransactionId = :txId
          AND s.platform = :platform
          AND s.deletedAt IS NULL
    """)
    fun findByOriginalTransactionIdAndPlatform(txId: String, platform: String): Subscription?

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.userId = :userId
          AND s.status IN ('TRIAL', 'ACTIVE', 'PAST_DUE')
          AND s.deletedAt IS NULL
    """)
    fun findAllActiveByUserId(userId: Long): List<Subscription>

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.deletedAt IS NULL
          AND (:status IS NULL OR s.status = :status)
          AND (:platform IS NULL OR s.platform = :platform)
    """)
    fun findAllActiveFiltered(
        pageable: Pageable,
        @org.springframework.data.repository.query.Param("status") status: String?,
        @org.springframework.data.repository.query.Param("platform") platform: String?
    ): Page<Subscription>

    @Modifying
    @Query("""
        UPDATE Subscription s
        SET s.status = 'EXPIRED', s.updatedAt = :now
        WHERE s.userId = :userId
          AND s.status IN ('TRIAL', 'ACTIVE', 'PAST_DUE')
          AND s.deletedAt IS NULL
    """)
    fun expirePreviousForUser(userId: Long, now: LocalDateTime = LocalDateTime.now())
}
