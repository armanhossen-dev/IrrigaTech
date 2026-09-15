package com.ahrn.irrigatech.ui.screens.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import com.ahrn.irrigatech.IrrigaTechApp
import com.ahrn.irrigatech.R
import com.ahrn.irrigatech.ui.theme.AquaCyan
import com.ahrn.irrigatech.ui.theme.EmeraldDark
import com.ahrn.irrigatech.ui.theme.EmeraldDeep
import com.ahrn.irrigatech.ui.theme.SlateUltraDark
import com.ahrn.irrigatech.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(onFinished: (signedIn: Boolean, hasDevice: Boolean) -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as IrrigaTechApp).container

    LaunchedEffect(Unit) {
        delay(1500)
        val user = container.sessionStore.user.first()
        val device = container.deviceStore.activeDevice()
        onFinished(user != null, device != null)
    }

    val transition = rememberInfiniteTransition(label = "splashPulse")
    val glowScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowScale",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SlateUltraDark, EmeraldDark, EmeraldDeep, AquaCyan),
                ),
            )
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(glowScale)
                    .background(Color.White.copy(alpha = 0.10f), CircleShape),
            )
            Image(
                painter = painterResource(R.drawable.img_logo),
                contentDescription = stringResource(R.string.cd_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(28.dp)),
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
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
        Spacer(Modifier.height(Spacing.xxl))
        CircularProgressIndicator(
            color = Color.White.copy(alpha = 0.9f),
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.height(Spacing.xxl))
        Text(
            text = "Smart irrigation companion",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.padding(horizontal = Spacing.xl),
            textAlign = TextAlign.Center,
        )
    }
}