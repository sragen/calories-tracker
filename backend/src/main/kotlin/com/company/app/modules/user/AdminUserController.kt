package com.company.app.modules.user

import com.company.app.common.auth.UserResponse
import com.company.app.common.rbac.RequiresPermission
import com.company.app.common.rbac.RequiresPermission.Action
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - Users")
@SecurityRequirement(name = "bearerAuth")
class AdminUserController(private val service: AdminUserService) {

    @GetMapping
    @RequiresPermission(module = "USERS", action = Action.READ)
    fun list(pageable: Pageable): Page<UserResponse> = service.findAll(pageable)

    @GetMapping("/{id}")
    @RequiresPermission(module = "USERS", action = Action.READ)
    fun getById(@PathVariable id: Long): UserResponse = service.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresPermission(module = "USERS", action = Action.WRITE)
    fun create(@Valid @RequestBody req: CreateStaffRequest): UserResponse = service.createStaff(req)

    @PutMapping("/{id}")
    @RequiresPermission(module = "USERS", action = Action.WRITE)
    fun update(@PathVariable id: Long, @Valid @RequestBody req: UpdateUserRequest): UserResponse =
        service.update(id, req)

    @PatchMapping("/{id}/status")
    @RequiresPermission(module = "USERS", action = Action.WRITE)
    fun updateStatus(@PathVariable id: Long, @Valid @RequestBody req: UpdateStatusRequest): UserResponse =
        service.updateStatus(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequiresPermission(module = "USERS", action = Action.DELETE)
    fun delete(@PathVariable id: Long) = service.delete(id)
}
