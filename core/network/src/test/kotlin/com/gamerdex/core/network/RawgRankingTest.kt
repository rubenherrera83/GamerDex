package com.gamerdex.core.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.*
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RawgRankingTest {
    @Test fun rankingUsesExactPlatformGlobalScoreAndMinimumVotesWithPagination()=runTest {
        val server=MockWebServer();server.start()
        try {
            val retrofit=Retrofit.Builder().baseUrl(server.url("/api/")).addConverterFactory(Json{ignoreUnknownKeys=true}.asConverterFactory("application/json".toMediaType())).build()
            fun game(id:Int,votes:Int,platform:String="Genesis",score:Float=4.5f)="""{"id":$id,"name":"Game $id","rating":$score,"ratings_count":$votes,"platforms":[{"platform":{"name":"$platform"}}]}"""
            server.enqueue(MockResponse().setBody("""{"next":"https://api.rawg.io/api/games?page=2","results":[${(1..40).joinToString(","){game(it,1)}}]}"""))
            server.enqueue(MockResponse().setBody("""{"results":[${listOf(game(100,50,"PC"),game(101,10),game(102,20),game(103,30),game(104,40),game(105,50),game(106,100,"Genesis",0f)).joinToString(",")}]}"""))
            val repository=RawgRepository(retrofit.create(RawgApi::class.java),true,retrofit.create(RawgRankingApi::class.java))
            val top=repository.topForPlatform(167)
            assertEquals(listOf(101L,102L,103L,104L,105L),top.map{it.id});assertEquals(90f,top.first().communityRating!!.score)
            repeat(2){page->val r=server.takeRequest();assertEquals("167",r.requestUrl!!.queryParameter("platforms"));assertEquals("-rating",r.requestUrl!!.queryParameter("ordering"));assertEquals((page+1).toString(),r.requestUrl!!.queryParameter("page"))}
        }finally{server.shutdown()}
    }
    @Test fun noRatingsStaysEmptyAndServerErrorStaysAnError()=runTest {
        val server=MockWebServer();server.start()
        try {
            val retrofit=Retrofit.Builder().baseUrl(server.url("/api/")).addConverterFactory(Json{ignoreUnknownKeys=true}.asConverterFactory("application/json".toMediaType())).build()
            val repo=RawgRepository(retrofit.create(RawgApi::class.java),true,retrofit.create(RawgRankingApi::class.java))
            server.enqueue(MockResponse().setBody("""{"results":[]}"""));assertTrue(repo.topForPlatform(74).isEmpty())
            server.enqueue(MockResponse().setResponseCode(500));try{repo.topForPlatform(74);fail()}catch(_:retrofit2.HttpException){}
            assertEquals(2,server.requestCount)
        }finally{server.shutdown()}
    }
}
