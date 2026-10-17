package com.gamerdex.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.gamerdex.core.domain.Game
import com.gamerdex.ui.theme.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

@Composable fun Modifier.neonBorder(special: Boolean = false): Modifier = shadow(if (special) 12.dp else 4.dp, RoundedCornerShape(18.dp), clip = false,
    ambientColor = if (special) NeonPurple else CyberCyan, spotColor = if (special) NeonPurple else CyberCyan).border(1.dp,
    Brush.linearGradient(listOf(if (special) NeonPurple else CyberCyan, Color.Transparent, if (special) AcidGreen else CyberCyan.copy(alpha = .25f))), RoundedCornerShape(18.dp))

@Composable fun Shimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "loading")
    val position by transition.animateFloat(0f, 1000f, infiniteRepeatable(tween(1100), RepeatMode.Restart), label = "shine")
    Box(modifier.background(Brush.linearGradient(listOf(VaultSurface, Color(0xFF293347), VaultSurface),
        start = androidx.compose.ui.geometry.Offset(position - 500, 0f), end = androidx.compose.ui.geometry.Offset(position, 500f))))
}

@Composable fun Cover(url: String?, title: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    SubcomposeAsyncImage(model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
        contentDescription = title, contentScale = contentScale, modifier = modifier.clip(MaterialTheme.shapes.small),
        loading = { Shimmer(Modifier.fillMaxSize()) },
        error = { Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, VaultSurface))), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CyberBackdrop(Modifier.matchParentSize())
            Text(title.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = CyberCyan)
        } })
}

@Composable fun GameCard(game: Game, toggleFavorite: (() -> Unit)? = null, onClick: () -> Unit) {
    val special = game.effectiveEdition != com.gamerdex.core.domain.EditionType.STANDARD
    val shape = MaterialTheme.shapes.medium
    Column(Modifier.fillMaxWidth().clip(shape).background(VaultSurface).border(1.dp, if (special) CyberCyan.copy(alpha = .8f) else MaterialTheme.colorScheme.outline.copy(alpha = .6f), shape).clickable(onClick = onClick)) {
        Box {
            Cover(game.coverUrl, game.title, Modifier.fillMaxWidth().aspectRatio(.82f))
            Box(Modifier.fillMaxWidth().height(60.dp).align(androidx.compose.ui.Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, VaultSurface))))
            if (special) Surface(color = CyberCyan, contentColor = MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.small, modifier = Modifier.padding(8.dp)) {
                Text(if (game.effectiveEdition == com.gamerdex.core.domain.EditionType.COLLECTOR) "★ COLLECTOR" else "✦ SPECIAL", Modifier.padding(horizontal = 7.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
            }
            if (toggleFavorite != null) IconButton(onClick = toggleFavorite, modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = .9f), MaterialTheme.shapes.small)
                .then(Modifier.semantics { contentDescription = if (game.isFavorite) "Quitar ${game.title} de favoritos" else "Marcar ${game.title} como favorito" })) {
                Text(if (game.isFavorite) "★" else "☆", color = CyberCyan, style = MaterialTheme.typography.headlineSmall)
            }
        }
        Column(Modifier.padding(horizontal = 12.dp).padding(bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(game.platform.uppercase(), color = NeonPurple, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            Text(game.title, maxLines = 2, minLines = 2, style = MaterialTheme.typography.titleSmall, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            if (game.isFinished || game.completionPercent > 0 || game.hasPlatinum) Text("${if (game.hasPlatinum) "🏆 " else ""}${if (game.isFinished) "FINALIZADO · " else ""}${game.completionPercent} %", color = NeonPurple, style = MaterialTheme.typography.labelSmall)
            Text(if (game.isComplete) "▣ ${game.contentsLabel.uppercase()}" else "▧ INCOMPLETO", color = if (game.isComplete) MaterialTheme.colorScheme.onSurfaceVariant else AcidGreen, style = MaterialTheme.typography.labelSmall)
            game.pricePaidCents?.let { Text(com.gamerdex.core.domain.formatPrice(it, game.priceCurrency), style = MaterialTheme.typography.labelMedium, color = CyberCyan) }
        }
    }
}
