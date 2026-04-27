package com.company.app.modules.aiscan

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface AiScanLogRepository : JpaRepository<AiScanLog, Long> {

    @Query("""
        SELECT COUNT(l) FROM AiScanLog l
        WHERE l.userId = :userId AND l.createdAt >= :since
    """)
    fun countByUserIdSince(@Param("userId") userId: Long, @Param("since") since: LocalDateTime): Long
}
