package com.gamerdex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamerdex.ui.PreferencesViewModel
import com.gamerdex.ui.components.SectionLabel
import com.gamerdex.ui.theme.*
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PreferencesScreen(menu: () -> Unit, viewModel: PreferencesViewModel = hiltViewModel()) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val alias by viewModel.alias.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { it?.let(viewModel::export) }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(viewModel::inspect) }
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, topBar = { TopAppBar(title = { Text("Preferencias") }, navigationIcon = { TextButton(onClick = menu) { Text("☰") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("preferences-list"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { SectionLabel("PLAYER", "Tu identidad") }
            item { OutlinedTextField(alias, viewModel::setAlias, label = { Text("Apodo predeterminado") }, supportingText = { Text("Se usará al crear gamertags. Puedes cambiarlo en cada tarjeta.") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item { SectionLabel("01", "Elige tu universo") }
            VisualTheme.entries.forEach { candidate -> item {
                val p = candidate.palette()
                Surface(shape = MaterialTheme.shapes.medium, border = androidx.compose.foundation.BorderStroke(if (theme == candidate) 2.dp else 1.dp, if (theme == candidate) p.primary else MaterialTheme.colorScheme.outline),
                    color = p.surface, contentColor = p.text, modifier = Modifier.fillMaxWidth().clickable(enabled = !state.busy) { viewModel.select(candidate) }) {
                    Row(Modifier.padding(18.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(Modifier.weight(1f)) { Text(candidate.title, style = MaterialTheme.typography.titleLarge); Text(candidate.tagline, style = MaterialTheme.typography.bodySmall) }
                        listOf(p.primary, p.secondary, p.tertiary).forEach { color -> Box(Modifier.size(13.dp).background(color)) }
                        if (theme == candidate) Text("✓", color = p.primary)
                    }
                }
            } }
            item { SectionLabel("02", "Copias de seguridad") }
            item { Text("Tu colección, a salvo.", style = MaterialTheme.typography.titleLarge) }
            item { Text("Exporta juegos, sistemas, mandos, plataformas, compañías y fotos locales en un archivo .zip. Guárdalo donde prefieras y recupéralo en otro dispositivo. Los ajustes de tema se conservan por separado.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { Button(onClick = { export.launch("GamerDex-${System.currentTimeMillis()}.zip") }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Crear copia de seguridad") } }
            item { OutlinedButton(onClick = { restore.launch(arrayOf("application/zip", "application/octet-stream", "application/x-zip-compressed")) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Recuperar copia de seguridad") } }
            if (state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Procesando archivos…") }
            state.message?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.primary) } }
            item { Text("Las copias incluyen tus notas, importes y fotos. No incluyen la clave de RAWG. Una recuperación sustituye la colección actual después de validar el archivo y pedirte confirmación.", style = MaterialTheme.typography.bodySmall) }
        }
    }
    state.pending?.let { pending ->
        AlertDialog(onDismissRequest = { if (!state.busy) viewModel.cancelRestore() }, title = { Text("¿Recuperar esta colección?") },
            text = { Text("${pending.titles} juegos · ${pending.systems} sistemas · ${pending.purchases} compras · ${pending.photos} fotos\n${DateFormat.getDateTimeInstance().format(Date(pending.createdAt))}\n\nSe sustituirán la colección y el historial. Las copias antiguas no contienen compras de fichas eliminadas; las anteriores a v4 tampoco contienen sistemas. Puedes cancelar y crear primero una copia de tus datos.") },
            confirmButton = { TextButton(onClick = viewModel::restore, enabled = !state.busy) { Text("Restaurar colección") } },
            dismissButton = { TextButton(onClick = viewModel::cancelRestore, enabled = !state.busy) { Text("Cancelar") } })
    }
}
