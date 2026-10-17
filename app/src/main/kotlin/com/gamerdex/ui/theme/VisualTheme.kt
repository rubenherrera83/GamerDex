package com.gamerdex.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf

enum class VisualTheme(val title: String, val tagline: String, val dark: Boolean = true) {
    CYBERPUNK("Cyberpunk", "Noche eléctrica. Sin límites."),
    COMPETITIVE("Competitivo", "Precisión. Rango. Victoria."),
    RACING("Racing", "Tu colección, a máxima velocidad."),
    KAWAII("Kawaii", "Tu pequeño universo de juegos.", false),
    POP("Salto Pop", "Color, aventura y un salto más.", false),
    ARCADE("Órbita Pixel", "Una partida más entre las estrellas."),
    VOXEL("Mundo Vóxel", "Construye tu próxima aventura."),
    ROYALE("Zona Royale", "Tu escuadrón. Tu estilo. Tu victoria."),
    TACTICAL("Operación Táctica", "Cada pieza cuenta. Completa la misión."),
}
val LocalVisualTheme = staticCompositionLocalOf { VisualTheme.CYBERPUNK }
fun VisualTheme.heroTitle() = when (this) {
    VisualTheme.CYBERPUNK -> "TU COLECCIÓN.\nTUS REGLAS."
    VisualTheme.COMPETITIVE -> "SUBE DE NIVEL.\nMARCA TU RANGO."
    VisualTheme.RACING -> "TU GARAJE.\nSIN LÍMITES."
    VisualTheme.KAWAII -> "TU UNIVERSO.\nLLENO DE JUEGOS."
    VisualTheme.POP -> "DA EL SALTO.\nSIGUE JUGANDO."
    VisualTheme.ARCADE -> "INSERTA SUEÑOS.\nSUPERA TU RÉCORD."
    VisualTheme.VOXEL -> "BLOQUE A BLOQUE.\nCREA TU MUNDO."
    VisualTheme.ROYALE -> "TU ESCUADRÓN.\nTU VICTORIA."
    VisualTheme.TACTICAL -> "OBJETIVO:\nTU COLECCIÓN."
}
data class ThemePalette(val background: Color, val surface: Color, val raised: Color, val primary: Color, val secondary: Color, val tertiary: Color, val text: Color)
fun VisualTheme.palette() = when (this) {
    VisualTheme.CYBERPUNK -> ThemePalette(Color(0xFF080D13), Color(0xFF111C26), Color(0xFF1B2C38), Color(0xFFF5EF26), Color(0xFF00E6EF), Color(0xFFFF527D), Color(0xFFE9F1F4))
    VisualTheme.COMPETITIVE -> ThemePalette(Color(0xFF090D0C), Color(0xFF131D19), Color(0xFF20342A), Color(0xFFB2FF45), Color(0xFF81D6FF), Color(0xFFFFBD59), Color(0xFFF0F6F1))
    VisualTheme.RACING -> ThemePalette(Color(0xFF100C0D), Color(0xFF21171B), Color(0xFF38232B), Color(0xFFFF793B), Color(0xFFFFD449), Color(0xFFFF4D65), Color(0xFFFFF0E8))
    VisualTheme.KAWAII -> ThemePalette(Color(0xFFFFF6FB), Color(0xFFFFFFFF), Color(0xFFF6E6F3), Color(0xFFAD2E73), Color(0xFF7253AD), Color(0xFF3C7B70), Color(0xFF38253A))
    VisualTheme.POP -> ThemePalette(Color(0xFFFFF9ED), Color(0xFFFFFFFF), Color(0xFFE7F1FC), Color(0xFFC63238), Color(0xFF1565C0), Color(0xFF287C49), Color(0xFF192C45))
    VisualTheme.ARCADE -> ThemePalette(Color(0xFF060D18), Color(0xFF101D30), Color(0xFF182B45), Color(0xFFB9F36B), Color(0xFF69E2FF), Color(0xFFBF9EFF), Color(0xFFEFF8FF))
    VisualTheme.VOXEL -> ThemePalette(Color(0xFF101A14), Color(0xFF1A2B21), Color(0xFF2C4030), Color(0xFFA6D56A), Color(0xFFE1B887), Color(0xFFE8D16B), Color(0xFFF0F5E9))
    VisualTheme.ROYALE -> ThemePalette(Color(0xFF100D29), Color(0xFF211A43), Color(0xFF35285C), Color(0xFFC1A0FF), Color(0xFF68E6FF), Color(0xFFFFDF70), Color(0xFFF6F0FF))
    VisualTheme.TACTICAL -> ThemePalette(Color(0xFF131612), Color(0xFF20271E), Color(0xFF323B2B), Color(0xFFD1D8A2), Color(0xFFDFC596), Color(0xFFA9C2B6), Color(0xFFF0F1E6))
}
