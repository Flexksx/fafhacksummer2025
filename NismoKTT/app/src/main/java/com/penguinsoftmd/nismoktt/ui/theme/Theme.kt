package com.penguinsoftmd.nismoktt.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// New color definitions based on the provided scheme:
val OrangeTone = Color(0xFFFF6B35)  // FF6B35
val SoftYellow = Color(0xFFF7C59F)   // F7C59F
val Cream = Color(0xFFEFEFD0)        // EFEFD0
val Navy = Color(0xFF004E89)         // 004E89
val DarkBlue = Color(0xFF1A659E)     // 1A659E

// Updated Dark Color Scheme: Only overriding colors explicitly specified.
private val DarkColorScheme = darkColorScheme(
    primary = OrangeTone,
    secondary = SoftYellow,
    tertiary = DarkBlue,
    background = Navy
    // surface, onPrimary, etc. are auto-generated.
)

// Updated Light Color Scheme: Only overriding colors explicitly specified.
private val LightColorScheme = lightColorScheme(
    primary = OrangeTone,
    secondary = SoftYellow,
    tertiary = DarkBlue,
    background = Cream
    // All other colors are generated automatically.
)

@Composable
fun NismoKTTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false to always use your custom palette.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}