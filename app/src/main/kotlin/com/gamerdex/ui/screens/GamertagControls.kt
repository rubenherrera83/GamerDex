package com.gamerdex.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gamerdex.ui.theme.*

@Composable internal fun GamertagSection(title:String,content:@Composable ColumnScope.()->Unit){
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(title,style=MaterialTheme.typography.titleMedium,modifier=Modifier.semantics{heading()})
            content()
        }
    }
}

@Composable internal fun GamertagPreview(bitmap:Bitmap?,updating:Boolean,failed:Boolean,open:()->Unit,retry:()->Unit){
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text("Tu gamertag",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
            TextButton(open,enabled=bitmap!=null && !updating){Text("Ampliar")}
        }
        if(bitmap!=null)Image(bitmap.asImageBitmap(),contentDescription="Vista previa del gamertag",
            modifier=Modifier.fillMaxWidth().aspectRatio(16f/9f).clickable(enabled=!updating,onClickLabel="Ampliar vista previa",onClick=open))
        else Box(Modifier.fillMaxWidth().aspectRatio(16f/9f).background(MaterialTheme.colorScheme.surface),contentAlignment=Alignment.Center){
            Text(if(updating)"Generando vista previa…" else "Vista previa no disponible",style=MaterialTheme.typography.bodyMedium)
        }
        if(updating)LinearProgressIndicator(Modifier.fillMaxWidth().testTag("gamertag-rendering"))
        if(failed)TextButton(retry){Text("Actualizar vista previa")}
        Text("PNG · 1600 × 900",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable internal fun GamertagActions(ready:Boolean,busy:Boolean,message:String?,problem:String?,share:()->Unit,save:()->Unit){
    Surface(tonalElevation=3.dp) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal=16.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            (message ?: problem)?.let{Text(it,style=MaterialTheme.typography.bodySmall,modifier=Modifier.semantics{liveRegion=LiveRegionMode.Polite})}
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Button(share,enabled=ready && !busy,modifier=Modifier.weight(1f).fillMaxHeight().heightIn(min=48.dp).testTag("gamertag-share").semantics{contentDescription="Compartir gamertag"}){Text(if(busy)"Preparando…" else "Compartir")}
                OutlinedButton(save,enabled=ready && !busy,modifier=Modifier.weight(1f).fillMaxHeight().heightIn(min=48.dp).testTag("gamertag-save").semantics{contentDescription="Guardar gamertag como imagen"}){Text("Guardar PNG")}
            }
        }
    }
}

@Composable internal fun GamertagDesign(appTheme:VisualTheme,custom:VisualTheme?,choose:(VisualTheme?)->Unit){
    var expanded by rememberSaveable{mutableStateOf(false)}
    GamertagSection("Diseño") {
        Text("Diseño: ${(custom ?: appTheme).title}",modifier=Modifier.testTag("gamertag-current-design"))
        Text("Puedes cambiar el diseño de esta tarjeta sin cambiar el tema de la app.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        FilterChip(custom==null,{choose(null)},label={Text("Usar tema de la app")},modifier=Modifier.testTag("gamertag-follow-theme"))
        Box {
            OutlinedButton({expanded=true},modifier=Modifier.testTag("gamertag-style-picker")){Text("Elegir diseño")}
            DropdownMenu(expanded,{expanded=false},modifier=Modifier.heightIn(max=360.dp)) {
                VisualTheme.entries.forEach{candidate->DropdownMenuItem(text={Text(candidate.title)},
                    leadingIcon={Box(Modifier.size(16.dp).background(candidate.palette().primary))},
                    trailingIcon={if(candidate==(custom ?: appTheme))Text("✓")},
                    onClick={choose(candidate);expanded=false},modifier=Modifier.testTag("gamertag-design-${candidate.name}"))}
            }
        }
    }
}

@Composable internal fun GamertagFullPreview(bitmap:Bitmap?,close:()->Unit){
    Dialog(onDismissRequest=close,properties=DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)) {
        Surface(modifier=Modifier.fillMaxSize().testTag("gamertag-full-preview")) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
                TextButton(close,modifier=Modifier.align(Alignment.End)){Text("Cerrar vista previa")}
                if(bitmap!=null)Image(bitmap.asImageBitmap(),contentDescription="Gamertag ampliado",modifier=Modifier.fillMaxWidth().weight(1f))
            }
        }
    }
}
