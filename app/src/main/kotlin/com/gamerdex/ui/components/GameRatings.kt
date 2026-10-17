package com.gamerdex.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.CommunityRating
import com.gamerdex.core.domain.formatPersonalRating
import com.gamerdex.ui.theme.LocalVisualTheme
import java.text.NumberFormat

fun ratingColor(score: Float, dark: Boolean): Color = when {
    score < 50f -> lerp(if (dark) Color(0xFFFF5869) else Color(0xFF9F1933), if (dark) Color(0xFFFF8A75) else Color(0xFFBF333A), (score / 50f).coerceIn(0f, 1f))
    score < 75f -> lerp(if (dark) Color(0xFFFFBD44) else Color(0xFF8C5900), if (dark) Color(0xFFFFE77A) else Color(0xFF806600), (score - 50f) / 25f)
    else -> lerp(if (dark) Color(0xFF82CC6C) else Color(0xFF2B732B), if (dark) Color(0xFF3FF2A0) else Color(0xFF00764E), ((score - 75f) / 25f).coerceIn(0f, 1f))
}

@Composable fun GameRatings(personal: Float?, community: CommunityRating?, linked: Boolean, loading: Boolean, failed: Boolean, retry: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RatingPanel("Tu nota", personal, if (personal == null) "Sin valorar" else when { personal < 50 -> "BAJA"; personal < 75 -> "MEDIA"; else -> "ALTA" }, Modifier.weight(1f))
        RatingPanel("Comunidad RAWG", community?.score, when {
            !linked -> "Vincula con RAWG"
            loading -> "Consultando…"
            failed -> "No disponible"
            community == null -> "Sin media disponible"
            else -> "${NumberFormat.getIntegerInstance().format(community.votes)} votos"
        }, Modifier.weight(1f))
    }
    if (community != null) Text("Media RAWG convertida a escala 0–100.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (failed && linked) TextButton(onClick = retry) { Text("Reintentar valoración RAWG") }
}
@Composable private fun RatingPanel(title: String, score: Float?, subtitle: String, modifier: Modifier) {
    val color = score?.let { ratingColor(it, LocalVisualTheme.current.dark) } ?: MaterialTheme.colorScheme.onSurfaceVariant
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, color.copy(alpha = .6f))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(score?.let(::formatPersonalRating) ?: "—", color = color, style = MaterialTheme.typography.headlineLarge)
            if (score != null) Text("/ 100", color = color, style = MaterialTheme.typography.labelSmall)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}
