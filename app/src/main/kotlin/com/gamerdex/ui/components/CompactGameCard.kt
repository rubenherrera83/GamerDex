package com.gamerdex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.Game
import com.gamerdex.core.domain.EditionType

@Composable fun CompactGameCard(game: Game, favorite: () -> Unit, open: () -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("compact-cover-${game.id}").clip(MaterialTheme.shapes.small)
        .background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small).clickable(onClick = open)) {
        Box(Modifier.fillMaxWidth().aspectRatio(.72f)) {
            Cover(game.coverUrl ?: game.localCustomPhotos.firstOrNull(), game.title, Modifier.fillMaxSize())
            IconButton(onClick = favorite, modifier = Modifier.align(Alignment.TopEnd).background(MaterialTheme.colorScheme.surface.copy(alpha = .88f), MaterialTheme.shapes.small)
                .semantics { contentDescription = if (game.isFavorite) "Quitar ${game.title} de favoritos" else "Marcar ${game.title} como favorito" }) {
                Text(if (game.isFavorite) "★" else "☆", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
            }
            if (game.effectiveEdition != EditionType.STANDARD) Text("◆", color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.BottomStart).background(MaterialTheme.colorScheme.surface.copy(alpha = .9f)).padding(5.dp).semantics { contentDescription = "Edición especial" })
            if (game.isFinished) Text("✓", color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.align(Alignment.BottomEnd).background(MaterialTheme.colorScheme.secondaryContainer).padding(5.dp).semantics { contentDescription = "Juego finalizado" })
        }
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(game.title, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
            Text(game.platform, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
