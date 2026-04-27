package com.company.app.modules.subscription.repository

import com.company.app.modules.subscription.entity.PremiumWhitelist
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PremiumWhitelistRepository : JpaRepository<PremiumWhitelist, Long> {
    fun existsByUserId(userId: Long): Boolean
    fun findByUserId(userId: Long): PremiumWhitelist?
    fun deleteByUserId(userId: Long)

    @Query("""
        SELECT w FROM PremiumWhitelist w
        ORDER BY w.createdAt DESC
    """)
    fun findAllPaged(pageable: Pageable): Page<PremiumWhitelist>
}
