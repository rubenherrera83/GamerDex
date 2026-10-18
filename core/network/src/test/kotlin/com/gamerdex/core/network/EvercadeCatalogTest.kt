package com.gamerdex.core.network

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.*
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit

class EvercadeCatalogTest {
    @Test fun officialCardsAreFilteredCachedAndDetailsKeepIncludedGameNames()=runTest {
        val html="""<a class="cartridge-item" href="https://evercade.co.uk/cartridges/atari-collection-1/"><div class="cartridge-image" style="background-image:url(https://evercade.co.uk/art.webp)"></div><span class="cartridge-title">Atari &amp; Friends</span></a><a class="cartridge-item" href="https://evil.example/cartridges/test/"><span class="cartridge-title">Bad</span></a>"""
        val server=MockWebServer();server.start()
        try{
            server.enqueue(MockResponse().setBody(html));server.enqueue(MockResponse().setBody("<span class='game-title'>Adventure</span><span class='game-title'>Centipede</span>"))
            val repository=EvercadeCatalog(Retrofit.Builder().baseUrl(server.url("/")).build().create(EvercadeApi::class.java))
            val found=repository.search("ATARI").single();assertEquals("Atari & Friends",found.title);assertEquals("https://evercade.co.uk/art.webp",found.cover)
            assertTrue(repository.search("Missing").isEmpty());assertEquals(1,server.requestCount)
            val details=repository.details(found.slug);assertEquals(listOf("Adventure","Centipede"),details.games)
            assertEquals("/cartridges/",server.takeRequest().path);assertEquals("/cartridges/atari-collection-1/",server.takeRequest().path)
        }finally{server.shutdown()}
    }
}
