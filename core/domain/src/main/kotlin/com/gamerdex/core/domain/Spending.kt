package com.gamerdex.core.domain

import kotlinx.serialization.Serializable

@Serializable enum class PurchaseKind { GAME, HARDWARE }
@Serializable data class Purchase(
    val kind: PurchaseKind, val sourceId: Long, val name: String, val platform: String,
    val amount: Long?, val currency: String, val purchasedOn: String? = null,
    val systemId: Long? = null, val systemName: String? = null,
) {
    val key get()="${kind.name}:$sourceId"
    init {
        require(sourceId>0 && name.isNotBlank() && platform.isNotBlank())
        require(currency in supportedCurrencies && (amount==null || amount in 0..99_999_999_999))
        require(currency!="JPY" || amount==null || amount%100L==0L)
        require(purchasedOn==null || validPurchaseDate(purchasedOn))
        require(systemId==null || systemId>0)
    }
}
fun gamePurchase(game:Game,hardware:List<Hardware>,previous:Purchase?=null)=Purchase(PurchaseKind.GAME,game.id,game.title,game.platform,game.pricePaidCents,game.priceCurrency,game.purchasedOn,game.ownedSystemId,
    hardware.find{it.id==game.ownedSystemId}?.name ?: previous?.systemName.takeIf{previous?.systemId==game.ownedSystemId})
fun hardwarePurchase(value:Hardware)=Purchase(PurchaseKind.HARDWARE,value.id,value.name,value.platform,value.pricePaidCents,value.currency,value.purchasedOn,value.id,value.name)
fun seedPurchases(games:List<Game>,hardware:List<Hardware>)=games.map{gamePurchase(it,hardware)}+hardware.map(::hardwarePurchase)
data class SpendTotals(val games:Map<String,Long>,val hardware:Map<String,Long>,val combined:Map<String,Long>,val withoutPrice:Int)
fun spendingTotals(entries:List<Purchase>):SpendTotals {
    fun sum(list:List<Purchase>)=list.filter{it.amount!=null}.groupBy{it.currency}.mapValues{(_,v)->v.sumOf{it.amount!!}}.toSortedMap()
    return SpendTotals(sum(entries.filter{it.kind==PurchaseKind.GAME}),sum(entries.filter{it.kind==PurchaseKind.HARDWARE}),sum(entries),entries.count{it.amount==null})
}
fun spendingByYear(entries:List<Purchase>)=entries.groupBy{it.purchasedOn?.take(4) ?: "Año desconocido"}.toSortedMap(compareByDescending<String>{it.toIntOrNull() ?: Int.MIN_VALUE}).mapValues{spendingTotals(it.value)}
fun filterPurchases(entries:List<Purchase>,year:String?=null,platform:String?=null)=entries.filter {
    (year==null || (it.purchasedOn?.take(4) ?: "Año desconocido")==year) && (platform==null || it.platform==platform)
}
