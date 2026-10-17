package com.gamerdex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformPicker(value: String, logo: String?, enabled: Boolean, change: (String) -> Unit) {
    var show by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(value, change, label = { Text("Plataforma *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = enabled,
        leadingIcon = { BrandLogo(value, logo, Modifier.size(32.dp), platform = true) },
        trailingIcon = { TextButton(onClick = { show = true }, enabled = enabled) { Text("Elegir") } })
    if (show) {
        var query by rememberSaveable { mutableStateOf("") }
        ModalBottomSheet(onDismissRequest = { show = false }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text("Elegir plataforma", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(query, { query = it }, label = { Text("Buscar consola o sistema") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
                val matches = PlatformCatalog.names.filter { it.contains(query.trim(), ignoreCase = true) }
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 400.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                    if (matches.isEmpty()) item { Text("Puedes escribir una plataforma personalizada en el formulario.", Modifier.padding(16.dp)) }
                    items(matches, key = { it }) { name ->
                        Row(Modifier.fillMaxWidth().clickable { change(name); show = false }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            BrandLogo(name, null, Modifier.size(40.dp), platform = true)
                            Text(name, Modifier.weight(1f))
                            if (name == value) Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
