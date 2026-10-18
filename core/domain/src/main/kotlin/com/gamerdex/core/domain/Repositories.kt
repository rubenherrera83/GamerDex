package com.gamerdex.core.domain

import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun observePurchases(): Flow<List<Purchase>> = kotlinx.coroutines.flow.flowOf(emptyList())
    suspend fun removeArchivedPurchase(key:String) { throw UnsupportedOperationException() }
    fun observeHardware(): Flow<List<Hardware>> = kotlinx.coroutines.flow.flowOf(emptyList())
    suspend fun saveHardware(hardware: Hardware) { throw UnsupportedOperationException() }
    suspend fun deleteHardware(id: Long) { throw UnsupportedOperationException() }
    fun observeGames(): Flow<List<Game>>
    suspend fun save(game: Game)
    suspend fun delete(id: Long)
    suspend fun snapshot(): CollectionSnapshot
    suspend fun replace(snapshot: CollectionSnapshot)
}

data class Metadata(val id: Long, val title: String, val coverUrl: String?, val year: Int?, val platforms: List<String>, val company: String? = null, val genres: List<String> = emptyList(), val communityRating: CommunityRating? = null)
data class CommunityRating(val score: Float, val votes: Int) {
    init { require(score.isFinite() && score in 0f..100f && votes > 0) }
}
interface MetadataRepository {
    suspend fun search(query: String): List<Metadata>
    suspend fun details(id: Long): Metadata
    suspend fun media(id: Long): GameMedia = GameMedia()
}

data class GameTrailer(val title: String, val url: String, val preview: String?)
data class GameMedia(val screenshots: List<String> = emptyList(), val trailers: List<GameTrailer> = emptyList(), val partialFailure: Boolean = false)
