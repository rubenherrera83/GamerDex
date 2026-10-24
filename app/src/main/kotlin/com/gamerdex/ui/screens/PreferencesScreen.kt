package com.gamerdex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamerdex.ui.PreferencesViewModel
import com.gamerdex.ui.StartScreen
import com.gamerdex.ui.components.ScreenHeader
import com.gamerdex.ui.theme.*
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable fun PreferencesScreen(menu: () -> Unit, viewModel: PreferencesViewModel = hiltViewModel()) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val alias by viewModel.alias.collectAsStateWithLifecycle()
    val startScreen by viewModel.startScreen.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val list=rememberLazyListState()
    val scope=rememberCoroutineScope()
    val focus=LocalFocusManager.current
    val keyboard=LocalSoftwareKeyboardController.current
    fun jump(index:Int){focus.clearFocus();keyboard?.hide();scope.launch{list.animateScrollToItem(index)}}
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { it?.let(viewModel::export) }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(viewModel::inspect) }
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, topBar = {
        Surface { Column {
            ScreenHeader("Preferencias",menu=menu)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=8.dp)) {
                TextButton({jump(0)},modifier=Modifier.testTag("preferences-jump-identity")){Text("Identidad")}
                TextButton({jump(3)},modifier=Modifier.testTag("preferences-jump-appearance")){Text("Apariencia")}
                TextButton({jump(2)},modifier=Modifier.testTag("preferences-jump-backup")){Text("Copias")}
            }
        }}
    }) { padding ->
        LazyColumn(state=list,modifier=Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("preferences-list"),
            contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            item(key="identity") { PreferencePanel("Tu identidad") {
                OutlinedTextField(alias,viewModel::setAlias,label={Text("Apodo predeterminado")},
                    supportingText={Text("Se usará en tus gamertags. Puedes cambiarlo en cada tarjeta.")},
                    singleLine=true,modifier=Modifier.fillMaxWidth())
                Text("Los cambios se guardan automáticamente.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }}
            item(key="start") { PreferencePanel("Pantalla inicial") {
                Text("Elige dónde empezar una sesión nueva. Al volver a una sesión abierta se conserva su navegación.",style=MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    StartScreen.entries.forEach{screen->FilterChip(startScreen==screen,{viewModel.setStartScreen(screen)},
                        label={Text(screen.title)},modifier=Modifier.testTag("start-screen-${screen.name}"),enabled=!state.busy)}
                }
            }}
            item(key="backup") { PreferencePanel("Copias de seguridad") {
                Text("Guarda tu colección en un ZIP y recupérala en otro dispositivo.",style=MaterialTheme.typography.bodyMedium)
                Button(onClick={export.launch("GamerDex-${System.currentTimeMillis()}.zip")},enabled=!state.busy,
                    modifier=Modifier.fillMaxWidth().testTag("backup-export")){Text("Crear copia de seguridad")}
                OutlinedButton(onClick={restore.launch(arrayOf("application/zip","application/octet-stream","application/x-zip-compressed"))},enabled=!state.busy,
                    modifier=Modifier.fillMaxWidth().testTag("backup-import")){Text("Recuperar copia de seguridad")}
                if(state.busy){LinearProgressIndicator(Modifier.fillMaxWidth());Text("Procesando archivos…")}
                state.message?.let{message->Text(message,modifier=Modifier.semantics{liveRegion=LiveRegionMode.Polite},color=MaterialTheme.colorScheme.primary)}
                Text("Incluye juegos, sistemas, mandos, compras, notas y fotos locales. No incluye claves de API ni preferencias.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Al recuperar una copia se sustituirán la colección y el historial actuales. Antes podrás revisar el contenido y confirmar.",style=MaterialTheme.typography.bodySmall)
            }}
            item(key="appearance") { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Text("Apariencia",style=MaterialTheme.typography.titleLarge)
                Text("Actual: ${theme.title} · ${VisualTheme.entries.size} temas",style=MaterialTheme.typography.bodyMedium)
                Text("Toca un tema para aplicarlo al instante.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }}
            VisualTheme.entries.forEach { candidate -> item(key="theme-${candidate.name}") {
                val p=candidate.palette()
                Surface(shape=MaterialTheme.shapes.medium,
                    border=androidx.compose.foundation.BorderStroke(if(theme==candidate)2.dp else 1.dp,if(theme==candidate)p.primary else MaterialTheme.colorScheme.outline),
                    color=p.surface,contentColor=p.text,modifier=Modifier.fillMaxWidth().testTag("theme-${candidate.name}")
                        .selectable(selected=theme==candidate,enabled=!state.busy,role=Role.RadioButton){viewModel.select(candidate)}) {
                    Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f)) { Text(candidate.title,style=MaterialTheme.typography.titleMedium);Text(candidate.tagline,style=MaterialTheme.typography.bodySmall) }
                        listOf(p.primary,p.secondary,p.tertiary).forEach{color->Box(Modifier.size(12.dp).background(color))}
                        if(theme==candidate)Text("✓",color=p.primary)
                    }
                }
            }}
        }
    }
    state.pending?.let { pending ->
        AlertDialog(onDismissRequest={if(!state.busy)viewModel.cancelRestore()},title={Text("¿Recuperar esta colección?")},
            text={Text("${pending.titles} juegos · ${pending.systems} sistemas · ${pending.purchases} compras · ${pending.photos} fotos\n${DateFormat.getDateTimeInstance().format(Date(pending.createdAt))}\n\nSe sustituirán la colección y el historial. Las copias antiguas no contienen compras de fichas eliminadas; las anteriores a v4 tampoco contienen sistemas. Puedes cancelar y crear primero una copia de tus datos.")},
            confirmButton={TextButton(onClick=viewModel::restore,enabled=!state.busy){Text("Restaurar colección")}},
            dismissButton={TextButton(onClick=viewModel::cancelRestore,enabled=!state.busy){Text("Cancelar")}})
    }
}

@Composable private fun PreferencePanel(title:String,content:@Composable ColumnScope.()->Unit){
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(title,style=MaterialTheme.typography.titleMedium,modifier=Modifier.semantics{heading()})
            content()
        }
    }
}