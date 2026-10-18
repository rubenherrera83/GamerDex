package com.gamerdex.core.network

import com.gamerdex.core.domain.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable data class UpcItem(val title:String="",val ean:String="",val upc:String="",val gtin:String="")
@Serializable data class UpcResponse(val code:String,val items:List<UpcItem> = emptyList())
interface UpcItemDbApi {
    @GET("lookup") suspend fun lookup(@Query("upc") code:String):Response<UpcResponse>
}

/** Free endpoint: a single connection, one request per ten seconds, no credentials. */
class UpcItemDbRepository(
    private val api:UpcItemDbApi,
    private val nowMillis:()->Long={System.nanoTime()/1_000_000},
    private val wallMillis:()->Long=System::currentTimeMillis,
):BarcodeRepository {
    private data class Cached(val match:BarcodeMatch?,val expires:Long)
    private val mutex=Mutex()
    private val cached=linkedMapOf<String,Cached>()
    private var lastRequest:Long?=null
    private var blockedUntil=Long.MIN_VALUE
    override suspend fun lookup(code:String):BarcodeMatch? {
        require(validBarcode(code)){"Código EAN/UPC inválido. Comprueba todos los dígitos."}
        return mutex.withLock {
            cached[code]?.takeIf{it.expires>nowMillis()}?.let{return@withLock it.match}
            check(nowMillis()>=blockedUntil){"UPCitemdb ha alcanzado su límite temporal o diario. Espera antes de reintentar o busca por título."}
            lastRequest?.let {delay((10_000-(nowMillis()-it)).coerceAtLeast(0))}
            lastRequest=nowMillis()
            val response=api.lookup(code)
            if(response.code()==429 || response.headers()["X-RateLimit-Remaining"]=="0") {
                val reset=response.headers()["X-RateLimit-Reset"]?.toLongOrNull()
                val pause=reset?.let{it*1000-wallMillis()}?.coerceIn(10_000,86_400_000) ?: 900_000L
                blockedUntil=nowMillis()+pause
            }
            val match=when(response.code()) {
                404->null
                429->error("UPCitemdb ha alcanzado su límite temporal o diario. Espera antes de reintentar o busca por título.")
                200->{
                    val body=response.body() ?: error("UPCitemdb respondió sin datos de identificación.")
                    check(body.code=="OK"){"UPCitemdb no pudo completar la consulta. Busca por título o reintenta más tarde."}
                    val items=body.items.filter{item->listOf(item.ean,item.upc,item.gtin).any{it.isNotBlank() && it.padStart(14,'0')==code.padStart(14,'0')}}
                    check(body.items.isEmpty() || items.size==1){"UPCitemdb devolvió un resultado ambiguo o de otro código. Confirma el juego buscando por título."}
                    items.singleOrNull()?.let {item->
                        check(item.title.isNotBlank()){"UPCitemdb encontró el código sin un título utilizable. Busca por título."}
                        match(item.title.trim())
                    }
                }
                else->error("No se pudo consultar UPCitemdb (HTTP ${response.code()}). Reintenta o busca por título.")
            }
            if(cached.size>=500)cached.remove(cached.keys.first())
            cached[code]=Cached(match,nowMillis()+if(match==null)300_000 else 900_000)
            match
        }
    }
    companion object {
        private val suffix=Regex("\\s*\\((?:sp|es|uk|eu|us|jp|pal|ntsc)\\)\\s*$",RegexOption.IGNORE_CASE)
        private val platformSuffix=Regex("(?:\\s*[-–:]?\\s+)(Nintendo Switch|Switch|PlayStation 5|PlayStation 4|PlayStation 3|PS5|PS4|PS3|Xbox One|Xbox Series X(?:/S)?)$",RegexOption.IGNORE_CASE)
        private fun match(title:String):BarcodeMatch {
            val withoutLocale=title.replace(suffix,"").trim()
            val found=platformSuffix.find(withoutLocale)
            val platform=found?.groupValues?.get(1)?.let{when(it.lowercase()){
                "switch","nintendo switch"->"Nintendo Switch";"ps5","playstation 5"->"PlayStation 5";"ps4","playstation 4"->"PlayStation 4";"ps3","playstation 3"->"PlayStation 3";else->it
            }}.orEmpty()
            val query=found?.let{withoutLocale.substring(0,it.range.first).trim()}?.takeIf{it.length>=2} ?: withoutLocale
            return BarcodeMatch(title,platform,"UPCitemdb",query)
        }
        fun create():BarcodeRepository {
            val client=OkHttpClient.Builder().callTimeout(15,TimeUnit.SECONDS).retryOnConnectionFailure(false).followRedirects(false).followSslRedirects(false).build()
            return UpcItemDbRepository(Retrofit.Builder().baseUrl("https://api.upcitemdb.com/prod/trial/").client(client)
                .addConverterFactory(Json{ignoreUnknownKeys=true}.asConverterFactory("application/json".toMediaType())).build().create(UpcItemDbApi::class.java))
        }
    }
}
