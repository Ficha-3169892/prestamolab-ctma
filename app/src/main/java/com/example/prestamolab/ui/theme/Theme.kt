package com.example.prestamolab.ui.theme
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

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = WhiteText,

    secondary = BlueLight,
    onSecondary = WhiteText,

    tertiary = BlueDark,
    onTertiary = WhiteText,

    background = BlackBackground,
    onBackground = WhiteText,

    surface = DarkSurface,
    onSurface = WhiteText,

    error = ErrorRed,
    onError = WhiteText
)

private val LightColorScheme = lightColorScheme(
    primary = BlueDark,
    onPrimary = WhiteText,

    secondary = BluePrimary,
    onSecondary = WhiteText,

    tertiary = BlueLight,
    onTertiary = BlackBackground,

    background = Color.White,
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,

    error = ErrorRed,
    onError = WhiteText
)

@Composable
fun PrestamoLabTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,

    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
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
