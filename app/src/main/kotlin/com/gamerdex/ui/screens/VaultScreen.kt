package com.gamerdex.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.gamerdex.ui.CollectionState
import com.gamerdex.ui.CollectionView
import com.gamerdex.ui.components.*
import com.gamerdex.ui.theme.*
import com.gamerdex.branding.GamerDexLogo

@Composable fun VaultScreen(state: CollectionState, add: () -> Unit, open: (Long) -> Unit,
    menu: () -> Unit, platform: String?, genre: String?, setPlatform: (String?) -> Unit, clearFilters: () -> Unit,
    mode: CollectionView = CollectionView.LIST, setMode: (CollectionView) -> Unit = {}, titles: () -> Unit = {}, systems: () -> Unit = {}, specials: () -> Unit = {}, toggleFavorite: (com.gamerdex.core.domain.Game) -> Unit = {}, systemCount:Int=0) {
    var query by rememberSaveable { mutableStateOf("") }
    val platforms = remember(state.games) { state.games.map { it.platform }.distinct().sorted() }
    val visible = remember(state.games, platform, genre, query) { state.games.filter { (platform == null || it.platform == platform) && (genre == null || genre in it.genres || (genre == "Sin género" && it.genres.isEmpty())) && it.title.contains(query, true) } }
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, floatingActionButton = { ExtendedFloatingActionButton(onClick = add, containerColor = CyberCyan, contentColor = MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.medium) { Text("+ AÑADIR JUEGO") } }) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
        val columns = when (mode) { CollectionView.GRID -> GridCells.Fixed(if (maxWidth >= 600.dp) 4 else 3); CollectionView.COVER_FLOW -> GridCells.Fixed(1); CollectionView.LIST -> GridCells.Adaptive(155.dp) }
        LazyVerticalGrid(columns = columns, modifier = Modifier.fillMaxSize().testTag("collection-grid"), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    IconButton(onClick = menu, modifier = Modifier.then(Modifier.semantics { contentDescription = "Abrir menú" })) { Text("☰", color = CyberCyan, style = MaterialTheme.typography.headlineSmall) }
                    Column(Modifier.weight(1f).padding(start = 8.dp)) {
                        GamerDexLogo(Modifier.widthIn(max = 230.dp).fillMaxWidth())
                        Text("TU COLECCIÓN / PLAYER HUB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("◆", color = NeonPurple)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Metric("%02d".format(state.games.size), "TÍTULOS", Modifier.weight(1f).clickable(onClickLabel = "Ver todos los títulos", onClick = titles))
                    Metric("%02d".format(systemCount), "SISTEMAS", Modifier.weight(1f).testTag("owned-systems-counter").clickable(onClickLabel = "Ver mis sistemas", onClick = systems))
                    Metric("%02d".format(state.games.count { it.effectiveEdition != com.gamerdex.core.domain.EditionType.STANDARD }), "ESPECIALES", Modifier.weight(1f).clickable(onClickLabel = "Ver ediciones especiales", onClick = specials))
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(query, { query = it }, label = { Text("Buscar en tu colección") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.medium)
            }
            if (platform != null || genre != null) item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(listOfNotNull(platform, genre).joinToString(" / "), color = NeonPurple, modifier = Modifier.weight(1f))
                    TextButton(onClick = clearFilters) { Text("Limpiar filtros") }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(platform == null, { setPlatform(null) }, label = { Text("Todas") }) }
                    items(platforms) { name -> FilterChip(platform == name, { setPlatform(name) }, label = { Text(name) }, leadingIcon = { BrandLogo(name, state.games.firstOrNull { it.platform == name }?.platformLogoUrl, Modifier.size(22.dp), platform = true) }) }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionLabel("01", "Archivo de juegos / ${visible.size}") }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CollectionView.entries.forEach { candidate -> FilterChip(mode == candidate, { setMode(candidate) }, label = { Text(candidate.label) }) }
                }
            }
            if (state.loading) item(span = { GridItemSpan(maxLineSpan) }) { Shimmer(Modifier.fillMaxWidth().height(200.dp)) }
            else if (visible.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (state.games.isEmpty()) "Tu leyenda empieza aquí." else "Sin coincidencias.", style = MaterialTheme.typography.titleLarge)
                    Text(if (state.games.isEmpty()) "Añade tu primer juego. Conserva cada edición, cada hallazgo y cada recuerdo." else "Prueba otro título o limpia los filtros.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else if (mode == CollectionView.COVER_FLOW) item(span = { GridItemSpan(maxLineSpan) }) {
                key(platform, genre, query) { CoverFlow(visible, open) }
            }
            else items(visible, key = { it.id }) { game ->
                if (mode == CollectionView.GRID) CompactGameCard(game, { toggleFavorite(game) }) { open(game.id) }
                else GameCard(game, { toggleFavorite(game) }) { open(game.id) }
            }
        }
    }
}
}
