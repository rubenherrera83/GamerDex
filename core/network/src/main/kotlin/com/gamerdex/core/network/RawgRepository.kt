package com.gamerdex.core.network

import com.gamerdex.core.domain.*
import kotlinx.coroutines.*

class RawgRepository(private val api: RawgApi, private val configured: Boolean) : MetadataRepository {
    private fun checkKey() = check(configured) { "Configura RAWG_API_KEY en local.properties para buscar. Puedes añadir juegos manualmente sin conexión." }
    override suspend fun search(query: String): List<Metadata> {
        checkKey()
        return api.search(query.trim(), 40).results.map { it.toDomain() }
    }
    override suspend fun details(id: Long): Metadata { checkKey(); return api.details(id).toDomain() }
    override suspend fun media(id: Long): GameMedia = supervisorScope {
        checkKey()
        suspend fun <T> optional(fetch: suspend () -> T): T? = try { fetch() } catch (e: CancellationException) { throw e } catch (_: Exception) { null }
        val shots = async { optional { api.screenshots(id) } }
        val movies = async { optional { api.movies(id) } }
        val images = shots.await(); val clips = movies.await()
        GameMedia(images?.results.orEmpty().filter { !it.hidden && isValidLogoUrl(it.image) }.map { it.image }.distinct().take(12),
            clips?.results.orEmpty().mapNotNull { movie ->
                val url = (listOfNotNull(movie.data["480"], movie.data["max"]) + movie.data.values).firstOrNull { isValidLogoUrl(it) } ?: return@mapNotNull null
                GameTrailer(movie.name, url, movie.preview?.takeIf { isValidLogoUrl(it) })
            }.take(6), images == null || clips == null)
    }
    private fun RawgGame.toDomain() = Metadata(id, name, cover, released?.take(4)?.toIntOrNull(),
        platforms.orEmpty().map { it.platform.name }, publishers.firstOrNull()?.name ?: developers.firstOrNull()?.name, genres.map { it.name },
        rating?.takeIf { it.isFinite() && it > 0f && it <= 5f && (ratingsCount ?: 0) > 0 }?.let { CommunityRating(it * 20f, requireNotNull(ratingsCount)) })
}
