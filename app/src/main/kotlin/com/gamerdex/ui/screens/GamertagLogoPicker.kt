package com.gamerdex.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*
import com.gamerdex.gamertag.*
import com.gamerdex.ui.components.*

@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable fun GamertagLogoPicker(games:List<Game>,hardware:List<Hardware>,selected:List<GamertagLogo>,change:(List<GamertagLogo>)->Unit,dismiss:()->Unit) {
    var category by rememberSaveable { mutableStateOf("Consolas") }
    var query by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var photo by rememberSaveable { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val context=LocalContext.current
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if(uri!=null) try { context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);photo=uri.toString();error=null }
        catch(_:SecurityException){error="No se pudo conservar el permiso de la imagen. Selecciónala de nuevo."}
    }
    val candidates=remember(category,games,hardware) {
        when(category) {
            "Consolas" -> (hardware.map{it.platform}+games.map{it.platform}+PlatformCatalog.names).distinct().sorted().map { platform ->
                GamertagLogo(platform.take(80),(hardware.firstOrNull{it.platform==platform}?.logoUrl ?: games.firstOrNull{it.platform==platform}?.platformLogoUrl)?.takeIf(::isGamertagImage),GamertagLogoKind.PLATFORM)
            }
            "Compañías" -> (games.map{it.company}+listOf("SEGA","Nintendo","Atari")).distinct().sorted().map { company -> GamertagLogo(company.take(80),games.firstOrNull{it.company==company}?.companyLogoUrl?.takeIf(::isGamertagImage),GamertagLogoKind.COMPANY) }
            "Juegos" -> games.distinctBy { it.title to it.coverUrl }.map { GamertagLogo(it.title.take(80),it.coverUrl?.takeIf(::isGamertagImage),GamertagLogoKind.GAME) }
            else -> emptyList()
        }
    }
    ModalBottomSheet(onDismissRequest=dismiss,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        LazyColumn(Modifier.fillMaxWidth(),contentPadding=PaddingValues(20.dp,8.dp,20.dp,32.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { Text("Personalizar logotipos",style=MaterialTheme.typography.titleLarge);Text("Hasta 6 imágenes, en el orden que elijas. Se recuerdan para tus próximos gamertags.",style=MaterialTheme.typography.bodySmall) }
            item { FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Consolas","Compañías","Juegos","Personalizado").forEach{FilterChip(category==it,{category=it;query=""},label={Text(it)})}} }
            if(category=="Personalizado") {
                item { Text("Añade el logotipo de cualquier consola, compañía o juego. Un PNG transparente suele quedar mejor.") }
                item { OutlinedTextField(name,{name=it.take(80)},label={Text("Nombre del logotipo")},modifier=Modifier.fillMaxWidth()) }
                item { OutlinedTextField(url,{url=it.trim()},label={Text("URL HTTPS del logotipo")},modifier=Modifier.fillMaxWidth(),singleLine=true,isError=url.isNotBlank() && !isValidLogoUrl(url)) }
                item { OutlinedButton(onClick={picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))}){Text("Elegir imagen del logotipo")} }
                photo?.let { source -> item { Row { Cover(source,"Imagen del logotipo",Modifier.size(72.dp),contentScale=androidx.compose.ui.layout.ContentScale.Fit);TextButton(onClick={photo=null}){Text("Quitar imagen elegida")} } } }
                error?.let { item { Text(it,color=MaterialTheme.colorScheme.error) } }
                item {
                    val source=photo ?: url.ifBlank{null}
                    val valid=name.isNotBlank() && source!=null && isGamertagImage(source) && selected.size<6
                    Button(onClick={change(selected+GamertagLogo(name.trim(),source));name="";url="";photo=null},enabled=valid){Text("Añadir logotipo personalizado")}
                }
            } else {
                item { OutlinedTextField(query,{query=it},label={Text("Buscar logotipo")},modifier=Modifier.fillMaxWidth(),singleLine=true) }
                if(category=="Juegos") item { Text("Aquí se usan las carátulas disponibles. Para el logotipo del título, añade tu propia imagen en Personalizado.",style=MaterialTheme.typography.bodySmall) }
                if(category=="Compañías") item { Text("Se usa el logotipo guardado, el emblema local cuando exista o una insignia de texto. También puedes añadir uno propio.",style=MaterialTheme.typography.bodySmall) }
                items(candidates.filter{it.name.contains(query,true)}) { logo ->
                    val checked=logo in selected
                    Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        if(logo.kind==GamertagLogoKind.PLATFORM) BrandLogo(logo.name,logo.image,Modifier.size(42.dp),platform=true)
                        else if(logo.image!=null) Cover(logo.image,logo.name,Modifier.size(42.dp),contentScale=androidx.compose.ui.layout.ContentScale.Fit)
                        Text(logo.name,Modifier.weight(1f))
                        TextButton(onClick={change(if(checked)selected-logo else selected+logo)},enabled=checked || selected.size<6){Text(if(checked) "Quitar" else "Añadir")}
                    }
                }
            }
            item { TextButton(onClick=dismiss,modifier=Modifier.fillMaxWidth()){Text("Listo") } }
        }
    }
}
