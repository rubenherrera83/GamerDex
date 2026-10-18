package com.gamerdex.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.Metadata
import com.gamerdex.ui.SearchState
import com.gamerdex.ui.components.Cover
import com.gamerdex.ui.components.PlatformCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SearchSheet(state: SearchState, search: (String) -> Unit, choose: (Metadata) -> Unit, dismiss: () -> Unit, lookupBarcode: (String) -> Unit = {}) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var platformFilter by rememberSaveable { mutableStateOf("") }
    var yearFilter by rememberSaveable { mutableStateOf("") }
    var barcode by rememberSaveable { mutableStateOf("") }
    var showBarcode by rememberSaveable { mutableStateOf(false) }
    var scannerError by remember { mutableStateOf<String?>(null) }
    var selecting by remember { mutableStateOf<Metadata?>(null) }
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("Recluta tu próximo juego", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(state.query, search, label = { Text("Buscar título en RAWG") }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), singleLine = true)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(platformFilter,{platformFilter=it},label={Text("Filtrar plataforma")},modifier=Modifier.weight(1f),singleLine=true)
                OutlinedTextField(yearFilter,{yearFilter=it.filter(Char::isDigit).take(4)},label={Text("Año")},modifier=Modifier.weight(.55f),singleLine=true)
            }
            TextButton(onClick={showBarcode=true},enabled=!state.loading){Text("Buscar por código de barras / ScanDex")}
            state.barcodeMatch?.let { match -> Text("ScanDex: ${match.title} · ${match.platform}. Confirma el juego y su versión en RAWG.",style=MaterialTheme.typography.bodySmall) }
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 12.dp))
                TextButton(onClick = { search(state.query) }) { Text("Reintentar") }
            }
            if (!state.loading && state.error == null && state.query.length >= 2 && state.results.isEmpty()) Text("No se han encontrado juegos.")
            val matches=state.results.filter { (yearFilter.isBlank() || it.year?.toString()?.startsWith(yearFilter)==true) && (platformFilter.isBlank() || it.platforms.any { p -> PlatformCatalog.matches(p,platformFilter.trim()) }) }
            LazyColumn(Modifier.weight(1f,false).heightIn(max = 350.dp)) {
                if(matches.isEmpty() && state.results.isNotEmpty()) item { Text("Sin coincidencias con estos filtros; prueba a limpiarlos.") }
                items(matches, key = { it.id }) { result ->
                    Row(Modifier.fillMaxWidth().clickable(enabled = !state.loading) { if(result.platforms.size>1) selecting=result else choose(result) }.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Cover(result.coverUrl, result.title, Modifier.size(64.dp))
                        Column { Text(result.title, style = MaterialTheme.typography.titleMedium); Text(listOfNotNull(result.year?.toString(), result.platforms.joinToString { PlatformCatalog.display(it) }).joinToString(" · "), style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
            TextButton(onClick = dismiss) { Text("Continuar con el formulario manual") }
            Row { TextButton(onClick = { uriHandler.openUri("https://rawg.io") }) { Text("Datos: RAWG ↗", style = MaterialTheme.typography.labelSmall) }
                TextButton(onClick = { uriHandler.openUri("https://scandex.gamery.app") }) { Text("Códigos: ScanDex ↗",style=MaterialTheme.typography.labelSmall) } }
            Text("Los filtros se aplican a los primeros 40 resultados. Puedes escribir un título más concreto.",style=MaterialTheme.typography.labelSmall)
        }
    }
    if(showBarcode) BarcodeSearchDialog(dismiss={showBarcode=false},lookup={showBarcode=false;lookupBarcode(it)})
    selecting?.let { game -> AlertDialog(onDismissRequest={selecting=null},title={Text("Elige tu versión")},text={
        LazyColumn(Modifier.heightIn(max=350.dp)) { items(game.platforms.distinct()) { platform ->
            TextButton(onClick={selecting=null; choose(game.copy(platforms=listOf(platform)))},modifier=Modifier.fillMaxWidth()){Text(PlatformCatalog.display(platform))}
        } }
    },confirmButton={},dismissButton={TextButton(onClick={selecting=null}){Text("Cancelar")}}) }
}
