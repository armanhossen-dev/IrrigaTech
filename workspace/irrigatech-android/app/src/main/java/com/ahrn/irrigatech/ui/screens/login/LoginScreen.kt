package com.ahrn.irrigatech.ui.screens.login

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahrn.irrigatech.IrrigaTechApp
import com.ahrn.irrigatech.R
import com.ahrn.irrigatech.ui.AppViewModelProvider
import com.ahrn.irrigatech.ui.theme.BrandGreen
import com.ahrn.irrigatech.ui.theme.BrandGreenDark
import com.ahrn.irrigatech.ui.theme.SkyBlue
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.viewmodel.AuthViewModel
import com.ahrn.irrigatech.ui.viewmodel.SignInState
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.first

@Composable
fun LoginScreen(
    onAuthenticated: (hasDevice: Boolean) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val container = (context.applicationContext as IrrigaTechApp).container
    val user by viewModel.user.collectAsStateWithLifecycle()
    val signInState by viewModel.signInState.collectAsStateWithLifecycle()
    val demoMode = FirebaseApp.getApps(context).isEmpty()

    LaunchedEffect(user) {
        if (user != null) {
            val hasDevice = container.deviceStore.activeDevice() != null
            onAuthenticated(hasDevice)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(BrandGreen, BrandGreenDark)),
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                )
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.xxl))
            Image(
                painter = painterResource(R.drawable.img_logo),
                contentDescription = stringResource(R.string.cd_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp)),
            )
            Spacer(Modifier.height(Spacing.lg))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.xl))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.xl))
            Text(
                text = "Monitor soil moisture, control your pumps and save every drop.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.xxl))

            val loading = signInState is SignInState.Loading
            Button(
                onClick = {
                    val activity = context as? Activity ?: return@Button
                    viewModel.signIn(activity)
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        GoogleGlyph()
                        Spacer(Modifier.size(Spacing.md))
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            val error = (signInState as? SignInState.Error)?.message
            if (error != null) {
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }

            if (demoMode) {
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text = "Firebase is not configured yet, so a demo profile will be used. " +
                        "Add google-services.json to enable real Google Sign-In.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Text(
            text = "By continuing you agree to keep your controller credentials private.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
        )
    }
}

@Composable
private fun GoogleGlyph() {
    Text(
        text = "G",
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFF4285F4),
        fontWeight = FontWeight.Bold,
    )
}
