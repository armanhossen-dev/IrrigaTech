package com.ahrn.irrigatech

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahrn.irrigatech.data.local.AppSettings
import com.ahrn.irrigatech.ui.navigation.AppNavHost
import com.ahrn.irrigatech.ui.theme.AccentGreenBlue
import com.ahrn.irrigatech.ui.theme.AccentSwatches
import com.ahrn.irrigatech.ui.theme.IrrigaTechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { IrrigaTechRoot() }
    }
}

@Composable
private fun IrrigaTechRoot() {
    val context = LocalContext.current
    val container = (context.applicationContext as IrrigaTechApp).container
    val settings by container.settingsStore.settings.collectAsStateWithLifecycle(
        initialValue = AppSettings(),
    )
    val accent = AccentSwatches.firstOrNull { it.id == settings.accentId } ?: AccentGreenBlue

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {},
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    IrrigaTechTheme(themeMode = settings.themeMode, accent = accent) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AppNavHost()
        }
    }
}
