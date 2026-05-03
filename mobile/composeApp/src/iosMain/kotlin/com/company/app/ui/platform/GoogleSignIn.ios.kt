package com.company.app.ui.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Bridge that the iOS host registers with a real GoogleSignIn-iOS SDK adapter.
 *
 * The Swift side calls [registerGoogleSignInBridge] from `iOSApp.init()` once
 * the GoogleSignIn-iOS SDK has been added via SPM and configured (URL scheme +
 * GIDClientID). Until then, calls fall back to a "not configured" error so the
 * UI degrades gracefully without crashing.
 */
interface IosGoogleSignInBridge {
    fun signIn(onResult: (idToken: String?, errorMessage: String?) -> Unit)
}

private var bridge: IosGoogleSignInBridge? = null

fun registerGoogleSignInBridge(b: IosGoogleSignInBridge) {
    bridge = b
}

private class IosGoogleSignInClient : GoogleSignInClient {
    override suspend fun signIn(): Result<String> {
        val b = bridge ?: return Result.failure(
            IllegalStateException("Google Sign-In not configured: see docs/GOOGLE_SIGN_IN_SETUP.md")
        )
        return suspendCancellableCoroutine { cont ->
            b.signIn { idToken, error ->
                if (idToken != null) {
                    cont.resume(Result.success(idToken))
                } else {
                    cont.resume(Result.failure(Exception(error ?: "Google sign-in failed")))
                }
            }
        }
    }
}

@Composable
actual fun rememberGoogleSignInClient(): GoogleSignInClient = IosGoogleSignInClient()
