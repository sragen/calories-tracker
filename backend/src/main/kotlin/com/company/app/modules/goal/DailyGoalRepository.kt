package com.company.app.modules.goal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DailyGoalRepository : JpaRepository<DailyGoal, Long> {
    fun findByUserId(userId: Long): DailyGoal?
}
