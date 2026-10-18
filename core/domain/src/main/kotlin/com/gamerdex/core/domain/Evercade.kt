package com.gamerdex.core.domain

data class EvercadeCartridge(val slug:String,val title:String,val cover:String?,val url:String,val games:List<String> = emptyList())
interface EvercadeRepository {
    suspend fun search(query:String):List<EvercadeCartridge>
    suspend fun details(slug:String):EvercadeCartridge
}
