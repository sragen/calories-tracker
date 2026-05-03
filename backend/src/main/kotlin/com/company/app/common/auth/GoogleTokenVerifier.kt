package com.company.app.common.auth

import com.company.app.common.exception.AppException
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

data class GoogleUserInfo(
    val sub: String,
    val email: String?,
    val emailVerified: Boolean,
    val name: String?,
    val pictureUrl: String?,
)

/**
 * Verifies Google Sign-In ID tokens server-side.
 *
 * Configure expected `aud` values via `app.auth.google.client-ids` (comma-separated).
 * Typically you list iOS, Android, and Web client IDs from your Google Cloud project —
 * tokens minted for any of them are accepted.
 */
@Service
class GoogleTokenVerifier(
    @Value("\${app.auth.google.client-ids:}") clientIdsCsv: String,
) {
    private val clientIds: List<String> = clientIdsCsv
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    private val verifier: GoogleIdTokenVerifier by lazy {
        if (clientIds.isEmpty()) {
            throw AppException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "GOOGLE_AUTH_NOT_CONFIGURED",
                "Google Sign-In is not configured on this server",
            )
        }
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(clientIds)
            .build()
    }

    fun verify(idToken: String): GoogleUserInfo {
        val token = try {
            verifier.verify(idToken)
        } catch (e: Exception) {
            throw AppException.unauthorized("Invalid Google token")
        } ?: throw AppException.unauthorized("Invalid Google token")

        val payload = token.payload
        val iss = payload.issuer
        if (iss != "https://accounts.google.com" && iss != "accounts.google.com") {
            throw AppException.unauthorized("Invalid Google token issuer")
        }

        return GoogleUserInfo(
            sub = payload.subject,
            email = payload.email,
            emailVerified = payload.emailVerified ?: false,
            name = payload["name"] as? String,
            pictureUrl = payload["picture"] as? String,
        )
    }
}
