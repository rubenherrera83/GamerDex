package com.gamerdex.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*
import com.gamerdex.ui.components.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal

fun HardwareKind.label() = when(this) { HardwareKind.CONSOLE -> "Consola"; HardwareKind.COMPUTER -> "PC / ordenador"; HardwareKind.MICROCOMPUTER -> "Microordenador"; HardwareKind.HANDHELD -> "Portátil" }

@Composable fun HardwareImage(item: Hardware, modifier: Modifier = Modifier) {
    val photo = item.localPhotos.firstOrNull() ?: item.imageUrl
    if (photo != null) Cover(photo, "Imagen de ${item.name}", modifier, contentScale = androidx.compose.ui.layout.ContentScale.Fit)
    else {
        val accent = MaterialTheme.colorScheme.primary
        val panel = MaterialTheme.colorScheme.surfaceVariant
        Column(modifier.background(MaterialTheme.colorScheme.surface), horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.weight(1f).fillMaxWidth().padding(20.dp)) {
                val w = size.width; val h = size.height
                if (item.kind == HardwareKind.HANDHELD) {
                    drawRoundRect(panel, Offset(w*.15f, h*.1f), Size(w*.7f, h*.8f), CornerRadius(18f))
                    drawRect(accent.copy(alpha=.5f), Offset(w*.28f,h*.2f), Size(w*.44f,h*.36f))
                    drawLine(accent,Offset(w*.25f,h*.72f),Offset(w*.4f,h*.72f),8f)
                    drawLine(accent,Offset(w*.325f,h*.63f),Offset(w*.325f,h*.81f),8f)
                    drawCircle(accent,7f,Offset(w*.7f,h*.72f))
                } else if(item.kind != HardwareKind.CONSOLE) {
                    drawRoundRect(panel,Offset(w*.18f,h*.05f),Size(w*.64f,h*.55f),CornerRadius(10f))
                    drawRect(accent.copy(alpha=.4f),Offset(w*.23f,h*.1f),Size(w*.54f,h*.42f))
                    drawRoundRect(panel,Offset(w*.1f,h*.68f),Size(w*.8f,h*.26f),CornerRadius(8f))
                    repeat(10) { x -> repeat(3) { y -> drawRect(accent.copy(alpha=.6f),Offset(w*(.14f+x*.072f),h*(.72f+y*.06f)),Size(w*.05f,h*.035f)) } }
                } else {
                    drawRoundRect(panel,Offset(w*.08f,h*.24f),Size(w*.84f,h*.55f),CornerRadius(20f))
                    drawRoundRect(accent.copy(alpha=.8f),Offset(w*.08f,h*.24f),Size(w*.84f,h*.55f),CornerRadius(20f),style=Stroke(3f))
                    drawOval(accent.copy(alpha=.18f),Offset(w*.27f,h*.28f),Size(w*.46f,h*.32f))
                    drawLine(accent,Offset(w*.32f,h*.37f),Offset(w*.68f,h*.37f),6f)
                    drawCircle(accent,6f,Offset(w*.82f,h*.66f))
                    repeat(2) { drawRoundRect(accent.copy(alpha=.7f),Offset(w*(.24f+it*.24f),h*.68f),Size(w*.12f,h*.06f),CornerRadius(3f)) }
                }
            }
            Text("Ilustración genérica · añade tu foto", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom=8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun HardwareListScreen(hardware: List<Hardware>, menu: () -> Unit, add: () -> Unit, open: (Long) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    Scaffold(containerColor=Color.Transparent, topBar={ TopAppBar(title={Text("Mis sistemas")},navigationIcon={TextButton(onClick=menu){Text("☰")}}) },
        floatingActionButton={ExtendedFloatingActionButton(onClick=add){Text("+ AÑADIR SISTEMA")}}) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding=PaddingValues(20.dp,12.dp,20.dp,100.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
            item { Text("${hardware.size} sistemas · ${hardware.sumOf { h -> h.controllers.sumOf { it.quantity } }} accesorios / componentes", style=MaterialTheme.typography.titleMedium) }
            item { OutlinedTextField(query,{query=it},label={Text("Buscar en mis sistemas")},modifier=Modifier.fillMaxWidth()) }
            if(hardware.isEmpty()) item { Text("Registra cada consola u ordenador que conservas, con su caja, accesorios y datos de compra.") }
            items(hardware.filter { (it.name+" "+it.platform+" "+it.serialNumber).contains(query,true) },key={it.id}) { item ->
                OutlinedCard(onClick={open(item.id)},modifier=Modifier.fillMaxWidth()) {
                    HardwareImage(item,Modifier.fillMaxWidth().height(170.dp))
                    Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                        BrandLogo(item.platform,item.logoUrl,Modifier.size(44.dp),platform=true)
                        Column(Modifier.weight(1f)) { Text(item.name,style=MaterialTheme.typography.titleLarge); Text(item.platform); Text("${item.condition.label()} · ${item.controllers.sumOf { it.quantity }} accesorios / componentes",style=MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun HardwareDetailScreen(item: Hardware, back: () -> Unit, edit: () -> Unit, delete: () -> Unit, gamertag:()->Unit={}) {
    var confirm by remember { mutableStateOf(false) }
    Scaffold(containerColor=Color.Transparent,topBar={TopAppBar(title={Text("Mi sistema")},navigationIcon={TextButton(onClick=back){Text("Volver")}},actions={TextButton(onClick=edit){Text("Editar")}})}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            HardwareImage(item,Modifier.fillMaxWidth().height(240.dp))
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) { BrandLogo(item.platform,item.logoUrl,Modifier.size(60.dp),platform=true); Text(item.name,style=MaterialTheme.typography.headlineMedium) }
            Text(listOf(item.platform,item.kind.label(),item.region).filter(String::isNotBlank).joinToString(" · "))
            Text("Caja: ${if(item.hasBox) "Sí" else "No"} · Instrucciones: ${if(item.hasManual) "Sí" else "No"} · Cables: ${if(item.hasCables) "Sí" else "No"}")
            Text("Estado: ${item.condition.label()}")
            Text("Funcionamiento: ${when(item.working){true->"Funciona";false->"No funciona";null->"Sin comprobar"}}")
            Text("Número de serie: ${item.serialNumber.ifBlank { "No registrado" }}")
            Text("Fecha de compra: ${item.purchasedOn ?: "Desconocida"}")
            Text("Precio pagado: ${item.pricePaidCents?.let { formatPrice(it,item.currency) } ?: "Desconocido"}")
            OutlinedButton(onClick=gamertag,modifier=Modifier.fillMaxWidth()){Text("Crear gamertag de este sistema")}
            SectionLabel("02","Componentes y accesorios")
            if(item.controllers.isEmpty()) Text("Sin componentes ni accesorios registrados")
            item.controllers.forEach { c -> OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) { Text("${c.quantity} × ${c.name}",style=MaterialTheme.typography.titleMedium); Text(c.condition.label()); if(c.serialNumber.isNotBlank()) Text("Serie: ${c.serialNumber}"); if(c.notes.isNotBlank()) Text(c.notes) } } }
            if(item.notes.isNotBlank()) Text(item.notes)
            HardwareInsights(item.platform)
            item.localPhotos.drop(1).forEach { Cover(it,"Foto de ${item.name}",Modifier.fillMaxWidth().height(220.dp),contentScale=androidx.compose.ui.layout.ContentScale.Fit) }
            TextButton(onClick={confirm=true}){Text("Eliminar sistema",color=MaterialTheme.colorScheme.error)}
        }
    }
    if(confirm) AlertDialog(onDismissRequest={confirm=false},title={Text("¿Eliminar este sistema?")},text={Text("Se eliminará esta ficha y sus mandos. Los juegos y su compra en el historial de gasto se conservan. Las compras archivadas se pueden quitar desde Estadísticas.")},confirmButton={TextButton(onClick=delete){Text("Eliminar")}},dismissButton={TextButton(onClick={confirm=false}){Text("Cancelar")}})
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun HardwareEditorScreen(original: Hardware?, saving: Boolean, save: (Hardware) -> Unit, back: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(original?.name.orEmpty()) }; var platform by rememberSaveable { mutableStateOf(original?.platform.orEmpty()) }
    var kind by rememberSaveable { mutableStateOf(original?.kind ?: HardwareKind.CONSOLE) }; var region by rememberSaveable { mutableStateOf(original?.region.orEmpty()) }
    var box by rememberSaveable { mutableStateOf(original?.hasBox ?: false) }; var manual by rememberSaveable { mutableStateOf(original?.hasManual ?: false) }; var cables by rememberSaveable { mutableStateOf(original?.hasCables ?: false) }
    var condition by rememberSaveable { mutableStateOf(original?.condition ?: PhysicalCondition.UNSPECIFIED) }; var working by rememberSaveable { mutableStateOf(original?.working) }
    var serial by rememberSaveable { mutableStateOf(original?.serialNumber.orEmpty()) }; var date by rememberSaveable { mutableStateOf(original?.purchasedOn.orEmpty()) }
    var price by rememberSaveable { mutableStateOf(original?.pricePaidCents?.let { BigDecimal.valueOf(it,2).stripTrailingZeros().toPlainString() }.orEmpty()) }; var currency by rememberSaveable { mutableStateOf(original?.currency ?: "EUR") }
    var notes by rememberSaveable { mutableStateOf(original?.notes.orEmpty()) }; var imageUrl by rememberSaveable { mutableStateOf(original?.imageUrl.orEmpty()) }; var logoUrl by rememberSaveable { mutableStateOf(original?.logoUrl.orEmpty()) }
    var photos by rememberSaveable { mutableStateOf(original?.localPhotos ?: emptyList()) }
    var controllerJson by rememberSaveable { mutableStateOf(Json.encodeToString(original?.controllers ?: emptyList<Controller>())) }
    val controllers = remember(controllerJson) { Json.decodeFromString<List<Controller>>(controllerJson) }
    fun update(index: Int, value: Controller) { controllerJson = Json.encodeToString(controllers.toMutableList().apply { set(index,value) }) }
    var photoError by remember { mutableStateOf<String?>(null) }
    val context=LocalContext.current
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(8)) { uris ->
        try { uris.forEach { context.contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION) }; photos=(photos+uris.map{it.toString()}).distinct(); photoError=null }
        catch(_:SecurityException){photoError="No se pudo conservar el permiso de las fotos."}
    }
    val dateValid=date.isBlank() || validPurchaseDate(date)
    val priceValid=runCatching { parsePriceCents(price,currency) }.isSuccess
    val valid=name.isNotBlank() && platform.isNotBlank() && dateValid && priceValid && isValidLogoUrl(imageUrl.ifBlank{null}) && isValidLogoUrl(logoUrl.ifBlank{null})
    BackHandler(saving) { }
    Scaffold(containerColor=Color.Transparent,topBar={TopAppBar(title={Text(if(original==null) "Nuevo sistema" else "Editar sistema")},navigationIcon={TextButton(onClick=back,enabled=!saving){Text("Volver")}})}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name,{name=it},label={Text("Modelo / nombre *")},modifier=Modifier.fillMaxWidth(),enabled=!saving)
            PlatformPicker(platform,logoUrl.ifBlank{null},!saving){platform=it}
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { HardwareKind.entries.forEach { FilterChip(kind==it,{kind=it},label={Text(it.label())},enabled=!saving) } }
            OutlinedTextField(region,{region=it},label={Text("Región / variante (PAL, USA, Japón…)")},modifier=Modifier.fillMaxWidth(),enabled=!saving)
            HardwareToggle("Con caja",box,!saving){box=it}; HardwareToggle("Con instrucciones",manual,!saving){manual=it}; HardwareToggle("Con cables / fuente",cables,!saving){cables=it}
            Text("Estado físico",style=MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { PhysicalCondition.entries.forEach { FilterChip(condition==it,{condition=it},label={Text(it.label())},enabled=!saving) } }
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf(null to "Sin comprobar",true to "Funciona",false to "No funciona").forEach{(v,t)->FilterChip(working==v,{working=v},label={Text(t)},enabled=!saving)} }
            OutlinedTextField(serial,{serial=it},label={Text("Número de serie")},modifier=Modifier.fillMaxWidth(),enabled=!saving)
            OutlinedTextField(date,{date=it},label={Text("Fecha de compra (AAAA-MM-DD)")},supportingText={Text("Opcional; deja en blanco si no la sabes")},isError=!dateValid,modifier=Modifier.fillMaxWidth(),enabled=!saving)
            OutlinedTextField(price,{price=it},label={Text("Precio pagado")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),isError=!priceValid,modifier=Modifier.fillMaxWidth(),enabled=!saving)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { supportedCurrencies.forEach{FilterChip(currency==it,{currency=it},label={Text(it)},enabled=!saving)} }
            SectionLabel("02","Componentes y accesorios")
            Text("En un PC puedes registrar CPU, GPU, RAM, discos y periféricos. En consolas, mandos y otros accesorios.",style=MaterialTheme.typography.bodySmall)
            controllers.forEachIndexed { index,c -> OutlinedCard { Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(c.name,{if(it.isNotBlank())update(index,c.copy(name=it))},label={Text(if(kind==HardwareKind.COMPUTER || kind==HardwareKind.MICROCOMPUTER)"Componente ${index+1}: nombre / modelo" else "Mando ${index+1}: modelo / edición")},modifier=Modifier.fillMaxWidth(),enabled=!saving)
                Row(verticalAlignment=Alignment.CenterVertically) { Text("Cantidad: ${c.quantity}",Modifier.weight(1f)); TextButton(onClick={update(index,c.copy(quantity=c.quantity-1))},enabled=!saving && c.quantity>1){Text("−")}; TextButton(onClick={update(index,c.copy(quantity=c.quantity+1))},enabled=!saving && c.quantity<99){Text("+")} }
                FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) { PhysicalCondition.entries.forEach{FilterChip(c.condition==it,{update(index,c.copy(condition=it))},label={Text(it.label())},enabled=!saving)} }
                OutlinedTextField(c.serialNumber,{update(index,c.copy(serialNumber=it))},label={Text("Serie del mando / accesorio")},modifier=Modifier.fillMaxWidth(),enabled=!saving)
                OutlinedTextField(c.notes,{update(index,c.copy(notes=it))},label={Text("Detalles: especial, color, funcionamiento…")},modifier=Modifier.fillMaxWidth(),enabled=!saving)
                TextButton(onClick={controllerJson=Json.encodeToString(controllers.filterIndexed{i,_->i!=index})},enabled=!saving){Text("Quitar mando ${index+1}")}
            } } }
            OutlinedButton(onClick={controllerJson=Json.encodeToString(controllers+Controller(if(kind==HardwareKind.COMPUTER || kind==HardwareKind.MICROCOMPUTER)"Nuevo componente" else "Mando estándar"))},enabled=!saving && controllers.size<100){Text(if(kind==HardwareKind.COMPUTER || kind==HardwareKind.MICROCOMPUTER)"+ Añadir componente / accesorio" else "+ Añadir mando / accesorio")}
            SectionLabel("03","Imagen y notas")
            photos.forEachIndexed { i,p -> Row(verticalAlignment=Alignment.CenterVertically) { Cover(p,"Foto del sistema ${i+1}",Modifier.size(80.dp)); TextButton(onClick={photos=photos-p},enabled=!saving){Text("Quitar foto ${i+1}")} } }
            OutlinedButton(onClick={picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))},enabled=!saving){Text("Elegir fotos del sistema")}
            photoError?.let{Text(it,color=MaterialTheme.colorScheme.error)}
            OutlinedTextField(imageUrl,{imageUrl=it},label={Text("URL HTTPS de imagen (opcional)")},modifier=Modifier.fillMaxWidth(),enabled=!saving,isError=!isValidLogoUrl(imageUrl.ifBlank{null}))
            OutlinedTextField(logoUrl,{logoUrl=it},label={Text("URL HTTPS del logotipo (opcional)")},modifier=Modifier.fillMaxWidth(),enabled=!saving,isError=!isValidLogoUrl(logoUrl.ifBlank{null}))
            Text("La primera foto será la imagen principal. Sin foto se muestra una ilustración genérica y el logotipo del catálogo.",style=MaterialTheme.typography.bodySmall)
            OutlinedTextField(notes,{notes=it},label={Text("Notas y otros accesorios")},modifier=Modifier.fillMaxWidth(),enabled=!saving)
            Button(onClick={save(Hardware(original?.id ?: 0,name.trim(),platform.trim(),kind,region.trim(),box,manual,cables,condition,working,serial.trim(),date.ifBlank{null},parsePriceCents(price,currency),currency,controllers,photos,imageUrl.ifBlank{null},logoUrl.ifBlank{null},notes))},enabled=valid && !saving,modifier=Modifier.fillMaxWidth()){Text(if(saving) "Guardando…" else "GUARDAR SISTEMA")}
        }
    }
}
@Composable private fun HardwareToggle(label:String,value:Boolean,enabled:Boolean,change:(Boolean)->Unit) { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f)); Switch(value,change,enabled=enabled)} }
