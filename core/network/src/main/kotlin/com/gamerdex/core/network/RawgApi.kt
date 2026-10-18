package com.gamerdex.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable data class RawgResults(val results: List<RawgGame> = emptyList(),val next:String?=null)
@Serializable data class RawgName(val name: String)
@Serializable data class RawgPlatform(val platform: RawgName)
@Serializable data class RawgGame(
    val id: Long, val name: String,
    @SerialName("background_image") val cover: String? = null,
    val released: String? = null, val platforms: List<RawgPlatform>? = null,
    val publishers: List<RawgName> = emptyList(), val developers: List<RawgName> = emptyList(),
    val genres: List<RawgName> = emptyList(),
    val rating: Float? = null,
    @SerialName("ratings_count") val ratingsCount: Int? = null,
)
interface RawgApi {
    @GET("games") suspend fun search(@Query("search") query: String, @Query("page_size") size: Int = 12): RawgResults
    @GET("games/{id}") suspend fun details(@Path("id") id: Long): RawgGame
    @GET("games/{id}/screenshots") suspend fun screenshots(@Path("id") id: Long, @Query("page_size") size: Int = 12): RawgScreenshots
    @GET("games/{id}/movies") suspend fun movies(@Path("id") id: Long): RawgMovies
}
interface RawgRankingApi {
    @GET("games") suspend fun ranked(@Query("platforms") platformId:Int,@Query("page") page:Int,@Query("ordering") ordering:String="-rating",@Query("page_size") size:Int=40):RawgResults
}

@Serializable data class RawgScreenshot(val image: String, val hidden: Boolean = false)
@Serializable data class RawgScreenshots(val results: List<RawgScreenshot> = emptyList())
@Serializable data class RawgMovie(val name: String, val preview: String? = null, val data: Map<String, String> = emptyMap())
@Serializable data class RawgMovies(val results: List<RawgMovie> = emptyList())
