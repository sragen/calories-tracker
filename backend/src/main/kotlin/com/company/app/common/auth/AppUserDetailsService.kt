package com.company.app.common.auth

import com.company.app.modules.user.UserRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AppUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserPrincipal =
        userRepository.findByEmailAndDeletedAtIsNull(email)
            ?.let { UserPrincipal(it.id, it.email ?: "", it.role, it.password ?: "") }
            ?: throw UsernameNotFoundException("User not found: $email")

    fun loadById(id: Long): UserPrincipal? =
        userRepository.findActiveById(id)
            ?.let { UserPrincipal(it.id, it.email ?: "", it.role, it.password ?: "") }
}
