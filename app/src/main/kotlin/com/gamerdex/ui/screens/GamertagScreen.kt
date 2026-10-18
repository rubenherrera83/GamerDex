package com.gamerdex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamerdex.core.domain.Game
import com.gamerdex.core.domain.Hardware
import com.gamerdex.gamertag.*
import com.gamerdex.ui.components.*
import com.gamerdex.ui.theme.VisualTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.content.Intent
import kotlinx.coroutines.launch
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun GamertagScreen(games: List<Game>, theme: VisualTheme, initialGame: Long, menu: () -> Unit, defaultAlias: String = "", hardware: List<Hardware> = emptyList(), viewModel: GamertagViewModel = hiltViewModel(),initialSystem:Long=0) {
    var alias by rememberSaveable(defaultAlias) { mutableStateOf(defaultAlias.ifBlank { "PLAYER ONE" }) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sharing by remember { mutableStateOf(false) }
    var shareError by remember { mutableStateOf<String?>(null) }
    var target by rememberSaveable { mutableStateOf(if(initialSystem>0)"Sistema / PC" else if (initialGame > 0) "Juego" else "Colección") }
    var systemId by rememberSaveable{mutableLongStateOf(initialSystem.takeIf{it>0} ?: hardware.firstOrNull()?.id ?: 0L)}
    var selectedComponents by rememberSaveable(systemId){mutableStateOf<List<Int>?>(null)}
    var includeNotes by rememberSaveable{mutableStateOf(false)}
    var gameId by rememberSaveable { mutableLongStateOf(initialGame.takeIf { it > 0 } ?: games.firstOrNull()?.id ?: 0L) }
    var platform by rememberSaveable { mutableStateOf(games.firstOrNull()?.platform.orEmpty()) }
    var choosing by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val logos by viewModel.logos.collectAsStateWithLifecycle()
    var customize by rememberSaveable { mutableStateOf(false) }
    val selected = games.firstOrNull { it.id == gameId }
    val selectedSystem=hardware.firstOrNull{it.id==systemId}
    val componentIndices=(selectedComponents ?: selectedSystem?.controllers?.indices?.take(10).orEmpty()).filter{it in (selectedSystem?.controllers?.indices ?: IntRange.EMPTY)}
    val subset = if (target == "Plataforma") games.filter { it.platform == platform } else games
    val input = when (target) {
        "Sistema / PC" -> selectedSystem?.let{hardwareGamertag(alias,it,theme,componentIndices,includeNotes)} ?: GamertagInput(alias,"ELIGE UN SISTEMA / PC","MI EQUIPO","",null,theme,hardwareCard=true)
        "Juego" -> GamertagInput(alias, selected?.title ?: "Elige un juego", selected?.let { "${it.platform} / ${it.company}" }.orEmpty(), selected?.let { "${it.contentsLabel}   /   ${it.releaseYear ?: "SIN FECHA"}" } ?: "MI ARCHIVO PERSONAL", selected?.coverUrl, theme)
        "Plataforma" -> GamertagInput(alias, platform.ifBlank { "MI PLATAFORMA" }, "ARCHIVO DE PLATAFORMA", "${subset.size} JUEGOS   /   ${subset.count { it.isComplete }} COMPLETOS", subset.firstOrNull()?.coverUrl, theme)
        else -> GamertagInput(alias, "MI COLECCIÓN", "CADA JUEGO TIENE UNA HISTORIA", "${games.count { it.isComplete }} COMPLETOS   /   ${games.map { it.platform }.distinct().size} PLATAFORMAS", games.firstOrNull()?.coverUrl, theme)
    }.copy(totalGames=games.size,totalSystems=hardware.size,logos=logos)
    val canExport=state.bitmap!=null && state.renderedInput==input && (target=="Colección" || (target=="Juego" && selected!=null) || (target=="Plataforma" && subset.isNotEmpty()) || (target=="Sistema / PC" && selectedSystem!=null))
    LaunchedEffect(input) { viewModel.generate(input) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { it?.let(viewModel::export) }
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, topBar = { TopAppBar(title = { Text("Gamertags") }, navigationIcon = { TextButton(onClick = menu) { Text("☰") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("gamertag-editor"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { SectionLabel("CREATOR", "Tu identidad. Tu colección.") }
            item { Text("Llévate tu estilo.", style = MaterialTheme.typography.headlineLarge) }
            item { Text("Genera una tarjeta de un juego, una plataforma, un sistema/PC con sus componentes o tu colección. El diseño sigue el tema elegido en preferencias.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { OutlinedTextField(alias, { alias = it.take(32) }, label = { Text("Tu gamertag / alias") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Juego", "Plataforma", "Colección","Sistema / PC").forEach { kind -> FilterChip(target == kind, { target = kind }, label = { Text(kind) }) } } }
            if (target != "Colección") item {
                OutlinedButton(onClick = { query = ""; choosing = true }, modifier = Modifier.fillMaxWidth().testTag("gamertag-target-picker"), enabled = if(target=="Sistema / PC")hardware.isNotEmpty() else games.isNotEmpty()) { Text(when(target){"Sistema / PC"->selectedSystem?.name ?: "Elegir sistema / PC";"Juego"->selected?.title ?: "Elegir juego";else->platform.ifBlank { "Elegir plataforma" }}) }
            }
            if(target=="Sistema / PC") {
                item { Text("Componentes y accesorios",style=MaterialTheme.typography.titleMedium)
                    Text("Elige hasta 10 elementos de la ficha. Los textos largos se recortan en la imagen. No se copian los campos de precio, fecha o serie.",style=MaterialTheme.typography.bodySmall)
                    if(selectedSystem==null)Text("Elige un equipo registrado en Mis sistemas.")
                    else if(selectedSystem.controllers.isEmpty())Text("Puedes añadir CPU, GPU, RAM, discos y periféricos como accesorios en la ficha del sistema.")
                }
                items(selectedSystem?.controllers?.size ?: 0){index->
                    val c=selectedSystem!!.controllers[index];val checked=index in componentIndices
                    Row(Modifier.fillMaxWidth().toggleable(checked,enabled=checked || componentIndices.size<10,role=androidx.compose.ui.semantics.Role.Checkbox,onValueChange={selectedComponents=if(it)componentIndices+index else componentIndices-index}).padding(vertical=4.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){
                        Checkbox(checked,null,enabled=checked || componentIndices.size<10);Text("${c.quantity} × ${c.name}",Modifier.weight(1f))
                    }
                }
                item { Row(Modifier.fillMaxWidth().toggleable(includeNotes,role=androidx.compose.ui.semantics.Role.Checkbox,onValueChange={includeNotes=it}),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Checkbox(includeNotes,null);Text("Incluir notas de componentes",Modifier.weight(1f))} }
            }
            item { Text("Totales de tu colección: ${games.size} videojuegos · ${hardware.size} sistemas",style=MaterialTheme.typography.titleMedium)
                Text("Cuenta todas las copias de juegos y las unidades registradas en Mis sistemas, también en las tarjetas de un solo juego o plataforma.",style=MaterialTheme.typography.bodySmall) }
            item { OutlinedButton(onClick={customize=true},modifier=Modifier.fillMaxWidth()){Text("Personalizar logotipos (${logos.size}/6)")} }
            items(logos) { logo ->
                val index=logos.indexOf(logo)
                Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                    Text(logo.name,Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,maxLines=2,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    TextButton(onClick={viewModel.setLogos(logos.toMutableList().apply{add(index-1,removeAt(index))})},enabled=index>0,modifier=Modifier.semantics{contentDescription="Subir ${logo.name}"}){Text("↑")}
                    TextButton(onClick={viewModel.setLogos(logos.toMutableList().apply{add(index+1,removeAt(index))})},enabled=index<logos.lastIndex,modifier=Modifier.semantics{contentDescription="Bajar ${logo.name}"}){Text("↓")}
                    IconButton(onClick={viewModel.setLogos(logos-logo)},modifier=Modifier.semantics{contentDescription="Quitar ${logo.name}"}){Text("×")}
                }
            }
            item {
                if (state.bitmap != null) Image(state.bitmap!!.asImageBitmap(), contentDescription = "Vista previa del gamertag", modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
                else if (state.message == null) Shimmer(Modifier.fillMaxWidth().aspectRatio(16f / 9f))
            }
            item { Button(onClick = { viewModel.prepareExport(); export.launch("GamerDex-gamertag-${System.currentTimeMillis()}.png") }, enabled = canExport, modifier = Modifier.fillMaxWidth()) { Text("Guardar gamertag como imagen") } }
            item { OutlinedButton(onClick = {
                val bitmap = state.bitmap ?: return@OutlinedButton
                sharing = true; shareError = null
                scope.launch {
                    try { context.startActivity(Intent.createChooser(GamertagSharing.intent(context, bitmap), "Compartir gamertag")) }
                    catch (e: kotlinx.coroutines.CancellationException) { throw e }
                    catch (_: Exception) { shareError = "No se pudo compartir. Puedes guardar el PNG y enviarlo desde tu galería." }
                    finally { sharing = false }
                }
            }, enabled = !sharing && canExport, modifier = Modifier.fillMaxWidth()) { Text(if (sharing) "Preparando imagen…" else "Compartir gamertag") } }
            shareError?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
            state.message?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.primary) } }
            item { Text("PNG · 1600 × 900 · Sin subir tus datos a servicios externos", style = MaterialTheme.typography.labelSmall) }
        }
    }
    if(customize) GamertagLogoPicker(games,hardware,logos,viewModel::setLogos){customize=false}
    if (choosing) ModalBottomSheet(onDismissRequest = { choosing = false }) {
        Column(Modifier.padding(20.dp)) {
            OutlinedTextField(query, { query = it }, label = { Text("Buscar ${target.lowercase()}") }, modifier = Modifier.fillMaxWidth())
            LazyColumn(Modifier.heightIn(max = 400.dp)) {
                if(target=="Sistema / PC")items(hardware.filter{(it.name+" "+it.platform).contains(query,true)},key={it.id}){system->Text("${system.name} · ${system.platform} · #${system.id}",Modifier.fillMaxWidth().clickable{systemId=system.id;choosing=false}.padding(16.dp))}
                else if (target == "Juego") items(games.filter { it.title.contains(query, true) }, key = { it.id }) { game -> Text(game.title, Modifier.fillMaxWidth().clickable { gameId = game.id; choosing = false }.padding(16.dp)) }
                else items(games.map { it.platform }.distinct().sorted().filter { it.contains(query, true) }) { name -> Text(name, Modifier.fillMaxWidth().clickable { platform = name; choosing = false }.padding(16.dp)) }
            }
        }
    }
}
