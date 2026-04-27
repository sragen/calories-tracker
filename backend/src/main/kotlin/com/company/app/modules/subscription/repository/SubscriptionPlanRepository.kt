package com.company.app.modules.subscription.repository

import com.company.app.modules.subscription.entity.SubscriptionPlan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionPlanRepository : JpaRepository<SubscriptionPlan, Long> {
    fun findByIsActiveTrue(): List<SubscriptionPlan>
}
