package com.company.app.ui.platform

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private const val META_DATA_KEY = "com.company.app.GOOGLE_WEB_CLIENT_ID"

private fun Context.googleWebClientId(): String? {
    val pm = packageManager
    val ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    val raw = ai.metaData?.getString(META_DATA_KEY) ?: return null
    return raw.takeIf { it.isNotBlank() && !it.startsWith("YOUR_") }
}

private class AndroidGoogleSignInClient(private val context: Context) : GoogleSignInClient {
    private val credentialManager = CredentialManager.create(context)

    override suspend fun signIn(): Result<String> {
        val clientId = context.googleWebClientId()
            ?: return Result.failure(IllegalStateException(
                "Google Sign-In not configured: set @string/google_web_client_id"
            ))

        val option = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val response = credentialManager.getCredential(context, request)
            val credential = GoogleIdTokenCredential.createFrom(response.credential.data)
            Result.success(credential.idToken)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("cancelled"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Composable
actual fun rememberGoogleSignInClient(): GoogleSignInClient {
    val context = LocalContext.current
    return remember(context) { AndroidGoogleSignInClient(context) }
}
