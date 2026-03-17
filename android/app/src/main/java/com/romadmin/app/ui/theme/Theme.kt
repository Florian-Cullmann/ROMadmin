package com.romadmin.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF9B72CF),
    secondary = androidx.compose.ui.graphics.Color(0xFF6C63FF),
    tertiary = androidx.compose.ui.graphics.Color(0xFF4FC3F7),
    surface = androidx.compose.ui.graphics.Color(0xFF1A1B1E),
    background = androidx.compose.ui.graphics.Color(0xFF111113),
)

@Composable
fun RomAdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> DarkColorScheme
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}
