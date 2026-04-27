package com.company.app.shared.data.repository

import com.company.app.shared.data.model.PremiumStatusResponse
import com.company.app.shared.data.model.SnapTokenResponse
import com.company.app.shared.data.model.SubscriptionPlan
import com.company.app.shared.data.network.ApiService

class SubscriptionRepository(private val api: ApiService) {

    suspend fun getPlans(): Result<List<SubscriptionPlan>> =
        runCatching { api.getSubscriptionPlans() }

    suspend fun getStatus(): Result<PremiumStatusResponse> =
        runCatching { api.getPremiumStatus() }

    suspend fun purchase(planId: Long): Result<SnapTokenResponse> =
        runCatching { api.purchaseSubscription(planId) }
}
