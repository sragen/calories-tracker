package com.company.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.components.CalSnapTextButton
import com.company.app.ui.platform.rememberGoogleSignInClient
import com.company.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (isNewUser: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val googleClient = rememberGoogleSignInClient()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess(state.isNewUser)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CalSnapSpacing.screenPad),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
        ) {
            Text(
                text = "CALSNAP",
                style = CalSnapType.Label.copy(letterSpacing = 2.0.dp.value.let {
                    androidx.compose.ui.unit.TextUnit(it, androidx.compose.ui.unit.TextUnitType.Sp)
                }),
                color = CalSnapColors.Muted,
            )

            Spacer(Modifier.height(CalSnapSpacing.xs))

            Text(
                text = "Welcome back",
                style = CalSnapType.HeadlineLarge,
                color = CalSnapColors.Ink,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Sign in to continue tracking",
                style = CalSnapType.Body,
                color = CalSnapColors.Muted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(CalSnapSpacing.lg))

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
                    text = "Sign In",
                    onClick = { viewModel.login(email, password) },
                    enabled = email.isNotBlank() && password.isNotBlank(),
                )
            }

            CalSnapTextButton(
                text = "Don't have an account? Create one",
                onClick = onNavigateToRegister,
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
        }
    }
}

@Composable
internal fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(CalSnapColors.Divider))
        Text(
            text = "OR",
            style = CalSnapType.Label,
            color = CalSnapColors.Muted,
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(CalSnapColors.Divider))
    }
}

@Composable
internal fun GoogleSignInButton(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(CalSnapRadius.pill))
            .background(CalSnapColors.Background)
            .border(
                width = 1.dp,
                color = CalSnapColors.Border,
                shape = RoundedCornerShape(CalSnapRadius.pill),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        GoogleGlyph()
        Text(
            text = "Continue with Google",
            color = CalSnapColors.Ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

@Composable
private fun GoogleGlyph() {
    // Google "G" wordmark approximated via colored 'G' on a 18dp circle.
    // Real assets should be replaced via Google brand guidelines once available.
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(50)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "G",
            color = Color(0xFF4285F4),
            fontSize = 18.sp,
            fontWeight = FontWeight.W900,
        )
    }
}

@Composable
internal fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = CalSnapType.Body, color = CalSnapColors.Hint) },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        textStyle = CalSnapType.Body.copy(color = CalSnapColors.Ink),
        shape = RoundedCornerShape(CalSnapRadius.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CalSnapColors.Ink,
            unfocusedBorderColor = CalSnapColors.Border,
            focusedContainerColor = CalSnapColors.Background,
            unfocusedContainerColor = CalSnapColors.SurfaceAlt,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
