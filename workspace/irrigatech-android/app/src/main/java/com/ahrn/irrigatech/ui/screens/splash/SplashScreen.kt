package com.ahrn.irrigatech.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ahrn.irrigatech.IrrigaTechApp
import com.ahrn.irrigatech.R
import com.ahrn.irrigatech.ui.theme.BrandGreen
import com.ahrn.irrigatech.ui.theme.BrandGreenDark
import com.ahrn.irrigatech.ui.theme.SkyBlue
import com.ahrn.irrigatech.ui.theme.Spacing
import androidx.compose.ui.platform.LocalContext
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrandGreen, BrandGreenDark, SkyBlue),
                ),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.img_logo),
            contentDescription = stringResource(R.string.cd_logo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(28.dp)),
        )
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
        CircularProgressIndicator(color = Color.White.copy(alpha = 0.9f))
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
