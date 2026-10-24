package com.gamerdex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.Game

/** A small cover and two text lines leave room for large collections, with a 48 dp favourite target. */
@Composable fun GameListRow(game: Game, favorite: () -> Unit, open: () -> Unit) {
    OutlinedCard(onClick=open,modifier=Modifier.fillMaxWidth().testTag("game-row-${game.id}")) {
        Row(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(start=8.dp,end=4.dp,top=6.dp,bottom=6.dp),
            verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Cover(game.coverUrl ?: game.localCustomPhotos.firstOrNull(),game.title,Modifier.size(36.dp,48.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                Text(game.title,style=MaterialTheme.typography.bodyMedium,maxLines=2,overflow=TextOverflow.Ellipsis)
                Text(game.platform,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
            }
            IconButton(onClick=favorite,modifier=Modifier.semantics { contentDescription=if(game.isFavorite) "Quitar ${game.title} de favoritos" else "Marcar ${game.title} como favorito" }) {
                Text(if(game.isFavorite)"★" else "☆",style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.primary)
            }
        }
    }
}
