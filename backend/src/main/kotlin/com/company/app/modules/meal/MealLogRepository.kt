package com.company.app.modules.meal

import com.company.app.common.crud.BaseRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface MealLogRepository : BaseRepository<MealLog, Long> {

    @Query("""
        SELECT m FROM MealLog m
        WHERE m.userId = :userId AND m.loggedAt = :date AND m.deletedAt IS NULL
        ORDER BY m.createdAt ASC
    """)
    fun findByUserIdAndDate(@Param("userId") userId: Long, @Param("date") date: LocalDate): List<MealLog>

    @Query("""
        SELECT COUNT(m) FROM MealLog m
        WHERE m.userId = :userId AND m.loggedAt = :date AND m.deletedAt IS NULL
    """)
    fun countByUserIdAndDate(@Param("userId") userId: Long, @Param("date") date: LocalDate): Long

    @Query("""
        SELECT m FROM MealLog m
        WHERE m.userId = :userId
        AND m.loggedAt BETWEEN :from AND :to
        AND m.deletedAt IS NULL
        ORDER BY m.loggedAt ASC
    """)
    fun findByUserIdAndDateRange(
        @Param("userId") userId: Long,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate
    ): List<MealLog>
}
