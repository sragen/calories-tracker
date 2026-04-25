package com.company.app.common.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(
    private val jwtService: JwtService,
    private val userDetailsService: AppUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val token = request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)

        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            jwtService.getUserIdFromToken(token)
                ?.let { userDetailsService.loadById(it) }
                ?.let { principal ->
                    val auth = UsernamePasswordAuthenticationToken(
                        principal, null, principal.authorities
                    ).also { it.details = WebAuthenticationDetailsSource().buildDetails(request) }
                    SecurityContextHolder.getContext().authentication = auth
                }
        }
        chain.doFilter(request, response)
    }
}
