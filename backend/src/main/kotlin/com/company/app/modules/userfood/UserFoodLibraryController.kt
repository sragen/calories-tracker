package com.company.app.modules.userfood

import com.company.app.common.auth.UserPrincipal
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user-foods")
class UserFoodLibraryController(private val service: UserFoodLibraryService) {

    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "RECENTLY_USED") sort: String,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ) = service.list(principal.id, sort, search, PageRequest.of(page, size.coerceAtMost(50)))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun save(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: SaveUserFoodRequest
    ) = service.save(principal.id, req)

    @PostMapping("/{id}/log")
    fun log(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody req: QuickLogRequest
    ) = service.log(principal.id, id, req)

    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody req: SaveUserFoodRequest
    ) = service.update(principal.id, id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) = service.delete(principal.id, id)
}
