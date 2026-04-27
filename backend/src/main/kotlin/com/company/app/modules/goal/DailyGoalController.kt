package com.company.app.modules.goal

import com.company.app.common.auth.UserPrincipal
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/goals/daily")
class DailyGoalController(private val dailyGoalService: DailyGoalService) {

    @GetMapping
    fun get(@AuthenticationPrincipal principal: UserPrincipal) =
        dailyGoalService.getByUserId(principal.id)

    @PutMapping
    fun update(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: DailyGoalRequest
    ) = dailyGoalService.update(principal.id, req)

    @PostMapping("/reset")
    fun reset(@AuthenticationPrincipal principal: UserPrincipal) =
        dailyGoalService.reset(principal.id)
}
