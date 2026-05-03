package com.company.app.ui.platform

import androidx.compose.runtime.Composable

/**
 * Platform-specific Google Sign-In wrapper.
 *
 * Returns the Google ID token (JWT) on success, which the backend then verifies.
 * Cancellation by the user surfaces as a Result.failure with a "cancelled" message.
 *
 * Implementations:
 *  - Android: Credential Manager + GetGoogleIdOption
 *  - iOS: GoogleSignIn-iOS SDK (GIDSignIn.sharedInstance.signIn)
 */
interface GoogleSignInClient {
    suspend fun signIn(): Result<String>
}

@Composable
expect fun rememberGoogleSignInClient(): GoogleSignInClient
