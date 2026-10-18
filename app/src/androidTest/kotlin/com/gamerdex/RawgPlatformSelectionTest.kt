package com.gamerdex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.gamerdex.core.domain.Metadata
import com.gamerdex.ui.SearchState
import com.gamerdex.ui.screens.SearchSheet
import com.gamerdex.ui.theme.GamerDexTheme
import com.gamerdex.ui.theme.VisualTheme
import org.junit.*
import org.junit.Assert.*

class RawgPlatformSelectionTest {
    @get:Rule val compose=createComposeRule()
    @Test fun genesisIsVisibleAndCanBeSelectedThroughMegaDriveAlias() {
        var selected:Metadata?=null
        compose.setContent { GamerDexTheme(VisualTheme.CYBERPUNK){SearchSheet(SearchState("Sonic",listOf(
            Metadata(53551,"Sonic the Hedgehog (1991)",null,1991,listOf("PC","iOS","Genesis")),
            Metadata(283965,"Sonic The Hedgehog (2006)",null,2006,listOf("PlayStation 3")))),{}, {selected=it},{})} }
        compose.onNodeWithText("Filtrar plataforma").performTextInput("Mega Drive")
        compose.onNodeWithText("Año").performTextInput("1991")
        compose.onNodeWithText("Sonic The Hedgehog (2006)").assertDoesNotExist()
        compose.onNodeWithText("Genesis / Mega Drive–Genesis",substring=true).assertExists()
        compose.onNodeWithText("Sonic the Hedgehog (1991)").performClick()
        compose.onNodeWithText("Elige tu versión").assertExists()
        compose.onNodeWithText("Genesis / Mega Drive–Genesis").performScrollTo().performClick()
        assertEquals(listOf("Genesis"),selected!!.platforms)
    }
}
