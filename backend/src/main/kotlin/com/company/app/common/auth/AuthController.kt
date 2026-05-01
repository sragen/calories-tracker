package com.company.app.common.auth

import com.company.app.common.exception.AppException
import com.company.app.modules.user.User
import com.company.app.modules.user.UserRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody req: RegisterRequest): AuthResponse {
        if (userRepository.findByEmailAndDeletedAtIsNull(req.email) != null)
            throw AppException.conflict("Email already registered")

        val user = userRepository.save(
            User(email = req.email, phone = req.phone,
                 password = passwordEncoder.encode(req.password), name = req.name)
        )
        return buildAuthResponse(user)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): AuthResponse {
        val user = userRepository.findByEmailAndDeletedAtIsNull(req.email)
            ?: throw AppException.unauthorized("Invalid email or password")

        if (user.status != "ACTIVE")
            throw AppException.forbidden("Account is ${user.status.lowercase()}")

        if (!passwordEncoder.matches(req.password, user.password))
            throw AppException.unauthorized("Invalid email or password")

        user.lastLogin = LocalDateTime.now()
        userRepository.save(user)

        return buildAuthResponse(user)
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshRequest): AuthResponse {
        val tokenEntity = refreshTokenRepository.findByToken(req.refreshToken)
            ?: throw AppException.unauthorized("Invalid refresh token")

        if (!tokenEntity.isValid)
            throw AppException.unauthorized("Refresh token expired or revoked")

        val user = userRepository.findActiveById(tokenEntity.userId)
            ?: throw AppException.unauthorized("User not found")

        refreshTokenRepository.revokeByToken(req.refreshToken)
        return buildAuthResponse(user)
    }

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: UserPrincipal): UserResponse =
        userRepository.findActiveById(principal.id)?.toResponse()
            ?: throw AppException.notFound("User not found")

    @PutMapping("/me")
    fun updateMe(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody req: UpdateProfileRequest
    ): UserResponse {
        val user = userRepository.findActiveById(principal.id)
            ?: throw AppException.notFound("User not found")

        req.name?.let { user.name = it }
        req.phone?.let { user.phone = it }
        req.avatarUrl?.let { user.avatarUrl = it }
        req.fcmToken?.let { user.fcmToken = it }
        user.updatedAt = LocalDateTime.now()

        return userRepository.save(user).toResponse()
    }

    private fun buildAuthResponse(user: User): AuthResponse {
        val refreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.save(
            RefreshToken(userId = user.id, token = refreshToken,
                         expiresAt = LocalDateTime.now().plusDays(30))
        )
        return AuthResponse(
            accessToken = jwtService.generateAccessToken(user.id, user.email ?: "", user.role),
            refreshToken = refreshToken
        )
    }
}
