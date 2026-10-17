package com.gamerdex

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gamerdex.core.domain.*
import com.gamerdex.ui.RawgMediaViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RawgRatingStateTest {
    @Test fun ratingsAndMediaFailuresAreIndependent() = runBlocking {
        listOf(true, false).forEach { ratingAvailable ->
            val repository = object : MetadataRepository {
                override suspend fun search(query: String) = emptyList<Metadata>()
                override suspend fun details(id: Long): Metadata {
                    if (!ratingAvailable) error("No rating access")
                    delay(20)
                    return Metadata(id, "Game", null, null, listOf("PC"), communityRating = CommunityRating(85f, 12))
                }
                override suspend fun media(id: Long): GameMedia {
                    if (ratingAvailable) error("No media access")
                    delay(20)
                    return GameMedia(screenshots = listOf("https://media.rawg.io/test.jpg"))
                }
            }
            val viewModel = RawgMediaViewModel(repository)
            viewModel.load(12)
            val state = withTimeout(5_000) { viewModel.state.first { !it.loading && !it.ratingLoading } }
            if (ratingAvailable) { assertEquals(85f, state.communityRating!!.score); assertNotNull(state.error); assertFalse(state.ratingError) }
            else { assertTrue(state.ratingError); assertEquals(1, state.media!!.screenshots.size); assertNull(state.error) }
        }
    }
}
