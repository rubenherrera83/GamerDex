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
    choose: (Metadata, (Metadata) -> Unit) -> Unit, save: (Game, () -> Unit) -> Unit, back: () -> Unit, lookupBarcode: (String) -> Unit = {}) {
    var title by rememberSaveable { mutableStateOf(original?.title.orEmpty()) }
    var platform by rememberSaveable { mutableStateOf(original?.platform.orEmpty()) }
    var company by rememberSaveable { mutableStateOf(original?.company.orEmpty()) }
    var year by rememberSaveable { mutableStateOf(original?.releaseYear?.toString().orEmpty()) }
    var cover by rememberSaveable { mutableStateOf(original?.coverUrl.orEmpty()) }
    var platformLogo by rememberSaveable { mutableStateOf(original?.platformLogoUrl.orEmpty()) }
    var companyLogo by rememberSaveable { mutableStateOf(original?.companyLogoUrl.orEmpty()) }
    var finished by rememberSaveable { mutableStateOf(original?.isFinished ?: false) }
    var completion by rememberSaveable { mutableStateOf((original?.completionPercent ?: 0).toString()) }
    var platinum by rememberSaveable { mutableStateOf(original?.hasPlatinum ?: false) }
    var favorite by rememberSaveable { mutableStateOf(original?.isFavorite ?: false) }
    var rawgId by rememberSaveable { mutableStateOf(original?.rawgId) }
    var customizeLogos by rememberSaveable { mutableStateOf(false) }
    var copy by rememberSaveable { mutableStateOf(CopyDetailsState.from(original)) }
    var rating by rememberSaveable { mutableFloatStateOf(original?.rating ?: 0f) }
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
    BackHandler(enabled = saving) { /* Keep navigation stable until the transaction completes. */ }
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
    val valid = title.isNotBlank() && platform.isNotBlank() && company.isNotBlank() && validYear && validPlatformLogo && validCompanyLogo && copy.valid && validCompletion
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, topBar = { TopAppBar(title = { Text(if (original == null) "Nueva adquisición" else "Editar juego") }, navigationIcon = { TextButton(onClick = back, enabled = !saving) { Text("Volver") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { showSearch = true }, modifier = Modifier.fillMaxWidth(), enabled = !saving) { Text("⌕ Autocompletar con RAWG") }
            OutlinedTextField(title, { title = it }, label = { Text("Título *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            PlatformPicker(platform, platformLogo.ifBlank { null }, !saving) { if (it != platform) platformLogo = ""; platform = it }
            OutlinedTextField(company, { if (it != company) companyLogo = ""; company = it }, label = { Text("Compañía *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            TextButton(onClick = { customizeLogos = !customizeLogos }) { Text(if (customizeLogos) "Ocultar logotipos" else "Personalizar logotipos") }
            if (customizeLogos) {
                OutlinedTextField(platformLogo, { platformLogo = it }, label = { Text("URL del logotipo de plataforma") }, isError = !validPlatformLogo, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(companyLogo, { companyLogo = it }, label = { Text("URL del logotipo de compañía") }, isError = !validCompanyLogo, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Usa URLs HTTPS. Los logotipos se comparten con los juegos de la misma plataforma o compañía. Sin URL se mantienen las insignias o el logotipo existente.", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(year, { year = it }, label = { Text("Año de lanzamiento") }, isError = !validYear, supportingText = { if (!validYear) Text("Introduce un año entre 1950 y 2100") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            OutlinedTextField(cover, { cover = it }, label = { Text("URL de portada (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            CopyDetailsFields(copy, { copy = it }, enabled = !saving)
            Text("Tu partida", style = MaterialTheme.typography.titleLarge)
            ProgressToggle("★ Favorito", favorite, !saving) { favorite = it }
            ProgressToggle("Juego finalizado", finished, !saving) { finished = it }
            OutlinedTextField(completion, { completion = it.take(3) }, label = { Text("Porcentaje completado (0–100)") }, isError = !validCompletion,
                supportingText = { Text("Finalizar la historia y completar el 100 % se registran por separado.") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, enabled = !saving, modifier = Modifier.fillMaxWidth())
            if (supportsPlatinum(platform)) ProgressToggle("Trofeo platino conseguido", platinum, !saving) { platinum = it }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (rated) "Valoración: ${formatPersonalRating(rating)} / 100" else "Sin valoración"); Switch(rated, { rated = it }, modifier = Modifier.semantics { contentDescription = "Activar valoración personal" }) }
            if (rated) Slider(rating, { rating = it }, valueRange = 0f..100f, steps = 99)
            OutlinedTextField(notes, { notes = it }, label = { Text("Notas y recuerdos") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
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
            Button(onClick = { save(Game(id = original?.id ?: 0, title = title.trim(), platform = platform.trim(), company = company.trim(),
                releaseYear = year.toIntOrNull(), coverUrl = cover.trim().ifBlank { null }, localCustomPhotos = photos,
                isSpecialEdition = copy.edition != EditionType.STANDARD, specialEditionDetails = copy.details.trim().ifBlank { null }.takeIf { copy.edition != EditionType.STANDARD },
                condition = when { copy.digital -> GameCondition.DIGITAL; copy.sealed -> GameCondition.SEALED; copy.physical == PhysicalCondition.MINT -> GameCondition.MINT; else -> GameCondition.VERY_GOOD },
                rating = rating.takeIf { rated }, notes = notes.trim().ifBlank { null }, createdAt = original?.createdAt ?: System.currentTimeMillis(),
                platformLogoUrl = platformLogo.trim().ifBlank { null }, companyLogoUrl = companyLogo.trim().ifBlank { null }, editionType = copy.edition,
                hasBox = copy.box && !copy.digital, hasManual = copy.manual && !copy.digital, hasMedia = copy.media && !copy.digital, physicalCondition = copy.physical,
                pricePaidCents = parsePriceCents(copy.price), priceCurrency = copy.currency, genres = copy.genres.split(',').map(String::trim).filter(String::isNotBlank).distinct(),
                isFinished = finished, completionPercent = completion.toInt(), hasPlatinum = platinum && supportsPlatinum(platform), isFavorite = favorite, rawgId = rawgId)) { committed = true } },
                enabled = valid && !saving && !importing, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(if (saving) "Guardando…" else "GUARDAR EN LA BÓVEDA") }
            Text("* Campos obligatorios · Tu colección se guarda en este dispositivo.", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (showSearch) SearchSheet(searchState, search, { result -> choose(result) { metadata ->
        title = metadata.title; platform = metadata.platforms.singleOrNull() ?: platform.takeIf { p -> p.isNotBlank() && metadata.platforms.any { com.gamerdex.ui.components.PlatformCatalog.family(it) == com.gamerdex.ui.components.PlatformCatalog.family(p) } }.orEmpty(); company = metadata.company.orEmpty(); platformLogo = ""; companyLogo = ""
        rawgId = metadata.id
        year = metadata.year?.toString().orEmpty(); cover = metadata.coverUrl.orEmpty(); showSearch = false
        copy = copy.copy(genres = metadata.genres.joinToString(", "))
    } }, { showSearch = false; search("") }, lookupBarcode)
}

@Composable private fun ProgressToggle(label: String, value: Boolean, enabled: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().toggleable(value, enabled = enabled, role = Role.Switch, onValueChange = change).padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f)); Switch(value, null, enabled = enabled)
    }
}
