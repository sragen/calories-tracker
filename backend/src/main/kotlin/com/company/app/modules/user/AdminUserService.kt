package com.company.app.modules.user

import com.company.app.common.auth.UserResponse
import com.company.app.common.exception.AppException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminUserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val allowedRoles = setOf("USER", "STAFF", "ADMIN", "SUPER_ADMIN")
    private val allowedStatuses = setOf("ACTIVE", "INACTIVE", "SUSPENDED")

    fun findAll(pageable: Pageable, q: String?): Page<UserResponse> =
        if (!q.isNullOrBlank()) userRepository.searchActive(q.trim(), pageable).map { it.toResponse() }
        else userRepository.findAllActive(pageable).map { it.toResponse() }

    fun findById(id: Long): UserResponse =
        userRepository.findActiveById(id)?.toResponse()
            ?: throw AppException.notFound("User not found")

    @Transactional
    fun createStaff(req: CreateStaffRequest): UserResponse {
        if (req.role !in allowedRoles) throw AppException.badRequest("Invalid role: ${req.role}")
        if (req.role == "USER") throw AppException.badRequest("Use register endpoint for end users")

        if (userRepository.findByEmailAndDeletedAtIsNull(req.email) != null)
            throw AppException.conflict("Email already in use")

        if (req.phone != null && userRepository.findByPhoneAndDeletedAtIsNull(req.phone) != null)
            throw AppException.conflict("Phone already in use")

        val user = User(
            email = req.email,
            password = passwordEncoder.encode(req.password),
            name = req.name,
            phone = req.phone,
            role = req.role,
            status = "ACTIVE"
        )
        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun update(id: Long, req: UpdateUserRequest): UserResponse {
        val user = userRepository.findActiveById(id) ?: throw AppException.notFound("User not found")

        if (req.role != null) {
            if (req.role !in allowedRoles) throw AppException.badRequest("Invalid role: ${req.role}")
            user.role = req.role
        }
        req.name?.let { user.name = it }
        req.phone?.let { user.phone = it }
        req.avatarUrl?.let { user.avatarUrl = it }
        user.updatedAt = LocalDateTime.now()

        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun updateStatus(id: Long, req: UpdateStatusRequest): UserResponse {
        if (req.status !in allowedStatuses) throw AppException.badRequest("Invalid status: ${req.status}")
        val user = userRepository.findActiveById(id) ?: throw AppException.notFound("User not found")
        user.status = req.status
        user.updatedAt = LocalDateTime.now()
        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val user = userRepository.findActiveById(id) ?: throw AppException.notFound("User not found")
        user.deletedAt = LocalDateTime.now()
        userRepository.save(user)
    }
}
