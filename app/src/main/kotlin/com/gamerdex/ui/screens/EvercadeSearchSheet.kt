package com.gamerdex.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamerdex.core.domain.*
import com.gamerdex.ui.components.Cover
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class EvercadeState(val query:String="",val items:List<EvercadeCartridge> = emptyList(),val busy:Boolean=false,val error:String?=null)
@HiltViewModel class EvercadeViewModel @Inject constructor(private val repository:EvercadeRepository):ViewModel() {
    private val mutable=MutableStateFlow(EvercadeState());val state=mutable.asStateFlow();private var job:Job?=null
    fun search(query:String){job?.cancel();mutable.value=EvercadeState(query,busy=true);job=viewModelScope.launch {
        delay(250)
        try{mutable.value=EvercadeState(query,repository.search(query))}catch(e:CancellationException){throw e}catch(_:Exception){mutable.value=EvercadeState(query,error="No se pudo consultar el catálogo oficial. Puedes abrir su web o registrar el cartucho manualmente.")}
    }}
    fun choose(item:EvercadeCartridge,selected:(EvercadeCartridge)->Unit){job?.cancel();mutable.update{it.copy(busy=true)};job=viewModelScope.launch {
        try {val details=repository.details(item.slug);mutable.update{it.copy(busy=false)};selected(details)}catch(e:CancellationException){throw e}catch(_:Exception){mutable.update{it.copy(busy=false,error="No se pudieron consultar los juegos incluidos. Puedes registrar el cartucho manualmente.")}}
    }}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun EvercadeSearchSheet(selected:(EvercadeCartridge)->Unit,dismiss:()->Unit,viewModel:EvercadeViewModel=hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle();val uri=androidx.compose.ui.platform.LocalUriHandler.current
    LaunchedEffect(Unit){viewModel.search("")}
    ModalBottomSheet(onDismissRequest=dismiss,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("Catálogo Evercade",style=MaterialTheme.typography.titleLarge)
            Text("Busca cartuchos y recopilaciones oficiales. Cada cartucho se registra como una copia; sus títulos incluidos se anotan en la ficha.",style=MaterialTheme.typography.bodySmall)
            OutlinedTextField(state.query,viewModel::search,label={Text("Buscar cartucho o colección Evercade")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            if(state.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let{Text(it,color=MaterialTheme.colorScheme.error);TextButton(onClick={viewModel.search(state.query)}){Text("Reintentar catálogo")}}
            LazyColumn(Modifier.weight(1f,false).heightIn(max=420.dp)) {
                if(!state.busy && state.error==null && state.items.isEmpty())item{Text("No hay cartuchos con ese nombre.")}
                items(state.items,key={it.slug}) { cart->Row(Modifier.fillMaxWidth().clickable(enabled=!state.busy){viewModel.choose(cart,selected)}.padding(vertical=10.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){Cover(cart.cover,cart.title,Modifier.size(64.dp));Text(cart.title,Modifier.weight(1f))} }
            }
            TextButton(onClick={uri.openUri("https://evercade.co.uk/cartridges/")}){Text("Fuente: catálogo oficial Evercade ↗")}
        }
    }
}
