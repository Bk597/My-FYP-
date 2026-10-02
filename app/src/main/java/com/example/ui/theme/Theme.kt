package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PhanderDarkPrimary,
    onPrimary = PhanderDarkOnPrimary,
    primaryContainer = PhanderDarkPrimaryContainer,
    onPrimaryContainer = PhanderDarkOnPrimaryContainer,
    secondary = PhanderDarkSecondary,
    onSecondary = PhanderDarkOnSecondary,
    secondaryContainer = PhanderDarkSecondaryContainer,
    onSecondaryContainer = PhanderDarkOnSecondaryContainer,
    tertiary = PhanderDarkTertiary,
    onTertiary = PhanderDarkOnTertiary,
    tertiaryContainer = PhanderDarkTertiaryContainer,
    onTertiaryContainer = PhanderDarkOnTertiaryContainer,
    background = PhanderDarkBackground,
    onBackground = PhanderDarkOnBackground,
    surface = PhanderDarkSurface,
    onSurface = PhanderDarkOnSurface,
    surfaceVariant = PhanderDarkSurfaceVariant,
    onSurfaceVariant = PhanderDarkOnSurfaceVariant,
    outline = PhanderDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PhanderTealPrimary,
    onPrimary = PhanderTealOnPrimary,
    primaryContainer = PhanderTealContainer,
    onPrimaryContainer = PhanderTealOnContainer,
    secondary = PhanderGoldSecondary,
    onSecondary = PhanderGoldOnSecondary,
    secondaryContainer = PhanderGoldContainer,
    onSecondaryContainer = PhanderGoldOnContainer,
    tertiary = PhanderEmeraldTertiary,
    onTertiary = PhanderEmeraldOnTertiary,
    tertiaryContainer = PhanderEmeraldContainer,
    onTertiaryContainer = PhanderEmeraldOnContainer,
    background = PhanderLightBackground,
    onBackground = PhanderLightOnBackground,
    surface = PhanderLightSurface,
    onSurface = PhanderLightOnSurface,
    surfaceVariant = PhanderLightSurfaceVariant,
    onSurfaceVariant = PhanderLightOnSurfaceVariant,
    outline = PhanderLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false so Phander's signature emerald & gold branding is always showcased beautifully
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
