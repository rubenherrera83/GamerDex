package com.gamerdex.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamerdex.core.domain.*
import com.gamerdex.gamertag.*
import com.gamerdex.ui.components.*
import com.gamerdex.ui.theme.VisualTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import androidx.compose.ui.semantics.*

@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable fun GamertagScreen(games:List<Game>,theme:VisualTheme,initialGame:Long,menu:()->Unit,defaultAlias:String="",hardware:List<Hardware> = emptyList(),viewModel:GamertagViewModel=hiltViewModel(),initialSystem:Long=0) {
    var alias by rememberSaveable(defaultAlias){mutableStateOf(defaultAlias.ifBlank{"PLAYER ONE"})}
    var target by rememberSaveable{mutableStateOf(if(initialSystem>0)"Sistema / PC" else if(initialGame>0)"Juego" else "Colección")}
    var systemId by rememberSaveable{mutableLongStateOf(initialSystem.takeIf{it>0} ?: 0L)}
    var gameId by rememberSaveable{mutableLongStateOf(initialGame.takeIf{it>0} ?: 0L)}
    var platform by rememberSaveable{mutableStateOf("")}
    val selected=games.firstOrNull{it.id==gameId} ?: games.firstOrNull().takeIf{gameId==0L}
    val selectedSystem=hardware.firstOrNull{it.id==systemId} ?: hardware.firstOrNull().takeIf{systemId==0L}
    val chosenPlatform=platform.ifBlank{games.firstOrNull()?.platform.orEmpty()}
    var selectedComponents by rememberSaveable(selectedSystem?.id){mutableStateOf<List<Int>?>(null)}
    var includeNotes by rememberSaveable{mutableStateOf(false)}
    var customTheme by rememberSaveable{mutableStateOf<VisualTheme?>(null)}
    val design=customTheme ?: theme
    val componentIndices=(selectedComponents ?: selectedSystem?.controllers?.indices?.take(10).orEmpty()).filter{it in (selectedSystem?.controllers?.indices ?: IntRange.EMPTY)}
    val subset=if(target=="Plataforma")games.filter{it.platform==chosenPlatform} else games
    val state by viewModel.state.collectAsStateWithLifecycle()
    val logos by viewModel.logos.collectAsStateWithLifecycle()
    val input=when(target){
        "Sistema / PC"->selectedSystem?.let{hardwareGamertag(alias,it,design,componentIndices,includeNotes)} ?: GamertagInput(alias,"ELIGE UN SISTEMA / PC","MI EQUIPO","",null,design,hardwareCard=true)
        "Juego"->GamertagInput(alias,selected?.title ?: "Elige un juego",selected?.let{"${it.platform} / ${it.company}"}.orEmpty(),selected?.let{"${it.contentsLabel}   /   ${it.releaseYear ?: "SIN FECHA"}"} ?: "MI ARCHIVO PERSONAL",selected?.coverUrl,design)
        "Plataforma"->GamertagInput(alias,chosenPlatform.ifBlank{"MI PLATAFORMA"},"ARCHIVO DE PLATAFORMA","${subset.size} JUEGOS   /   ${subset.count{it.isComplete}} COMPLETOS",subset.firstOrNull()?.coverUrl,design)
        else->GamertagInput(alias,"MI COLECCIÓN","CADA JUEGO TIENE UNA HISTORIA","${games.count{it.isComplete}} COMPLETOS   /   ${games.map{it.platform}.distinct().size} PLATAFORMAS",games.firstOrNull()?.coverUrl,design)
    }.copy(totalGames=games.size,totalSystems=hardware.size,logos=logos)
    val eligible=when(target){"Juego"->selected!=null;"Plataforma"->subset.isNotEmpty();"Sistema / PC"->selectedSystem!=null;else->true}
    val canExport=eligible && state.bitmap!=null && state.renderedInput==input && !state.rendering
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    val list=rememberLazyListState()
    val focus=LocalFocusManager.current
    val keyboard=LocalSoftwareKeyboardController.current
    var sharing by remember{mutableStateOf(false)}
    var shareError by remember{mutableStateOf<String?>(null)}
    var customize by rememberSaveable{mutableStateOf(false)}
    var choosing by rememberSaveable{mutableStateOf(false)}
    var query by rememberSaveable{mutableStateOf("")}
    var fullPreview by rememberSaveable{mutableStateOf(false)}
    val components=if(target=="Sistema / PC")selectedSystem?.controllers.orEmpty() else emptyList()
    val logosIndex=3+if(target=="Sistema / PC")1+components.size+(if(components.isNotEmpty())1 else 0) else 0
    fun leaveInput(){focus.clearFocus();keyboard?.hide()}
    fun jump(section:String){leaveInput();scope.launch{list.animateScrollToItem(when(section){"preview"->0;"content"->1;"design"->2;"components"->3;else->logosIndex})}}
    LaunchedEffect(input){viewModel.generate(input)}
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")){uri->if(uri!=null)viewModel.export(uri) else viewModel.cancelExport()}
    val problem=when{
        !eligible->if(target=="Sistema / PC")"Elige un equipo registrado en Sistemas para crear esta tarjeta." else "Elige un juego o una plataforma de tu colección para crear esta tarjeta."
        state.rendering->"Actualizando vista previa…"
        else->null
    }
    Scaffold(containerColor=androidx.compose.ui.graphics.Color.Transparent,topBar={Surface{Column{
        ScreenHeader("Gamertags",menu=menu)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=8.dp)){
            val shortcuts=buildList{add("preview" to "Vista previa");add("content" to "Contenido");add("design" to "Diseño");if(target=="Sistema / PC")add("components" to "Componentes");add("logos" to "Logos")}
            shortcuts.forEach{(key,title)->TextButton({jump(key)},modifier=Modifier.testTag("gamertag-jump-$key")){Text(title)}}
        }
    }}},bottomBar={GamertagActions(canExport,sharing || state.exporting,shareError ?: state.message,problem,share={
        val bitmap=state.bitmap
        if(bitmap!=null && canExport){
            leaveInput();sharing=true;shareError=null
            scope.launch{
                try{context.startActivity(Intent.createChooser(GamertagSharing.intent(context,bitmap),"Compartir gamertag"))}
                catch(e:kotlinx.coroutines.CancellationException){throw e}
                catch(_:Exception){shareError="No se pudo compartir. Puedes guardar el PNG y enviarlo desde tu galería."}
                finally{sharing=false}
            }
        }
    },save={leaveInput();if(viewModel.prepareExport())export.launch("GamerDex-gamertag-${System.currentTimeMillis()}.png")})}){padding->
        LazyColumn(state=list,modifier=Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("gamertag-editor"),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            item(key="preview"){
                Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
                    GamertagPreview(state.bitmap,state.rendering,state.message!=null && state.renderedInput!=input,{leaveInput();fullPreview=true},{viewModel.generate(input)})
                    Text("Totales de tu colección: ${games.size} videojuegos · ${hardware.size} sistemas",style=MaterialTheme.typography.bodyMedium)
                }
            }
            item(key="content"){GamertagSection("Contenido e identidad"){
                OutlinedTextField(alias,{alias=it.take(32)},label={Text("Tu gamertag / alias")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                Text("¿Qué quieres mostrar?",style=MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    listOf("Colección","Juego","Plataforma","Sistema / PC").forEach{kind->FilterChip(target==kind,{leaveInput();target=kind},label={Text(kind)},modifier=Modifier.testTag("gamertag-kind-$kind"))}
                }
                if(target!="Colección")OutlinedButton({leaveInput();query="";choosing=true},modifier=Modifier.fillMaxWidth().testTag("gamertag-target-picker"),enabled=if(target=="Sistema / PC")hardware.isNotEmpty() else games.isNotEmpty()){
                    Text(when(target){"Sistema / PC"->selectedSystem?.name ?: "Elegir sistema / PC";"Juego"->selected?.title ?: "Elegir juego";else->chosenPlatform.ifBlank{"Elegir plataforma"}})
                }
                if(target=="Sistema / PC" && selectedSystem!=null)Text(selectedSystem.platform+selectedSystem.serialNumber.takeIf{it.isNotBlank()}?.let{" · Serie: $it"}.orEmpty(),style=MaterialTheme.typography.bodySmall)
                if(target=="Juego" && selected!=null)Text("${selected.platform} · ${selected.effectiveEdition.label()}",style=MaterialTheme.typography.bodySmall)
                Text("Los totales cuentan todos tus videojuegos y equipos, también en tarjetas individuales.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }}
            item(key="design"){GamertagDesign(theme,customTheme){customTheme=it}}
            if(target=="Sistema / PC"){
                item(key="components"){Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Text("Componentes y accesorios · ${componentIndices.size}/10",style=MaterialTheme.typography.titleMedium)
                    Text("Selecciona hasta 10 elementos. Precio, fecha y números de serie no aparecen en la imagen.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    if(selectedSystem==null)Text("Elige un equipo registrado en Sistemas → Mis equipos.")
                    else if(components.isEmpty())Text("Puedes añadir CPU, GPU, RAM, discos y periféricos como accesorios en la ficha del sistema.")
                }}
                itemsIndexed(components,key={index,_->"component-$index"}){index,c->
                    val checked=index in componentIndices
                    OutlinedCard(Modifier.fillMaxWidth()){
                        Row(Modifier.fillMaxWidth().toggleable(checked,enabled=checked || componentIndices.size<10,role=Role.Checkbox,onValueChange={selectedComponents=if(it)componentIndices+index else componentIndices-index}).padding(8.dp),verticalAlignment=Alignment.CenterVertically){
                            Checkbox(checked,null,enabled=checked || componentIndices.size<10);Text("${c.quantity} × ${c.name}",Modifier.weight(1f))
                        }
                    }
                }
                if(components.isNotEmpty())item(key="component-notes"){
                    Row(Modifier.fillMaxWidth().toggleable(includeNotes,role=Role.Checkbox,onValueChange={includeNotes=it}),verticalAlignment=Alignment.CenterVertically){Checkbox(includeNotes,null);Text("Incluir notas de componentes",Modifier.weight(1f))}
                }
            }
            item(key="logos"){GamertagSection("Logotipos · ${logos.size}/6"){
                Text("Añade consolas, compañías, carátulas o imágenes propias. Se recuerdan para tus próximas tarjetas.",style=MaterialTheme.typography.bodySmall)
                OutlinedButton({leaveInput();customize=true},modifier=Modifier.fillMaxWidth()){Text("Personalizar logotipos (${logos.size}/6)")}
                if(logos.isNotEmpty())Text("Usa las flechas para cambiar su orden.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }}
            itemsIndexed(logos,key={index,_->"logo-$index"}){index,logo->
                OutlinedCard(Modifier.fillMaxWidth()){
                    Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){
                        if(logo.kind==GamertagLogoKind.PLATFORM)BrandLogo(logo.name,logo.image,Modifier.size(32.dp),platform=true)
                        else if(logo.image!=null)Cover(logo.image,logo.name,Modifier.size(32.dp),contentScale=androidx.compose.ui.layout.ContentScale.Fit)
                        Text(logo.name,Modifier.weight(1f).padding(start=8.dp),style=MaterialTheme.typography.bodySmall,maxLines=2,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        IconButton({viewModel.setLogos(logos.toMutableList().apply{add(index-1,removeAt(index))})},enabled=index>0,modifier=Modifier.semantics{contentDescription="Subir ${logo.name}"}){Text("↑")}
                        IconButton({viewModel.setLogos(logos.toMutableList().apply{add(index+1,removeAt(index))})},enabled=index<logos.lastIndex,modifier=Modifier.semantics{contentDescription="Bajar ${logo.name}"}){Text("↓")}
                        IconButton({viewModel.setLogos(logos-logo)},modifier=Modifier.semantics{contentDescription="Quitar ${logo.name}"}){Text("×")}
                    }
                }
            }
        }
    }
    if(fullPreview)GamertagFullPreview(state.bitmap){fullPreview=false}
    if(customize)GamertagLogoPicker(games,hardware,logos,viewModel::setLogos){customize=false}
    if(choosing)ModalBottomSheet(onDismissRequest={choosing=false},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)){
        Column(Modifier.padding(20.dp)){
            Text("Elegir ${target.lowercase()}",style=MaterialTheme.typography.titleLarge)
            OutlinedTextField(query,{query=it},label={Text("Buscar ${target.lowercase()}")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            LazyColumn(Modifier.heightIn(max=400.dp).testTag("gamertag-target-results")){
                if(target=="Sistema / PC"){
                    val matches=hardware.filter{(it.name+" "+it.platform+" "+it.serialNumber).contains(query,true)}
                    if(matches.isEmpty())item{Text("No hay coincidencias.",Modifier.padding(16.dp))}
                    items(matches,key={it.id}){system->
                        Column(Modifier.fillMaxWidth().clickable{systemId=system.id;choosing=false}.testTag("gamertag-system-${system.id}").padding(16.dp)){
                            Text(system.name);Text(system.platform+system.serialNumber.takeIf{it.isNotBlank()}?.let{" · Serie: $it"}.orEmpty(),style=MaterialTheme.typography.bodySmall)
                        }
                    }
                }else if(target=="Juego"){
                    val matches=games.filter{(it.title+" "+it.platform).contains(query,true)}
                    if(matches.isEmpty())item{Text("No hay coincidencias.",Modifier.padding(16.dp))}
                    items(matches,key={it.id}){game->Column(Modifier.fillMaxWidth().clickable{gameId=game.id;choosing=false}.testTag("gamertag-game-${game.id}").padding(16.dp)){
                        Text(game.title);Text("${game.platform} · ${game.effectiveEdition.label()}",style=MaterialTheme.typography.bodySmall)
                    }}
                }else{
                    val matches=games.map{it.platform}.distinct().sorted().filter{it.contains(query,true)}
                    if(matches.isEmpty())item{Text("No hay coincidencias.",Modifier.padding(16.dp))}
                    items(matches){name->Text(name,Modifier.fillMaxWidth().clickable{platform=name;choosing=false}.padding(16.dp))}
                }
            }
        }
    }
}
