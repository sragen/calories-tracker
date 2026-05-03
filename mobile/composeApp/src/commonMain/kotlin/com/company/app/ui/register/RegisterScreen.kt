package com.company.app.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.components.CalSnapTextButton
import com.company.app.ui.login.AuthField
import com.company.app.ui.login.GoogleSignInButton
import com.company.app.ui.login.OrDivider
import com.company.app.ui.platform.rememberGoogleSignInClient
import com.company.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: (isNewUser: Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val googleClient = rememberGoogleSignInClient()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onRegisterSuccess(state.isNewUser)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Back button row
            Row(
                modifier = Modifier
                    .padding(horizontal = CalSnapSpacing.screenPad)
                    .padding(top = CalSnapSpacing.lg),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CalSnapColors.SurfaceAlt)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CalSnapIcon(name = "chev-l", size = 18.dp, color = CalSnapColors.Ink)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CalSnapSpacing.screenPad)
                    .padding(top = CalSnapSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
            ) {
                Text(
                    text = "Create account",
                    style = CalSnapType.HeadlineLarge,
                    color = CalSnapColors.Ink,
                )

                Text(
                    text = "Start tracking your nutrition today",
                    style = CalSnapType.Body,
                    color = CalSnapColors.Muted,
                )

                Spacer(Modifier.height(CalSnapSpacing.md))

                AuthField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Full name",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )

                AuthField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )

                AuthField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )

                AuthField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )

                state.error?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CalSnapRadius.md))
                            .background(CalSnapColors.RedSoft)
                            .padding(CalSnapSpacing.md),
                    ) {
                        Text(it, style = CalSnapType.BodySmall, color = CalSnapColors.Red)
                    }
                }

                Spacer(Modifier.height(CalSnapSpacing.sm))

                if (state.isLoading) {
                    CircularProgressIndicator(color = CalSnapColors.Red, modifier = Modifier.size(28.dp))
                } else {
                    CalSnapPrimaryButton(
                        text = "Create Account",
                        onClick = { viewModel.register(name, email, password, confirmPassword) },
                        enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                    )
                }

                CalSnapTextButton(
                    text = "Already have an account? Sign in",
                    onClick = onBack,
                )

                Spacer(Modifier.height(CalSnapSpacing.sm))
                OrDivider()
                Spacer(Modifier.height(CalSnapSpacing.sm))

                GoogleSignInButton(
                    enabled = !state.isLoading,
                    onClick = {
                        scope.launch {
                            googleClient.signIn().fold(
                                onSuccess = { idToken -> viewModel.googleSignIn(idToken) },
                                onFailure = { e ->
                                    val msg = e.message ?: "Google sign-in failed"
                                    if (!msg.contains("cancelled", ignoreCase = true)) {
                                        viewModel.showError(msg)
                                    }
                                },
                            )
                        }
                    },
                )

                Spacer(Modifier.height(CalSnapSpacing.xl))
            }
        }
    }
}
