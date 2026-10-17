package com.gamerdex.core.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RawgRepositoryTest {
    @Test fun communityScoreUsesFivePointScaleAndVoteCount() = runTest {
        val server = MockWebServer(); server.start()
        try {
            server.enqueue(MockResponse().setBody("""{"id":12,"name":"Rated","rating":4.25,"rating_top":4,"ratings_count":123}"""))
            val api = Retrofit.Builder().baseUrl(server.url("/api/")).addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())).build().create(RawgApi::class.java)
            val rating = RawgRepository(api, true).details(12).communityRating!!
            assertEquals(85f, rating.score); assertEquals(123, rating.votes)
        } finally { server.shutdown() }
    }
    @Test fun missingVotesAndInvalidScoresDoNotBecomeZeroRatings() = runTest {
        var data = RawgGame(12, "Rated")
        val api = object : RawgApi {
            override suspend fun search(query: String, size: Int) = RawgResults()
            override suspend fun details(id: Long) = data
            override suspend fun screenshots(id: Long, size: Int) = RawgScreenshots()
            override suspend fun movies(id: Long) = RawgMovies()
        }
        listOf(null to 8, 4f to 0, 0f to 8, 6f to 8, Float.NaN to 8).forEach { (score, count) ->
            data = data.copy(rating = score, ratingsCount = count)
            assertNull(RawgRepository(api, true).details(12).communityRating)
        }
    }
    @Test fun mediaKeepsScreenshotsWhenTrailerAccessFails() = runTest {
        val api = object : RawgApi {
            override suspend fun search(query: String, size: Int): RawgResults = error("unused")
            override suspend fun details(id: Long): RawgGame = error("unused")
            override suspend fun screenshots(id: Long, size: Int) = RawgScreenshots(listOf(RawgScreenshot("https://media.rawg.io/visible.jpg"), RawgScreenshot("https://media.rawg.io/hidden.jpg", true), RawgScreenshot("file:///unsafe.jpg")))
            override suspend fun movies(id: Long): RawgMovies = error("Forbidden")
        }
        val result = RawgRepository(api, true).media(12)
        assertEquals(listOf("https://media.rawg.io/visible.jpg"), result.screenshots)
        assertTrue(result.partialFailure); assertTrue(result.trailers.isEmpty())
    }
    @Test fun parsesRealMediaEndpointsAndSelectsHttpsVideo() = runTest {
        val server = MockWebServer(); server.start()
        server.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
            override fun dispatch(request: okhttp3.mockwebserver.RecordedRequest) = MockResponse().setBody(if (request.path!!.contains("screenshots"))
                """{"results":[{"image":"https://media.rawg.io/shot.jpg"}]}""" else
                """{"results":[{"name":"Trailer","preview":"https://media.rawg.io/preview.jpg","data":{"480":"https://media.rawg.io/trailer.mp4","max":"http://unsafe.mp4"}}]}""")
        }
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/api/")).addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())).build().create(RawgApi::class.java)
            val media = RawgRepository(api, true).media(12)
            assertEquals("https://media.rawg.io/shot.jpg", media.screenshots.single())
            assertEquals("https://media.rawg.io/trailer.mp4", media.trailers.single().url)
            assertFalse(media.partialFailure)
            val paths = listOf(server.takeRequest().requestUrl!!.encodedPath, server.takeRequest().requestUrl!!.encodedPath).toSet()
            assertEquals(setOf("/api/games/12/screenshots", "/api/games/12/movies"), paths)
        } finally { server.shutdown() }
    }
    @Test fun mapsSearchWithNullableArtworkAndUnknownFields() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("""{"results":[{"id":12,"name":"Portal","background_image":null,"released":"2007-10-10","platforms":[{"platform":{"name":"PC"}}],"unknown":true}]}"""))
            val api = Retrofit.Builder().baseUrl(server.url("/api/"))
                .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
                .build().create(RawgApi::class.java)
            val result = RawgRepository(api, true).search("Portal & friends").single()
            assertEquals(2007, result.year)
            assertNull(result.coverUrl)
            assertEquals(listOf("PC"), result.platforms)
            assertEquals("Portal & friends", server.takeRequest().requestUrl!!.queryParameter("search"))
        } finally { server.shutdown() }
    }
    @Test fun unconfiguredKeyFailsBeforeRequest() = runTest {
        var called = false
        val api = object : RawgApi {
            override suspend fun search(query: String, size: Int): RawgResults { called = true; error("Unexpected request") }
            override suspend fun details(id: Long): RawgGame = error("Unexpected request")
            override suspend fun screenshots(id: Long, size: Int): RawgScreenshots = error("Unexpected request")
            override suspend fun movies(id: Long): RawgMovies = error("Unexpected request")
        }
        try { RawgRepository(api, false).search("Portal"); fail("Expected missing key") }
        catch (expected: IllegalStateException) { assertTrue(expected.message!!.contains("RAWG_API_KEY")) }
        assertFalse(called)
    }
}
