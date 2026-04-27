package com.company.app.shared.data.repository

import com.company.app.shared.data.model.BodyProfileRequest
import com.company.app.shared.data.model.BodyProfileResponse
import com.company.app.shared.data.model.BmrPreviewResponse
import com.company.app.shared.data.network.ApiService

class BodyProfileRepository(private val api: ApiService) {

    suspend fun get(): Result<BodyProfileResponse> = runCatching {
        api.getBodyProfile()
    }

    suspend fun hasProfile(): Result<Boolean> = runCatching {
        api.hasBodyProfile().hasProfile
    }

    suspend fun save(req: BodyProfileRequest): Result<BodyProfileResponse> = runCatching {
        api.saveBodyProfile(req)
    }

    suspend fun previewBmr(req: BodyProfileRequest): Result<BmrPreviewResponse> = runCatching {
        api.previewBmr(req)
    }
}
