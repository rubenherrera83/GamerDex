package com.gamerdex.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.Game
import com.gamerdex.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun BrowseScreen(games: List<Game>, platforms: Boolean, menu: () -> Unit, select: (String) -> Unit) {
    val groups = remember(games, platforms) {
        if (platforms) games.groupBy { it.platform }
        else games.flatMap { game -> game.genres.map { it to game } }.groupBy({ it.first }, { it.second })
    }
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, topBar = { ScreenHeader(if (platforms) "Plataformas" else "Géneros",menu=menu) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { SectionLabel("EXPLORAR", "${groups.size} ${if (platforms) "sistemas" else "géneros"}") }
            if (groups.isEmpty()) item { Text(if (platforms) "Añade juegos para ver tus plataformas." else "Indica los géneros al añadir juegos o impórtalos desde RAWG.", Modifier.padding(vertical = 24.dp)) }
            items(groups.keys.sorted(), key = { it }) { name ->
                Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().clickable { select(name) }) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        if (platforms) BrandLogo(name, groups.getValue(name).first().platformLogoUrl, Modifier.size(48.dp), platform = true)
                        else Cover(groups.getValue(name).first().coverUrl, name, Modifier.size(48.dp))
                        Column(Modifier.weight(1f)) { Text(name, style = MaterialTheme.typography.titleMedium); Text("${groups.getValue(name).size} juegos", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Text("↗", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
