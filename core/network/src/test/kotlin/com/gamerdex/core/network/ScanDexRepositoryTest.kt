package com.gamerdex.core.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.*
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ScanDexRepositoryTest {
    @Test fun lookupUsesHeaderAndPreservesCodeWithoutConfusingIgdbWithRawg()= runTest {
        val server=MockWebServer();server.start()
        try {
            val api=Retrofit.Builder().baseUrl(server.url("/api/v2/")).addConverterFactory(Json{ignoreUnknownKeys=true}.asConverterFactory("application/json".toMediaType())).build().create(ScanDexApi::class.java)
            val repository=ScanDexRepository(api,"test-token")
            server.enqueue(MockResponse().setBody("""{"id":87859,"source":"import","igdb_metadata":{"id":26758,"name":"Super Mario Odyssey","platform":{"id":130,"name":"Nintendo Switch"}}}"""))
            val result=repository.lookup("0711719577966")!!
            assertEquals("Super Mario Odyssey",result.title); assertEquals("Nintendo Switch",result.platform)
            val request=server.takeRequest(); assertEquals("test-token",request.getHeader("Authorization"));assertEquals("0711719577966",request.requestUrl!!.queryParameter("value"));assertNull(request.requestUrl!!.queryParameter("key"))
            server.enqueue(MockResponse().setResponseCode(404));assertNull(repository.lookup("0711719577966"))
            server.enqueue(MockResponse().setBody("""{"status":"unmatched","igdb_metadata":null}"""))
            try{repository.lookup("0711719577966");fail()}catch(e:IllegalStateException){assertTrue(e.message!!.contains("conoce el código"))}
            server.enqueue(MockResponse().setResponseCode(429))
            try { repository.lookup("0711719577966");fail() } catch(e:IllegalStateException){assertTrue(e.message!!.contains("límite"))}
            for(status in listOf(401,403)) {
                server.enqueue(MockResponse().setResponseCode(status))
                try { repository.lookup("0711719577966");fail() } catch(e:IllegalStateException){assertTrue(e.message!!.contains("token",ignoreCase=true))}
            }
            try { ScanDexRepository(api,"").lookup("0711719577966");fail() } catch(_:IllegalStateException){}
            assertEquals(6,server.requestCount)
        }finally{server.shutdown()}
    }
}
