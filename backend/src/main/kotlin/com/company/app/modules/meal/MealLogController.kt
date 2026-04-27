package com.company.app.modules.meal

import com.company.app.common.auth.UserPrincipal
import com.company.app.modules.subscription.SubscriptionService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/meal-logs")
class MealLogController(
    private val mealLogService: MealLogService,
    private val subscriptionService: SubscriptionService
) {

    @GetMapping
    fun getDiary(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ) = mealLogService.getDiary(principal.id, date ?: LocalDate.now())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addLog(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: MealLogRequest
    ) = mealLogService.addLog(principal.id, req)

    @PutMapping("/{id}")
    fun updateLog(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
        @RequestBody req: MealLogUpdateRequest
    ) = mealLogService.updateLog(principal.id, id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLog(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) = mealLogService.deleteLog(principal.id, id)

    @GetMapping("/summary")
    fun getSummary(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
    ) = mealLogService.getSummaryRange(principal.id, from, to)

    @GetMapping("/streak")
    fun getStreak(@AuthenticationPrincipal principal: UserPrincipal) =
        mapOf("streak" to mealLogService.getCurrentStreak(principal.id))

    @GetMapping("/export")
    fun export(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
    ): ResponseEntity<ByteArray> {
        if (!subscriptionService.isPremium(principal.id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val csv = mealLogService.exportCsv(principal.id, from, to)
        val headers = HttpHeaders().apply {
            contentType = MediaType.parseMediaType("text/csv")
            contentDisposition = ContentDisposition.attachment()
                .filename("diary-export-$from-to-$to.csv").build()
        }
        return ResponseEntity.ok().headers(headers).body(csv.toByteArray())
    }
}
