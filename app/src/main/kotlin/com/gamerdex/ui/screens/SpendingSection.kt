package com.gamerdex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*

private fun money(values:Map<String,Long>)=if(values.isEmpty()) "Sin importes registrados" else values.entries.joinToString(" · "){(currency,amount)->"${formatPrice(amount,currency)} $currency"}
@Composable private fun SpendAmounts(totals:SpendTotals) {
    Text("Videojuegos: ${money(totals.games)}",style=MaterialTheme.typography.bodyMedium)
    Text("Sistemas: ${money(totals.hardware)}",style=MaterialTheme.typography.bodyMedium)
    Text("Total por moneda: ${money(totals.combined)}",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.titleSmall)
    if(totals.withoutPrice>0)Text("${totals.withoutPrice} compras sin precio registrado",style=MaterialTheme.typography.bodySmall)
}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun SpendingSection(games:List<Game>,hardware:List<Hardware>,entries:List<Purchase>,removeArchived:(String)->Unit) {
    var show by rememberSaveable{mutableStateOf(false)};var mode by rememberSaveable{mutableStateOf("Por año")};var removing by remember{mutableStateOf<Purchase?>(null)}
    val archived=remember(entries,games,hardware){val g=games.map{it.id}.toSet();val h=hardware.map{it.id}.toSet();entries.filter{if(it.kind==PurchaseKind.GAME)it.sourceId !in g else it.sourceId !in h}}
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Gasto histórico registrado",style=MaterialTheme.typography.titleLarge)
        SpendAmounts(spendingTotals(entries))
        Text("Sin convertir monedas. Se conservan las compras de fichas eliminadas; no se reconstruyen las borradas antes de esta actualización. Sin fecha se agrupan en Año desconocido.",style=MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick={mode="Por año";show=true}){Text("Ver gasto por año, plataforma y sistema")}
    } }
    if(show) AlertDialog(onDismissRequest={show=false},title={Text("Historial de gasto")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Por año","Por plataforma","Por sistema","Archivadas").forEach{FilterChip(mode==it,{mode=it},label={Text(it)})}}
        if(mode=="Por sistema")Text("Los juegos cuentan en una unidad solo si los vinculas a ella al editar su ficha.",style=MaterialTheme.typography.bodySmall)
        val groups=remember(entries,hardware,mode) { when(mode) {
            "Por año"->entries.groupBy{it.purchasedOn?.take(4) ?: "Año desconocido"}.entries.sortedByDescending{it.key.toIntOrNull() ?: Int.MIN_VALUE}.map{it.key to it.value}
            "Por plataforma"->entries.groupBy{it.platform}.toSortedMap().entries.map{it.key to it.value}
            "Por sistema"->entries.groupBy{it.systemId}.entries.sortedBy{it.key ?: Long.MAX_VALUE}.map { (id,list)->
                val unit=hardware.find{it.id==id}
                val name=if(id==null) "Sin vincular (videojuegos)" else unit?.let{it.name+if(hardware.count{h->h.name==it.name}>1) " · ${it.serialNumber.ifBlank{"#$id"}}" else ""} ?: ((list.firstOrNull{it.kind==PurchaseKind.HARDWARE}?.systemName ?: list.firstOrNull()?.systemName ?: "Sistema #$id")+" · archivado #$id")
                name to list
            }
            else->emptyList()
        } }
        LazyColumn(Modifier.heightIn(max=430.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            if(mode=="Archivadas") {
                if(archived.isEmpty())item{Text("No hay compras de fichas eliminadas.")}
                items(archived,key={it.key}){p->OutlinedCard{Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Text(p.name,style=MaterialTheme.typography.titleMedium);Text("${p.platform} · ${p.purchasedOn ?: "Año desconocido"}")
                    Text(p.amount?.let{formatPrice(it,p.currency)+" "+p.currency} ?: "Sin precio")
                    TextButton(onClick={removing=p}){Text("Quitar del historial")}
                }}}
            }else {
                if(groups.isEmpty())item{Text("Todavía no hay compras registradas.")}
                items(groups){(name,list)->OutlinedCard{Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Text(name,style=MaterialTheme.typography.titleMedium);SpendAmounts(spendingTotals(list))
                    if(mode!="Por año")spendingByYear(list).forEach{(year,total)->HorizontalDivider();Text(year,style=MaterialTheme.typography.labelLarge);SpendAmounts(total)}
                }}}
            }
        }
    }},confirmButton={TextButton(onClick={show=false}){Text("Cerrar")}})
    removing?.let{p->AlertDialog(onDismissRequest={removing=null},title={Text("¿Quitar esta compra archivada?")},text={Text("Se retirará ${p.name} del historial de gasto. No se modifica la colección actual.")},confirmButton={TextButton(onClick={removeArchived(p.key);removing=null}){Text("Quitar compra")}},dismissButton={TextButton(onClick={removing=null}){Text("Cancelar")}})}
}
