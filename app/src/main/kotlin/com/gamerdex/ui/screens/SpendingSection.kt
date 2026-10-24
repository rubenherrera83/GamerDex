package com.gamerdex.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SpendingScreen(games:List<Game>,hardware:List<Hardware>,entries:List<Purchase>,removeArchived:(String)->Unit,back:()->Unit){
    var mode by rememberSaveable{mutableStateOf(SpendGrouping.YEAR)}
    var year by rememberSaveable{mutableStateOf<String?>(null)}
    var platform by rememberSaveable{mutableStateOf<String?>(null)}
    var chartCurrency by rememberSaveable{mutableStateOf<String?>(null)}
    var removingKey by rememberSaveable{mutableStateOf<String?>(null)}
    var expandedGroups by rememberSaveable{mutableStateOf(emptyList<String>())}
    val filtered=remember(entries,year,platform){filterPurchases(entries,year,platform)}
    val archived=remember(entries,games,hardware){val g=games.map{it.id}.toSet();val h=hardware.map{it.id}.toSet();entries.filter{if(it.kind==PurchaseKind.GAME)it.sourceId !in g else it.sourceId !in h}}
    val archivedKeys=remember(archived){archived.mapTo(mutableSetOf()){it.key}}
    val selected=remember(filtered,mode,archivedKeys){if(mode==SpendGrouping.ARCHIVED)filtered.filter{it.key in archivedKeys}else filtered}
    val totals=remember(selected){spendingTotals(selected)}
    val groups=remember(filtered,hardware,mode){spendingGroups(filtered,hardware,mode)}
    val years=remember(entries){entries.map{it.purchasedOn?.take(4) ?: "Año desconocido"}.distinct().sortedByDescending{it.toIntOrNull() ?: Int.MIN_VALUE}}
    val platforms=remember(entries){entries.map{it.platform}.distinct().sorted()}
    val currencies=totals.combined.keys.toList()
    val currency=chartCurrency?.takeIf{it in currencies} ?: currencies.firstOrNull()
    val savedModes=rememberSaveableStateHolder()
    BackHandler(onBack=back)
    Scaffold(containerColor=androidx.compose.ui.graphics.Color.Transparent,topBar={Surface{Column{
        TopAppBar(title={Text("Gasto histórico")},navigationIcon={TextButton(back){Text("Volver")}})
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            SpendingFilter("Año",year,"Todos los años",years,Modifier.weight(1f).fillMaxHeight()){year=it}
            SpendingFilter("Plataforma",platform,"Todas las plataformas",platforms,Modifier.weight(1f).fillMaxHeight()){platform=it}
        }
        if(year!=null || platform!=null)TextButton(onClick={year=null;platform=null},modifier=Modifier.padding(horizontal=8.dp)){Text("Limpiar filtros de gasto")}
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            SpendGrouping.entries.forEach{candidate->FilterChip(mode==candidate,{mode=candidate},label={Text(candidate.label)},modifier=Modifier.testTag("spending-mode-${candidate.name}"))}
        }
    }}}){padding->
        savedModes.SaveableStateProvider(mode.name){
            val listState=rememberLazyListState()
            var appliedYear by rememberSaveable{mutableStateOf(year)}
            var appliedPlatform by rememberSaveable{mutableStateOf(platform)}
            LaunchedEffect(year,platform){if(appliedYear!=year || appliedPlatform!=platform){listState.scrollToItem(0);appliedYear=year;appliedPlatform=platform}}
            LazyColumn(state=listState,modifier=Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("spending-list"),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
                item(key="summary"){
                    OutlinedCard(Modifier.fillMaxWidth().testTag("spending-selection-summary")){
                        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                            Text("Total de la selección",style=MaterialTheme.typography.titleLarge)
                            Text("${year ?: "Todos los años"} · ${platform ?: "Todas las plataformas"}",style=MaterialTheme.typography.bodyMedium)
                            Text("${selected.size} ${if(selected.size==1)"compra" else "compras"} ${if(mode==SpendGrouping.ARCHIVED){if(selected.size==1)"archivada" else "archivadas"}else{if(selected.size==1)"registrada" else "registradas"}}",style=MaterialTheme.typography.labelLarge)
                            SpendingAmounts(totals,"spending-selected")
                            if(selected.isEmpty())Text("Sin compras para estos filtros.")
                        }
                    }
                }
                if(mode!=SpendGrouping.ARCHIVED && currency!=null)item(key="chart"){SpendingChart(groups,mode,currency,{chartCurrency=it},currencies)}
                if(mode==SpendGrouping.SYSTEM)item{Text("Los juegos cuentan en una unidad solo si los vinculas a ella al editar su ficha.",style=MaterialTheme.typography.bodySmall)}
                if(mode==SpendGrouping.ARCHIVED){
                    if(selected.isEmpty())item{Text("No hay compras de fichas eliminadas en esta selección.")}
                    items(selected,key={it.key}){purchase->
                        OutlinedCard(Modifier.fillMaxWidth().testTag("archived-${purchase.key}")){
                            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                                Text(purchase.name,style=MaterialTheme.typography.titleMedium)
                                Text("${purchase.platform} · ${purchase.purchasedOn ?: "Año desconocido"}")
                                Text(purchase.amount?.let{"${formatPrice(it,purchase.currency)} ${purchase.currency}"} ?: "Sin precio")
                                TextButton(onClick={removingKey=purchase.key}){Text("Quitar del historial")}
                            }
                        }
                    }
                }else{
                    item{Text("Desglose · ${groups.size} grupos",style=MaterialTheme.typography.titleMedium)}
                    if(groups.isEmpty())item{Text("Todavía no hay compras registradas para esta selección.")}
                    items(groups,key={it.key}){group->SpendingGroupCard(group,mode,group.key in expandedGroups){expandedGroups=if(group.key in expandedGroups)expandedGroups.filterNot{it==group.key}else expandedGroups+group.key}}
                }
                item{
                    OutlinedCard(Modifier.fillMaxWidth()){
                        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                            Text("Cómo se calcula",style=MaterialTheme.typography.titleMedium)
                            Text("Solo se suman compras con precio registrado, sin convertir monedas. Las compras sin fecha permanecen en Año desconocido.",style=MaterialTheme.typography.bodySmall)
                            Text("Eliminar una ficha conserva su compra en el historial. Desde Archivadas puedes retirar esas compras; esto no modifica tu inventario actual.",style=MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
    val pending=archived.firstOrNull{it.key==removingKey}
    LaunchedEffect(removingKey,pending){if(removingKey!=null && pending==null)removingKey=null}
    pending?.let{purchase->AlertDialog(onDismissRequest={removingKey=null},title={Text("¿Quitar esta compra archivada?")},text={Text("Se retirará ${purchase.name} del historial de gasto. No se modifica la colección actual.")},
        confirmButton={TextButton(onClick={removingKey=null;removeArchived(purchase.key)}){Text("Quitar compra")}},dismissButton={TextButton(onClick={removingKey=null}){Text("Cancelar")}})}
}

@Composable private fun SpendingGroupCard(group:SpendGroup,mode:SpendGrouping,expanded:Boolean,toggle:()->Unit){
    OutlinedCard(Modifier.fillMaxWidth().testTag("spending-group-${group.key}")){
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text(group.name,style=MaterialTheme.typography.titleLarge)
            Text("${group.purchases.size} ${if(group.purchases.size==1)"compra" else "compras"}",style=MaterialTheme.typography.labelMedium)
            SpendingAmounts(spendingTotals(group.purchases),"group-${group.key}")
            if(mode!=SpendGrouping.YEAR){
                TextButton(onClick=toggle,modifier=Modifier.testTag("spending-expand-${group.key}")){Text(if(expanded)"Ocultar desglose anual" else "Ver desglose anual")}
                if(expanded)spendingByYear(group.purchases).forEach{(year,total)->
                    HorizontalDivider();Text(year,style=MaterialTheme.typography.titleMedium)
                    SpendingAmounts(total,"group-${group.key}-$year")
                }
            }
        }
    }
}
