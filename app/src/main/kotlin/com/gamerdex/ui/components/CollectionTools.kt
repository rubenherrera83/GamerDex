package com.gamerdex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.CollectionOrder

@Composable fun CollectionOrderMenu(order: CollectionOrder, change: (CollectionOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }, modifier = Modifier.testTag("collection-order")) {
            Text("Ordenar: ${order.label}", maxLines = 2)
        }
        DropdownMenu(expanded, { expanded = false }) {
            CollectionOrder.entries.forEach { candidate ->
                DropdownMenuItem(text = { Text(candidate.label) }, onClick = { change(candidate); expanded = false },
                    trailingIcon = { if (candidate == order) Text("✓") })
            }
        }
    }
}

@Composable private fun FilterChoice(label: String, value: String?, choices: List<String>, change: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton({ expanded = true }, Modifier.fillMaxWidth().testTag("filter-${if(label=="Plataforma")"platform" else "genre"}")) {
                Text(value ?: "Todas", modifier = Modifier.weight(1f)); Text("⌄")
            }
            DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 360.dp)) {
                DropdownMenuItem(text = { Text("Todas") }, onClick = { change(null); expanded = false })
                choices.forEach { name -> DropdownMenuItem(text = { Text(name) }, onClick = { change(name); expanded = false },modifier=Modifier.testTag("filter-option-$label-$name"),
                    leadingIcon = { if(label=="Plataforma") BrandLogo(name,null,Modifier.size(24.dp),platform=true) }) }
            }
        }
    }
}

@Composable fun CollectionFilters(
    platforms: List<String>, genres: List<String>, platform: String?, genre: String?,
    favorites: Boolean, finished: Boolean, specials: Boolean,
    setPlatform: (String?) -> Unit, setGenre: (String?) -> Unit, setFavorites: (Boolean) -> Unit,
    setFinished: (Boolean) -> Unit, setSpecials: (Boolean) -> Unit, clear: () -> Unit, close: () -> Unit,
) {
    AlertDialog(onDismissRequest = close, title = { Text("Filtrar colección") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChoice("Plataforma",platform,platforms,setPlatform)
            FilterChoice("Género",genre,genres,setGenre)
            FilterChip(favorites,{setFavorites(!favorites)},label={Text("Solo favoritos")})
            FilterChip(finished,{setFinished(!finished)},label={Text("Solo finalizados")})
            FilterChip(specials,{setSpecials(!specials)},label={Text("Solo ediciones especiales")})
            Text("Los filtros se combinan entre sí.",style=MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(close) { Text("Ver resultados") } },
        dismissButton = { TextButton(clear) { Text("Limpiar filtros") } })
}
