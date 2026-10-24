package com.gamerdex.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*
import com.gamerdex.ui.SearchState
import com.gamerdex.ui.components.Cover
import com.gamerdex.ui.components.PlatformPicker
import com.gamerdex.ui.components.CameraCaptureButton
import com.gamerdex.ui.components.DraftPhotos
import com.gamerdex.ui.components.activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

fun GameCondition.label() = when (this) {
    GameCondition.MINT -> "Impecable"
    GameCondition.VERY_GOOD -> "Muy bueno"
    GameCondition.LOOSE -> "Sin caja"
    GameCondition.DIGITAL -> "Digital"
    GameCondition.SEALED -> "Precintado"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun EditorScreen(original: Game?, saving: Boolean, searchState: SearchState, search: (String) -> Unit,
    choose: (Metadata, (Metadata) -> Unit) -> Unit, save: (Game, () -> Unit) -> Unit, back: () -> Unit, lookupBarcode: (String) -> Unit = {}, ownedHardware:List<Hardware> = emptyList(),initialPlatform:String="") {
    var title by rememberSaveable { mutableStateOf(original?.title.orEmpty()) }
    var platform by rememberSaveable { mutableStateOf(original?.platform ?: initialPlatform) }
    var company by rememberSaveable { mutableStateOf(original?.company?.takeUnless{it==UNSPECIFIED_COMPANY}.orEmpty()) }
    var year by rememberSaveable { mutableStateOf(original?.releaseYear?.toString().orEmpty()) }
    var cover by rememberSaveable { mutableStateOf(original?.coverUrl.orEmpty()) }
    var platformLogo by rememberSaveable { mutableStateOf(original?.platformLogoUrl.orEmpty()) }
    var companyLogo by rememberSaveable { mutableStateOf(original?.companyLogoUrl.orEmpty()) }
    var finished by rememberSaveable { mutableStateOf(original?.isFinished ?: false) }
    var completion by rememberSaveable { mutableStateOf((original?.completionPercent ?: 0).toString()) }
    var platinum by rememberSaveable { mutableStateOf(original?.hasPlatinum ?: false) }
    var favorite by rememberSaveable { mutableStateOf(original?.isFavorite ?: false) }
    var rawgId by rememberSaveable { mutableStateOf(original?.rawgId) }
    var purchasedOn by rememberSaveable{mutableStateOf(original?.purchasedOn.orEmpty())}
    var ownedSystemId by rememberSaveable{mutableStateOf(original?.ownedSystemId)}
    var catalogUrl by rememberSaveable{mutableStateOf(original?.catalogUrl)}
    var chooseSystem by rememberSaveable{mutableStateOf(false)}
    var showBarcode by rememberSaveable{mutableStateOf(false)}
    var scanImmediately by rememberSaveable{mutableStateOf(false)}
    var showEvercade by rememberSaveable{mutableStateOf(false)}
    var customizeLogos by rememberSaveable { mutableStateOf(false) }
    var copy by rememberSaveable { mutableStateOf(CopyDetailsState.from(original)) }
    var rating by rememberSaveable { mutableFloatStateOf(original?.rating ?: 0f) }
    var ratingInput by rememberSaveable{mutableStateOf(formatPersonalRating(original?.rating ?: 0f))}
    var rated by rememberSaveable { mutableStateOf(original?.rating != null) }
    var notes by rememberSaveable { mutableStateOf(original?.notes.orEmpty()) }
    var photos by rememberSaveable { mutableStateOf(original?.localCustomPhotos ?: emptyList()) }
    var cameraDrafts by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var committed by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val latestDrafts by rememberUpdatedState(cameraDrafts)
    DisposableEffect(Unit) {
        onDispose {
            if (!committed && context.activity()?.isChangingConfigurations != true) DraftPhotos.discard(context.applicationContext, latestDrafts)
        }
    }
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(12)) { uris ->
        scope.launch {
            importing = true
            photoError = null
            try {
                val persisted = withContext(Dispatchers.IO) { uris.map { uri ->
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    uri.toString()
                } }
                photos = (photos + persisted).distinct()
            } catch (e: SecurityException) { photoError = "No se pudo conservar el permiso de las fotos. Selecciónalas de nuevo." }
            finally { importing = false }
        }
    }
    val validYear = year.isBlank() || year.toIntOrNull()?.let { it in 1950..2100 } == true
    val validPlatformLogo = isValidLogoUrl(platformLogo.trim().ifBlank { null })
    val validCompanyLogo = isValidLogoUrl(companyLogo.trim().ifBlank { null })
    val validCompletion = completion.toIntOrNull()?.let { it in 0..100 } == true
    val validDate=purchasedOn.isBlank() || validPurchaseDate(purchasedOn)
    val validRating=!rated || ratingInput.replace(',','.').toFloatOrNull()?.let{it.isFinite() && it in 0f..100f}==true
    val problem=when {
        title.isBlank() || platform.isBlank()->"Completa el título y la plataforma para guardar."
        !validYear->"Revisa el año en Más datos: debe estar entre 1950 y 2100."
        !validPlatformLogo || !validCompanyLogo->"Revisa las URLs de logotipos en Más datos: deben usar HTTPS."
        !copy.valid->"Revisa el importe y la moneda en Compra."
        !validCompletion->"Revisa el porcentaje en Partida y valoración: debe estar entre 0 y 100."
        !validRating->"Revisa tu nota en Partida y valoración: debe estar entre 0 y 100."
        !validDate->"Revisa la fecha de compra: usa AAAA-MM-DD."
        else->null
    }
    val valid=problem==null
    val editable=!saving && !importing
    val initial=listOf(original?.title.orEmpty(),original?.platform ?: initialPlatform,original?.company?.takeUnless{it==UNSPECIFIED_COMPANY}.orEmpty(),original?.releaseYear?.toString().orEmpty(),original?.coverUrl.orEmpty(),original?.platformLogoUrl.orEmpty(),original?.companyLogoUrl.orEmpty(),original?.isFinished ?: false,(original?.completionPercent ?: 0).toString(),original?.hasPlatinum ?: false,original?.isFavorite ?: false,original?.rawgId,original?.purchasedOn.orEmpty(),original?.ownedSystemId,original?.catalogUrl,CopyDetailsState.from(original),original?.rating ?: 0f,original?.rating!=null,original?.notes.orEmpty(),original?.localCustomPhotos ?: emptyList<String>())
    val current=listOf(title,platform,company,year,cover,platformLogo,companyLogo,finished,completion,platinum,favorite,rawgId,purchasedOn,ownedSystemId,catalogUrl,copy,rating,rated,notes,photos)
    fun submit(){if(valid && editable)save(Game(id = original?.id ?: 0, title = title.trim(), platform = platform.trim(), company = company.trim().ifBlank{UNSPECIFIED_COMPANY},
        releaseYear = year.toIntOrNull(), coverUrl = cover.trim().ifBlank { null }, localCustomPhotos = photos,
        isSpecialEdition = copy.edition != EditionType.STANDARD, specialEditionDetails = copy.details.trim().ifBlank { null }.takeIf { copy.edition != EditionType.STANDARD },
        condition = when { copy.digital -> GameCondition.DIGITAL; copy.sealed -> GameCondition.SEALED; copy.physical == PhysicalCondition.MINT -> GameCondition.MINT; else -> GameCondition.VERY_GOOD },
        rating = rating.takeIf { rated }, notes = notes.trim().ifBlank { null }, createdAt = original?.createdAt ?: System.currentTimeMillis(),
        platformLogoUrl = platformLogo.trim().ifBlank { null }, companyLogoUrl = companyLogo.trim().ifBlank { null }, editionType = copy.edition,
        hasBox = copy.box && !copy.digital, hasManual = copy.manual && !copy.digital, hasMedia = copy.media && !copy.digital, physicalCondition = copy.physical,
        pricePaidCents = parsePriceCents(copy.price,copy.currency), priceCurrency = copy.currency, genres = copy.genres.split(',').map(String::trim).filter(String::isNotBlank).distinct(),
        isFinished = finished, completionPercent = completion.toInt(), hasPlatinum = platinum && supportsPlatinum(platform), isFavorite = favorite, rawgId = rawgId,purchasedOn=purchasedOn.ifBlank{null},ownedSystemId=ownedSystemId,catalogUrl=catalogUrl)){committed=true}}
    val requestBack=rememberFormExit(current!=initial || ratingInput!=formatPersonalRating(original?.rating ?: 0f),!editable,valid,problem,::submit,back)
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, topBar = { TopAppBar(title = { Text(if (original == null) "Añadir juego" else "Editar juego") }, navigationIcon = { TextButton(onClick = requestBack, enabled = editable) { Text("Volver") } }) },
        bottomBar={EditorSaveBar("GUARDAR EN LA BÓVEDA",!editable,valid,problem,::submit)}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Identifica el juego",style=MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { showSearch = true }, modifier = Modifier.fillMaxWidth(), enabled = editable) { Text("Buscar por nombre") }
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick={scanImmediately=true;showBarcode=true},enabled=!saving){Text("Escanear código de barras")}
            }
            OutlinedTextField(title, { title = it }, label = { Text("Título *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,enabled=editable)
            PlatformPicker(platform, platformLogo.ifBlank { null }, !saving) { if (it != platform) platformLogo = ""; platform = it }
            OutlinedTextField(company, { if (it != company) companyLogo = ""; company = it }, label = { Text("Compañía (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,enabled=editable)
            Text("Solo título y plataforma son obligatorios. Completa tu copia ahora o más adelante.",style=MaterialTheme.typography.bodySmall)
            EditorSection("identify","Más formas de identificar","Código manual y cartuchos Evercade"){
                OutlinedButton(onClick={scanImmediately=false;showBarcode=true},enabled=editable){Text("Introducir EAN / UPC")}
                OutlinedButton(onClick={showEvercade=true},enabled=editable){Text("Buscar cartucho Evercade")}
            }
            EditorSection("copy","Edición y estado","${copy.edition.label()} · ${if(copy.digital)"Digital" else if(copy.box && copy.manual && copy.media)"Caja, instrucciones y juego" else "Contenido incompleto"} · ${copy.physical.label()}",original!=null){CopyDetailsFields(copy,{copy=it},editable)}
            EditorSection("purchase","Compra","${if(copy.price.isBlank())"Sin precio" else "${copy.price} ${copy.currency}"} · ${purchasedOn.ifBlank{"Fecha desconocida"}}",original!=null){
            CopyPurchaseFields(copy,{copy=it},editable)
            PurchaseDateField(purchasedOn,{purchasedOn=it},editable)
            OutlinedButton(onClick={chooseSystem=true},modifier=Modifier.fillMaxWidth()) { Text("Sistema de mi inventario: ${ownedHardware.find{it.id==ownedSystemId}?.name ?: ownedSystemId?.let{"Unidad archivada #$it"} ?: "Sin vincular"}") }
            }
            EditorSection("metadata","Más datos","Año, géneros, portada y logotipos",original!=null){
            TextButton(onClick = { customizeLogos = !customizeLogos },enabled=editable) { Text(if (customizeLogos) "Ocultar logotipos" else "Personalizar logotipos") }
            if (customizeLogos) {
                OutlinedTextField(platformLogo, { platformLogo = it }, label = { Text("URL del logotipo de plataforma") }, isError = !validPlatformLogo, modifier = Modifier.fillMaxWidth(), singleLine = true,enabled=editable)
                OutlinedTextField(companyLogo, { companyLogo = it }, label = { Text("URL del logotipo de compañía") }, isError = !validCompanyLogo, modifier = Modifier.fillMaxWidth(), singleLine = true,enabled=editable)
                Text("Usa URLs HTTPS. Los logotipos se comparten con los juegos de la misma plataforma o compañía. Sin URL se mantienen las insignias o el logotipo existente.", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(year, { year = it }, label = { Text("Año de lanzamiento") }, isError = !validYear, supportingText = { if (!validYear) Text("Introduce un año entre 1950 y 2100") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,enabled=editable)
            OutlinedTextField(cover, { cover = it }, label = { Text("URL de portada (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,enabled=editable)
            OutlinedTextField(copy.genres,{copy=copy.copy(genres=it)},label={Text("Géneros (separados por comas)")},modifier=Modifier.fillMaxWidth(),enabled=editable)
            }
            EditorSection("progress","Partida y valoración","Favorito, finalizado, porcentaje, nota y platino",original!=null){
            ProgressToggle("★ Favorito", favorite, !saving) { favorite = it }
            ProgressToggle("Juego finalizado", finished, !saving) { finished = it }
            OutlinedTextField(completion, { completion = it.take(3) }, label = { Text("Porcentaje completado (0–100)") }, isError = !validCompletion,
                supportingText = { Text("Finalizar la historia y completar el 100 % se registran por separado.") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, enabled = !saving, modifier = Modifier.fillMaxWidth())
            if (supportsPlatinum(platform)) ProgressToggle("Trofeo platino conseguido", platinum, !saving) { platinum = it }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (rated) "Valoración: ${formatPersonalRating(rating)} / 100" else "Sin valoración"); Switch(rated, { rated = it }, modifier = Modifier.semantics { contentDescription = "Activar valoración personal" }) }
            if (rated) Slider(rating, { rating = it;ratingInput=formatPersonalRating(it) }, valueRange = 0f..100f, steps = 99,enabled=editable)
            if(rated)OutlinedTextField(ratingInput,{value->ratingInput=value;value.replace(',','.').toFloatOrNull()?.takeIf{it.isFinite() && it in 0f..100f}?.let{rating=it}},label={Text("Tu nota (0–100)")},isError=!validRating,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true,enabled=editable,modifier=Modifier.fillMaxWidth())
            }
            EditorSection("photos","Fotos y notas","Fotografías propias y recuerdos · ${photos.size} fotos",original!=null){
            OutlinedTextField(notes, { notes = it }, label = { Text("Notas y recuerdos") }, modifier = Modifier.fillMaxWidth(), minLines = 3,enabled=editable)
            Text("Tu copia física", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(photos, key = { it }) { uri ->
                Column { Cover(uri, "Foto de tu copia", Modifier.size(100.dp)); TextButton(onClick = {
                    photos = photos - uri
                    if (uri in cameraDrafts) { DraftPhotos.discard(context.applicationContext, listOf(uri)); cameraDrafts = cameraDrafts - uri }
                }, enabled = !saving && !importing) { Text("Quitar") } }
            } }
            OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !importing && !saving) { Text(if (importing) "Conservando fotos…" else "+ Elegir fotos") }
            CameraCaptureButton(enabled = !importing && !saving, onCaptured = { photos = photos + it; cameraDrafts = cameraDrafts + it })
            photoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            Text("* Campos obligatorios · Tu colección se guarda en este dispositivo.", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (showSearch) SearchSheet(searchState, search, { result -> choose(result) { metadata ->
        title = metadata.title; platform = metadata.platforms.singleOrNull() ?: platform.takeIf { p -> p.isNotBlank() && metadata.platforms.any { com.gamerdex.ui.components.PlatformCatalog.family(it) == com.gamerdex.ui.components.PlatformCatalog.family(p) } }.orEmpty(); company = metadata.company.orEmpty(); platformLogo = ""; companyLogo = ""
        rawgId = metadata.id
        catalogUrl=null
        year = metadata.year?.toString().orEmpty(); cover = metadata.coverUrl.orEmpty(); showSearch = false
        copy = copy.copy(genres = metadata.genres.joinToString(", "))
    } }, { showSearch = false; search("") }, lookupBarcode)
    if(showBarcode) BarcodeSearchDialog(scanImmediately,{showBarcode=false}){code->showBarcode=false;showSearch=true;lookupBarcode(code)}
    if(showEvercade) EvercadeSearchSheet({cart->
        title=cart.title;platform="Evercade";company="Blaze Entertainment";cover=cart.cover.orEmpty();year="";rawgId=null;catalogUrl=cart.url;platformLogo="";companyLogo=""
        notes=listOf(notes.takeIf{it.isNotBlank()},"Catálogo oficial Evercade: ${cart.url}",cart.games.takeIf{it.isNotEmpty()}?.joinToString("\n",prefix="Juegos incluidos:\n")).filterNotNull().joinToString("\n\n")
        copy=copy.copy(genres="Recopilación");showEvercade=false
    },{showEvercade=false})
    if(chooseSystem) AlertDialog(onDismissRequest={chooseSystem=false},title={Text("Vincular compra a un sistema")},text={androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max=400.dp)){
        item{TextButton(onClick={ownedSystemId=null;chooseSystem=false}){Text("Sin vincular")}}
        items(ownedHardware.size){i->val system=ownedHardware[i];val suffix=if(ownedHardware.count{it.name==system.name}>1) " · ${system.serialNumber.ifBlank{"#${system.id}"}}" else "";TextButton(onClick={ownedSystemId=system.id;chooseSystem=false}){Text("${system.name} · ${system.platform}$suffix")}}
    }},confirmButton={TextButton(onClick={chooseSystem=false}){Text("Cerrar")}})
}

@Composable private fun ProgressToggle(label: String, value: Boolean, enabled: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().toggleable(value, enabled = enabled, role = Role.Switch, onValueChange = change).padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f)); Switch(value, null, enabled = enabled)
    }
}
