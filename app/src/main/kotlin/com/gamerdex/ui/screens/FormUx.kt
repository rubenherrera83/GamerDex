package com.gamerdex.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

internal const val UNSPECIFIED_COMPANY="Sin especificar"

@Composable internal fun rememberFormExit(dirty:Boolean,busy:Boolean,valid:Boolean,problem:String?,save:()->Unit,back:()->Unit):()->Unit {
    var asking by rememberSaveable{mutableStateOf(false)}
    fun request(){if(!busy){if(dirty)asking=true else back()}}
    BackHandler(enabled=dirty || busy){request()}
    if(asking)AlertDialog(onDismissRequest={if(!busy)asking=false},title={Text("¿Guardar los cambios?")},
        text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text(if(valid)"Tienes cambios sin guardar." else "Tienes cambios sin guardar. ${problem.orEmpty()}")
            Button(onClick={asking=false;save()},enabled=valid && !busy,modifier=Modifier.fillMaxWidth()){Text("Guardar cambios")}
            OutlinedButton(onClick={asking=false},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Seguir editando")}
            TextButton(onClick={asking=false;back()},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Descartar cambios",color=MaterialTheme.colorScheme.error)}
        }},confirmButton={})
    return {request()}
}

@Composable internal fun EditorSaveBar(label:String,busy:Boolean,valid:Boolean,problem:String?,save:()->Unit) {
    Surface(tonalElevation=3.dp){Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal=20.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
        if(problem!=null)Text(problem,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick=save,enabled=valid && !busy,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(if(busy)"Guardando…" else label)}
    }}
}

@Composable internal fun EditorSection(id:String,title:String,summary:String,initiallyOpen:Boolean=false,content:@Composable ColumnScope.()->Unit) {
    var expanded by rememberSaveable{mutableStateOf(initiallyOpen)}
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        OutlinedCard(onClick={expanded=!expanded},modifier=Modifier.fillMaxWidth().testTag("editor-section-$id").semantics{stateDescription=if(expanded)"Expandido" else "Contraído"}){
            Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){
                Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium);Text(summary,style=MaterialTheme.typography.bodySmall)}
                Text(if(expanded)"−" else "+",style=MaterialTheme.typography.titleLarge)
            }
        }
        if(expanded)Column(verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun PurchaseDateField(value:String,change:(String)->Unit,enabled:Boolean=true) {
    var calendar by rememberSaveable{mutableStateOf(false)}
    val valid=value.isBlank() || com.gamerdex.core.domain.validPurchaseDate(value)
    OutlinedTextField(value,change,label={Text("Fecha de compra (AAAA-MM-DD)")},singleLine=true,enabled=enabled,isError=!valid,
        supportingText={Text(if(valid)"Opcional. Déjala vacía si no la sabes." else "Usa una fecha válida: AAAA-MM-DD.")},modifier=Modifier.fillMaxWidth(),
        trailingIcon={TextButton(onClick={calendar=true},enabled=enabled){Text("Calendario")}})
    if(calendar){
        val initial=remember(value){runCatching{LocalDate.parse(value).takeIf{it.year in 1900..2100}?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()}.getOrNull()}
        val state=rememberDatePickerState(initialSelectedDateMillis=initial)
        DatePickerDialog(onDismissRequest={calendar=false},confirmButton={TextButton(onClick={state.selectedDateMillis?.let{change(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString())};calendar=false},enabled=state.selectedDateMillis!=null){Text("Elegir fecha")}},dismissButton={TextButton(onClick={calendar=false}){Text("Cancelar")}}){DatePicker(state)}
    }
}
