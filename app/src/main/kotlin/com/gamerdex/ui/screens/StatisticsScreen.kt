package com.gamerdex.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*
import com.gamerdex.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun StatisticsScreen(games: List<Game>, menu: () -> Unit, open: (CollectionFilter) -> Unit, platform: (String) -> Unit, genre: (String) -> Unit) {
    val stats = remember(games) { collectionStats(games) }
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = { TopAppBar(title = { Text("Estadísticas") }, navigationIcon = { TextButton(onClick = menu) { Text("☰") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Tu historia en números.", style = MaterialTheme.typography.headlineMedium) }
            item { Text("Toca una cifra para ver sus juegos. El 100 % de progreso es independiente de tener caja, manual y disco.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            CollectionFilter.entries.chunked(2).forEach { pair -> item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { filter -> Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f).clickable { open(filter) }) {
                    Column(Modifier.padding(16.dp)) { Text("${stats.totals.getValue(filter)}", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary); Text(filter.title, style = MaterialTheme.typography.labelMedium) }
                } }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            } } }
            item { SectionLabel("01", "Por plataforma") }
            if (stats.platforms.isEmpty()) item { Text("Añade juegos para empezar tus estadísticas.") }
            stats.platforms.entries.sortedByDescending { it.value }.forEach { (name, count) -> item { StatBar(name, count, games.size) { platform(name) } } }
            item { SectionLabel("02", "Por género") }
            item { Text("Un juego puede contar en varios géneros.", style = MaterialTheme.typography.bodySmall) }
            stats.genres.entries.sortedByDescending { it.value }.forEach { (name, count) -> item { StatBar(name, count, games.size) { genre(name) } } }
        }
    }
}
@Composable private fun StatBar(name: String, count: Int, total: Int, click: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().clickable(onClick = click)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row { Text(name, Modifier.weight(1f)); Text("$count", color = MaterialTheme.colorScheme.primary) }
            LinearProgressIndicator(progress = { if (total == 0) 0f else count.toFloat() / total }, modifier = Modifier.fillMaxWidth())
        }
    }
}
