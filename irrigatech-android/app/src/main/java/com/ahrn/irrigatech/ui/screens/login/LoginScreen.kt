package com.ahrn.irrigatech.ui.screens.login

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.statusBarsPadding
import com.ahrn.irrigatech.IrrigaTechApp
import com.ahrn.irrigatech.R
import com.ahrn.irrigatech.data.model.DeviceConfig
import com.ahrn.irrigatech.ui.AppViewModelProvider
import com.ahrn.irrigatech.ui.theme.Radii
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.viewmodel.AuthViewModel
import com.ahrn.irrigatech.ui.viewmodel.SignInState
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
        val currentUser = user
        if (currentUser != null) {
            if (!demoMode) {
                runCatching {
                    val db = FirebaseFirestore.getInstance()
                    val doc = db.collection("users").document(currentUser.uid).get().await()
                    if (doc.exists()) {
                        val token = doc.getString("blynkAuthToken")
                        val deviceName = doc.getString("deviceName")
                        if (!token.isNullOrBlank() && !deviceName.isNullOrBlank()) {
                            val device = DeviceConfig(
                                id = currentUser.uid,
                                name = deviceName,
                            )
                            container.tokenStore.saveToken(device.id, token)
                            container.deviceStore.upsert(device)
                            container.deviceStore.setActive(device.id)
                        }
                    }
                }
            }
            val hasDevice = container.deviceStore.activeDevice() != null
            onAuthenticated(hasDevice)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_logo),
                    contentDescription = stringResource(R.string.cd_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.xs))

            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.xxl))

            Text(
                text = "Monitor soil moisture, control your pumps\nAnd save every drop.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.xxl))

            val loading = signInState is SignInState.Loading

            OutlinedButton(
                onClick = {
                    val activity = context as? Activity ?: return@OutlinedButton
                    viewModel.signIn(activity)
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(Radii.button),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            val error = (signInState as? SignInState.Error)?.message
            if (error != null) {
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = error,
                    color = StatusColors.alert,
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
            } else {
                Spacer(Modifier.height(Spacing.lg))
                TextButton(
                    onClick = { viewModel.signInDemo() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Sign in as Demo User",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
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

/**
 * Approximates the official Google "G" mark — four colored arcs plus the
 * blue crossbar — drawn on a Canvas so the button doesn't depend on a
 * bundled vector asset.
 */
@Composable
private fun GoogleGlyph() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = size.minDimension * 0.22f
        val radius = (size.minDimension - strokeWidth) / 2f
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2f, radius * 2f)

        // Red — top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 245f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth),
        )

        // Yellow — lower-left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 155f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth),
        )

        // Green — bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 65f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth),
        )

        // Blue — right arc, wider to leave room for the crossbar
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -25f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth),
        )

        // Blue crossbar — the flat stroke that turns the ring into a "G"
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(center.x - strokeWidth * 0.15f, center.y - strokeWidth / 2f),
            size = Size(radius + strokeWidth * 0.65f, strokeWidth),
        )
    }
}