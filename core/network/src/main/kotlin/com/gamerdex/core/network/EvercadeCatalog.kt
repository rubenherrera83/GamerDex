package com.gamerdex.core.network

import android.content.Context
import com.gamerdex.core.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.*
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File
import java.util.concurrent.TimeUnit

interface EvercadeApi {
    @GET("cartridges/") suspend fun index():ResponseBody
    @GET("cartridges/{slug}/") suspend fun detail(@Path("slug") slug:String):ResponseBody
}
class EvercadeCatalog(private val api:EvercadeApi):EvercadeRepository {
    private var cached:List<EvercadeCartridge> = emptyList()
    private var refreshed=0L
    private val mutex=Mutex()
    override suspend fun search(query:String):List<EvercadeCartridge> = mutex.withLock {
        if(cached.isEmpty() || System.nanoTime()-refreshed>TimeUnit.MINUTES.toNanos(10)) {
            val list=parseIndex(read(api.index()))
            check(list.isNotEmpty()){"El catálogo oficial de Evercade no se pudo interpretar. Prueba más tarde o abre su web."}
            cached=list;refreshed=System.nanoTime()
        }
        cached.filter{it.title.contains(query.trim(),true)}
    }
    override suspend fun details(slug:String):EvercadeCartridge {
        require(slug.matches(Regex("[a-z0-9-]+")))
        val base=search("").firstOrNull{it.slug==slug} ?: error("No se encontró ese cartucho en el catálogo oficial.")
        val doc=Jsoup.parse(read(api.detail(slug)))
        return base.copy(games=doc.select(".game-title").map{it.text()}.filter(String::isNotBlank).distinct().take(200))
    }
    private suspend fun read(body:ResponseBody)=withContext(Dispatchers.IO) {body.use {
        val source=it.source();source.request(4L*1024*1024+1)
        require(source.buffer.size<=4L*1024*1024){"El catálogo excede el tamaño admitido."};source.readUtf8()
    }}
    companion object {
        fun parseIndex(html:String):List<EvercadeCartridge> {
            val doc=Jsoup.parse(html,"https://evercade.co.uk/cartridges/")
            return doc.select("a.cartridge-item").mapNotNull { card ->
                val url=card.absUrl("href")
                val uri=runCatching{java.net.URI(url)}.getOrNull() ?: return@mapNotNull null
                if(uri.scheme!="https" || uri.host!="evercade.co.uk" || !uri.path.matches(Regex("/cartridges/[a-z0-9-]+/")))return@mapNotNull null
                val title=card.selectFirst(".cartridge-title")?.text()?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                val style=card.selectFirst(".cartridge-image")?.attr("style").orEmpty()
                val image=Regex("url\\(['\"]?([^)'\"]+)").find(style)?.groupValues?.get(1)?.takeIf(::isValidLogoUrl)
                EvercadeCartridge(uri.path.trim('/').substringAfterLast('/'),title,image,url)
            }.distinctBy{it.slug}.take(500)
        }
        fun create(context:Context):EvercadeRepository {
            val client=OkHttpClient.Builder().cache(Cache(File(context.cacheDir,"evercade_http"),8L*1024*1024)).callTimeout(20,TimeUnit.SECONDS).build()
            return EvercadeCatalog(Retrofit.Builder().baseUrl("https://evercade.co.uk/").client(client).build().create(EvercadeApi::class.java))
        }
    }
}
