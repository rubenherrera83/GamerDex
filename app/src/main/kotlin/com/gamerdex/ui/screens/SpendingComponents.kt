package com.gamerdex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.*

internal enum class SpendGrouping(val label:String){YEAR("Por año"),PLATFORM("Por plataforma"),SYSTEM("Por sistema"),ARCHIVED("Archivadas")}
internal data class SpendGroup(val key:String,val name:String,val purchases:List<Purchase>)

internal fun spendingGroups(entries:List<Purchase>,hardware:List<Hardware>,mode:SpendGrouping):List<SpendGroup> = when(mode){
    SpendGrouping.YEAR->entries.groupBy{it.purchasedOn?.take(4) ?: "Año desconocido"}.entries.sortedByDescending{it.key.toIntOrNull() ?: Int.MIN_VALUE}.map{(year,list)->SpendGroup("year-$year",year,list)}
    SpendGrouping.PLATFORM->entries.groupBy{it.platform}.toSortedMap().map{(name,list)->SpendGroup("platform-$name",name,list)}
    SpendGrouping.SYSTEM->entries.groupBy{it.systemId}.entries.sortedBy{it.key ?: Long.MAX_VALUE}.map{(id,list)->
        val unit=hardware.find{it.id==id}
        val name=if(id==null)"Sin vincular (videojuegos)" else unit?.let{
            it.name+if(hardware.count{h->h.name==it.name}>1)" · ${it.serialNumber.ifBlank{"#$id"}}" else ""
        } ?: ((list.firstOrNull{it.kind==PurchaseKind.HARDWARE}?.systemName ?: list.firstOrNull()?.systemName ?: "Sistema #$id")+" · archivado #$id")
        SpendGroup("unit-${id ?: "unlinked"}",name,list)
    }
    SpendGrouping.ARCHIVED->emptyList()
}

@Composable internal fun SpendingAmounts(totals:SpendTotals,prefix:String){
    if(totals.combined.isEmpty())Text("Sin importes registrados",style=MaterialTheme.typography.bodyLarge)
    totals.combined.forEach{(currency,amount)->
        Surface(color=MaterialTheme.colorScheme.surfaceVariant,contentColor=MaterialTheme.colorScheme.onSurface,shape=MaterialTheme.shapes.small,modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                Text("${formatPrice(amount,currency)} $currency",style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.primary,
                    modifier=Modifier.testTag("$prefix-total-$currency"))
                Text("Videojuegos: ${totals.games[currency]?.let{formatPrice(it,currency)} ?: "Sin importes"}",style=MaterialTheme.typography.bodyMedium)
                Text("Equipos: ${totals.hardware[currency]?.let{formatPrice(it,currency)} ?: "Sin importes"}",style=MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if(totals.withoutPrice>0)Text("${totals.withoutPrice} ${if(totals.withoutPrice==1)"compra" else "compras"} sin precio registrado",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable fun SpendingOverview(entries:List<Purchase>,open:()->Unit){
    val totals=remember(entries){spendingTotals(entries)}
    OutlinedCard(Modifier.fillMaxWidth()){
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text("Gasto histórico registrado",style=MaterialTheme.typography.titleLarge)
            if(totals.combined.isEmpty())Text("Sin importes registrados")
            totals.combined.forEach{(currency,amount)->Text("${formatPrice(amount,currency)} $currency",style=MaterialTheme.typography.headlineSmall,color=MaterialTheme.colorScheme.primary)}
            if(totals.withoutPrice>0)Text("${totals.withoutPrice} ${if(totals.withoutPrice==1)"compra" else "compras"} sin precio registrado",style=MaterialTheme.typography.bodySmall)
            Text("Incluye compras de fichas eliminadas. Las monedas se muestran por separado.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(open,Modifier.fillMaxWidth()){Text("Ver gasto por año, plataforma y sistema")}
        }
    }
}

@Composable internal fun SpendingFilter(label:String,value:String?,all:String,options:List<String>,modifier:Modifier=Modifier,choose:(String?)->Unit){
    var expanded by remember{mutableStateOf(false)}
    Box(modifier){
        OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxSize().testTag("spend-filter-$label")){Text("$label: ${value ?: all}")}
        DropdownMenu(expanded,{expanded=false},modifier=Modifier.heightIn(max=360.dp)){
            DropdownMenuItem(text={Text(all)},onClick={choose(null);expanded=false})
            options.forEach{option->DropdownMenuItem(text={Text(option)},onClick={choose(option);expanded=false},modifier=Modifier.testTag("spend-option-$label-$option"))}
        }
    }
}

@Composable internal fun SpendingChart(groups:List<SpendGroup>,mode:SpendGrouping,currency:String,chooseCurrency:(String)->Unit,currencies:List<String>){
    val values=remember(groups,currency,mode){
        val known=groups.mapNotNull{group->spendingTotals(group.purchases).combined[currency]?.let{group to it}}
        if(mode==SpendGrouping.YEAR){
            known.filter{it.first.name!="Año desconocido"}.take(6).reversed()+known.filter{it.first.name=="Año desconocido"}
        }else known.sortedByDescending{it.second}.take(6)
    }
    val maximum=values.maxOfOrNull{it.second} ?: 0L
    OutlinedCard(Modifier.fillMaxWidth().testTag("spending-chart")){
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            Text(if(mode==SpendGrouping.YEAR)"Evolución del gasto" else "Mayor gasto de la selección",style=MaterialTheme.typography.titleLarge,modifier=Modifier.semantics{heading()})
            var expanded by remember{mutableStateOf(false)}
            Box{
                TextButton(onClick={expanded=true},modifier=Modifier.testTag("spending-chart-currency")){Text("Moneda del gráfico: $currency")}
                DropdownMenu(expanded,{expanded=false}){
                    currencies.forEach{code->DropdownMenuItem(text={Text(code)},onClick={chooseCurrency(code);expanded=false},modifier=Modifier.testTag("chart-currency-$code"))}
                }
            }
            if(values.isEmpty())Text("No hay importes en $currency para este desglose.")
            values.forEach{(group,amount)->
                Column(Modifier.testTag("spending-bar-${group.key}"),verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Text(group.name,style=MaterialTheme.typography.labelLarge)
                    Text("${formatPrice(amount,currency)} $currency",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.primary)
                    LinearProgressIndicator(progress={if(maximum==0L)0f else (amount.toDouble()/maximum).toFloat().coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())
                }
            }
            Text(if(mode==SpendGrouping.YEAR)"Hasta 6 años con importes, más Año desconocido. El desglose inferior incluye todos los grupos." else "Hasta 6 grupos con mayor importe. El desglose inferior incluye todos los grupos.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Solo el gráfico usa esta moneda. Los totales conservan todas las monedas, sin convertirlas.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
