package com.example.phoneimage.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StudioLightScheme = lightColorScheme(
    primary = StudioColors.Violet,
    onPrimary = StudioColors.Surface,
    secondary = StudioColors.Cyan,
    tertiary = StudioColors.Pink,
    background = StudioColors.Background,
    onBackground = StudioColors.TextPrimary,
    surface = StudioColors.Surface,
    onSurface = StudioColors.TextPrimary,
    surfaceVariant = StudioColors.SurfaceHigh,
    onSurfaceVariant = StudioColors.TextSecondary,
    outline = StudioColors.Border,
    error = StudioColors.Danger,
)

@Composable
fun PhoneImageTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // 这是一款主打浅色编辑风的产品，始终使用浅色方案。
    val colorScheme = StudioLightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 透明系统栏，内容延伸到状态栏后面；浅底 → 深色系统栏图标
            window.statusBarColor = AndroidColor.TRANSPARENT
            window.navigationBarColor = AndroidColor.TRANSPARENT
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
