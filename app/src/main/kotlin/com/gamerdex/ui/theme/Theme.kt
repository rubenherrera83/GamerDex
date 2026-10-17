package com.gamerdex.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

val CyberCyan: Color @Composable get() = MaterialTheme.colorScheme.primary
val AcidGreen: Color @Composable get() = MaterialTheme.colorScheme.tertiary
val NeonPurple: Color @Composable get() = MaterialTheme.colorScheme.secondary
val VaultBlack: Color @Composable get() = MaterialTheme.colorScheme.background
val VaultSurface: Color @Composable get() = MaterialTheme.colorScheme.surface

@Composable fun GamerDexTheme(theme: VisualTheme = VisualTheme.CYBERPUNK, content: @Composable () -> Unit) {
    val base = Typography()
    val p = theme.palette()
    val scheme = if (theme.dark) darkColorScheme(primary = p.primary, secondary = p.secondary, tertiary = p.tertiary,
        background = p.background, surface = p.surface, surfaceVariant = p.raised, onPrimary = p.background,
        primaryContainer = p.raised, onPrimaryContainer = p.primary, secondaryContainer = p.raised, onSecondaryContainer = p.secondary,
        onBackground = p.text, onSurface = p.text, onSurfaceVariant = Color(0xFF9DADB8), outline = Color(0xFF344854))
    else lightColorScheme(primary = p.primary, secondary = p.secondary, tertiary = p.tertiary,
        background = p.background, surface = p.surface, surfaceVariant = p.raised, onPrimary = Color.White,
        primaryContainer = p.raised, onPrimaryContainer = p.primary, secondaryContainer = p.raised, onSecondaryContainer = p.secondary,
        onBackground = p.text, onSurface = p.text, onSurfaceVariant = if (theme == VisualTheme.POP) Color(0xFF52647A) else Color(0xFF705C72), outline = if (theme == VisualTheme.POP) Color(0xFF9DAFC3) else Color(0xFFBDA4B9))
    MaterialTheme(
        colorScheme = scheme,
        shapes = if (theme == VisualTheme.ARCADE || theme == VisualTheme.VOXEL) Shapes(small = androidx.compose.foundation.shape.RoundedCornerShape(0), medium = androidx.compose.foundation.shape.RoundedCornerShape(0))
        else if (theme == VisualTheme.KAWAII || theme == VisualTheme.POP) Shapes(small = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.Dp(14f)),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.Dp(24f))) else Shapes(small = androidx.compose.foundation.shape.CutCornerShape(topEnd = androidx.compose.ui.unit.Dp(10f)),
            medium = androidx.compose.foundation.shape.CutCornerShape(topEnd = androidx.compose.ui.unit.Dp(18f), bottomStart = androidx.compose.ui.unit.Dp(8f))),
        typography = Typography(titleLarge = base.titleLarge.copy(fontFamily = if (theme == VisualTheme.KAWAII || theme == VisualTheme.POP || theme == VisualTheme.ROYALE) FontFamily.SansSerif else FontFamily.Monospace),
            headlineLarge = base.headlineLarge.copy(fontFamily = if (theme == VisualTheme.KAWAII || theme == VisualTheme.POP || theme == VisualTheme.ROYALE) FontFamily.SansSerif else FontFamily.Monospace),
            labelSmall = base.labelSmall.copy(fontFamily = FontFamily.Monospace)), content = { CompositionLocalProvider(LocalVisualTheme provides theme) { ThemedBackdrop(content) } },
    )
}
