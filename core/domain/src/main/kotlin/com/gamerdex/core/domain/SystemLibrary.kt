package com.gamerdex.core.domain

data class SystemLibrary(val key:String,val title:String,val platform:String,val gameCount:Int,val unitCount:Int,val representative:Hardware?,val logoUrl:String?)

fun systemLibraries(games:List<Game>,hardware:List<Hardware>):List<SystemLibrary> {
    val titles=games.groupBy{SystemKnowledge.family(it.platform)}
    val units=hardware.groupBy{SystemKnowledge.family(it.platform)}
    return (titles.keys+units.keys).distinct().map{key->
        val copies=titles[key].orEmpty();val devices=units[key].orEmpty()
        val image=devices.firstOrNull{it.localPhotos.isNotEmpty() || it.imageUrl!=null} ?: devices.firstOrNull()
        val platform=devices.firstOrNull()?.platform ?: copies.first().platform
        val title=SystemKnowledge.history(platform)?.family ?: platform
        SystemLibrary(key,title,platform,copies.size,devices.size,image,devices.firstNotNullOfOrNull{it.logoUrl} ?: copies.firstNotNullOfOrNull{it.platformLogoUrl})
    }.sortedBy{it.title.lowercase(java.util.Locale.ROOT)}
}
fun gamesForSystem(games:List<Game>,key:String)=games.filter{SystemKnowledge.family(it.platform)==key}
