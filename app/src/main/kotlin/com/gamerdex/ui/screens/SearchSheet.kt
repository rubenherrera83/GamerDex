package com.gamerdex.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.Metadata
import com.gamerdex.ui.SearchState
import com.gamerdex.ui.components.Cover

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SearchSheet(state: SearchState, search: (String) -> Unit, choose: (Metadata) -> Unit, dismiss: () -> Unit) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    ModalBottomSheet(onDismissRequest = dismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("Recluta tu próximo juego", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(state.query, search, label = { Text("Buscar título en RAWG") }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), singleLine = true)
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 12.dp))
                TextButton(onClick = { search(state.query) }) { Text("Reintentar") }
            }
            if (!state.loading && state.error == null && state.query.length >= 2 && state.results.isEmpty()) Text("No se han encontrado juegos.")
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(state.results, key = { it.id }) { result ->
                    Row(Modifier.fillMaxWidth().clickable(enabled = !state.loading) { choose(result) }.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Cover(result.coverUrl, result.title, Modifier.size(64.dp))
                        Column { Text(result.title, style = MaterialTheme.typography.titleMedium); Text(listOfNotNull(result.year?.toString(), result.platforms.take(2).joinToString()).joinToString(" · "), style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
            TextButton(onClick = dismiss) { Text("Continuar con el formulario manual") }
            TextButton(onClick = { uriHandler.openUri("https://rawg.io") }) { Text("Metadatos e imágenes: RAWG ↗", style = MaterialTheme.typography.labelSmall) }
        }
    }
}
