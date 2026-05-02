package com.company.app.shared.data.repository

import com.company.app.shared.data.model.EntitlementResponse
import com.company.app.shared.data.model.SubscriptionPlanResponse
import com.company.app.shared.data.network.ApiService

class SubscriptionRepository(private val api: ApiService) {

    suspend fun getEntitlement(): Result<EntitlementResponse> =
        runCatching { api.getEntitlement() }

    suspend fun getPlans(): Result<List<SubscriptionPlanResponse>> =
        runCatching { api.getSubscriptionPlans() }
}
