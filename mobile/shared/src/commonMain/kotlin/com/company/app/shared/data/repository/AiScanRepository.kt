package com.company.app.shared.data.repository

import com.company.app.shared.data.model.AiDetectedFood
import com.company.app.shared.data.model.AiScanConfirmRequest
import com.company.app.shared.data.model.AiScanResponse
import com.company.app.shared.data.network.ApiService

class AiScanRepository(private val api: ApiService) {

    suspend fun analyze(imageBytes: ByteArray, mimeType: String = "image/jpeg"): Result<AiScanResponse> =
        runCatching { api.analyzeAiScan(imageBytes, mimeType) }

    suspend fun confirm(
        scanLogId: Long,
        selectedFoods: List<AiDetectedFood>,
        mealType: String,
        loggedAt: String
    ): Result<Int> = runCatching {
        val res = api.confirmAiScan(
            AiScanConfirmRequest(
                scanLogId = scanLogId,
                selectedFoods = selectedFoods,
                mealType = mealType,
                loggedAt = loggedAt
            )
        )
        res["logged"] ?: 0
    }
}
