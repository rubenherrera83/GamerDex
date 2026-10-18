package com.gamerdex.core.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.*
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class UpcItemDbRepositoryTest {
    private fun api(server:MockWebServer)=Retrofit.Builder().baseUrl(server.url("/prod/trial/"))
        .addConverterFactory(Json{ignoreUnknownKeys=true}.asConverterFactory("application/json".toMediaType())).build().create(UpcItemDbApi::class.java)
    private val item="""{"code":"OK","total":1,"items":[{"ean":"5026555067591","title":"Sid Meier's Civilization Vi Switch (sp)","category":"Electronics > Video Game Consoles","lowest_recorded_price":12.5}]}"""
    @Test fun exactCodeTitleAndSourceWithoutCredentialsOrRetailPrices()=runTest {
        val server=MockWebServer();server.start()
        try {
            val repository=UpcItemDbRepository(api(server),{testScheduler.currentTime})
            server.enqueue(MockResponse().setBody(item))
            val result=repository.lookup("5026555067591")!!
            assertEquals("UPCitemdb",result.source);assertEquals("Nintendo Switch",result.platform)
            assertEquals("Sid Meier's Civilization Vi",result.searchTitle);assertTrue(result.title.endsWith("(sp)"))
            val request=server.takeRequest();assertEquals("/prod/trial/lookup?upc=5026555067591",request.path)
            for(name in listOf("Authorization","user_key","key_type"))assertNull(request.getHeader(name))
            assertNull(request.requestUrl!!.queryParameter("key"))
            assertEquals(result,repository.lookup("5026555067591"));assertEquals(1,server.requestCount)
        }finally{server.shutdown()}
    }
    @Test fun preservesLeadingZeroAndUnknownPlatformWithoutInventingOne()=runTest {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setBody("""{"code":"OK","items":[{"ean":"0711719577966","title":"Horizon Forbidden West"}]}"""))
            val result=UpcItemDbRepository(api(server)).lookup("0711719577966")!!
            assertEquals("",result.platform);assertEquals("Horizon Forbidden West",result.searchTitle)
            assertEquals("0711719577966",server.takeRequest().requestUrl!!.queryParameter("upc"))
        }finally{server.shutdown()}
    }
    @Test fun pacesDistinctRequestsAndCachesUnknownCodes()=runTest {
        val server=MockWebServer();server.start()
        try {
            val repository=UpcItemDbRepository(api(server),{testScheduler.currentTime})
            server.enqueue(MockResponse().setResponseCode(404));assertNull(repository.lookup("5026555067591"))
            assertNull(repository.lookup("5026555067591"));assertEquals(1,server.requestCount)
            server.enqueue(MockResponse().setBody("""{"code":"OK","items":[]}"""));assertNull(repository.lookup("0711719577966"))
            assertEquals(10_000L,testScheduler.currentTime);assertEquals(2,server.requestCount)
        }finally{server.shutdown()}
    }
    @Test fun quotaHonorsResetAndDoesNotRetryOrMasqueradeAsMissing()=runTest {
        val server=MockWebServer();server.start()
        try {
            val repository=UpcItemDbRepository(api(server),{testScheduler.currentTime},{1_000_000+testScheduler.currentTime})
            server.enqueue(MockResponse().setResponseCode(429).setHeader("X-RateLimit-Reset","1060"))
            repeat(2){try{repository.lookup("5026555067591");fail()}catch(e:IllegalStateException){assertTrue(e.message!!.contains("límite"))}}
            assertEquals(1,server.requestCount)
            testScheduler.advanceTimeBy(60_001)
            server.enqueue(MockResponse().setBody(item));assertNotNull(repository.lookup("5026555067591"));assertEquals(2,server.requestCount)
        }finally{server.shutdown()}
    }
    @Test fun rejectsWrongOrAmbiguousCodesAndReportsServerFailure()=runTest {
        val server=MockWebServer();server.start()
        try {
            val repository=UpcItemDbRepository(api(server),{testScheduler.currentTime})
            for(body in listOf("""{"code":"OK","items":[{"ean":"0711719577966","title":"Wrong game"}]}""","""{"code":"OK","items":[{"ean":"5026555067591","title":"A"},{"ean":"5026555067591","title":"B"}]}""")) {
                server.enqueue(MockResponse().setBody(body))
                try{repository.lookup("5026555067591");fail()}catch(e:IllegalStateException){assertTrue(e.message!!.contains("ambiguo"))}
            }
            server.enqueue(MockResponse().setResponseCode(503))
            try{repository.lookup("5026555067591");fail()}catch(e:IllegalStateException){assertTrue(e.message!!.contains("503"))}
            assertEquals(3,server.requestCount)
        }finally{server.shutdown()}
    }
}
