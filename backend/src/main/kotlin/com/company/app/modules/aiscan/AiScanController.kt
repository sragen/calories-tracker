package com.company.app.modules.aiscan

import com.company.app.common.auth.UserPrincipal
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/ai-scan")
class AiScanController(
    private val aiScanService: AiScanService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/analyze", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun analyze(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam("image") image: MultipartFile
    ): AiScanResponse {
        val response = aiScanService.analyze(principal.id, image)
        log.info("ai-scan response (user={}): {}", principal.id, objectMapper.writeValueAsString(response))
        return response
    }

    @PostMapping("/confirm")
    fun confirm(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: AiScanConfirmRequest
    ) = mapOf("logged" to aiScanService.confirm(principal.id, request))
}
