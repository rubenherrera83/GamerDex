package com.gamerdex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*
import com.gamerdex.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LibraryScreen(games: List<Game>, filter: CollectionFilter, menu: () -> Unit, open: (Long) -> Unit, favorite: (Game) -> Unit) {
    var platform by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val subset = remember(games, filter) { games.filter(filter::accepts).sortedBy { it.title.lowercase() } }
    val systems = subset.map { it.platform }.distinct().sorted()
    val visible = subset.filter { (platform == null || it.platform == platform) && it.title.contains(query, true) }
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = { TopAppBar(title = { Text(filter.title) }, navigationIcon = { TextButton(onClick = menu) { Text("☰") } }) }) { padding ->
        LazyVerticalGrid(GridCells.Adaptive(155.dp), Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item(span = { GridItemSpan(maxLineSpan) }) { OutlinedTextField(query, { query = it }, label = { Text("Buscar en este listado") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item(span = { GridItemSpan(maxLineSpan) }) { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(platform == null, { platform = null }, label = { Text("Todos los sistemas") }) }
                items(systems) { name -> FilterChip(platform == name, { platform = name }, label = { Text(name) }, leadingIcon = { BrandLogo(name, subset.firstOrNull { it.platform == name }?.platformLogoUrl, Modifier.size(24.dp), platform = true) }) }
                if (platform != null && platform !in systems) item { FilterChip(true, { platform = null }, label = { Text("Limpiar filtro") }) }
            } }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionLabel("ARCHIVO", "${visible.size} juegos") }
            if (visible.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { Text(if (filter == CollectionFilter.FAVORITES && subset.isEmpty()) "Marca juegos con ★ para encontrarlos aquí." else "Sin coincidencias. Prueba otro sistema o búsqueda.", Modifier.padding(vertical = 24.dp)) }
            items(visible, key = { it.id }) { game -> GameCard(game, { favorite(game) }) { open(game.id) } }
        }
    }
}
