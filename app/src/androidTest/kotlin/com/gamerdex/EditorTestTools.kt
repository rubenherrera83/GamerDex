package com.gamerdex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull

/** Existing detailed flows explicitly opt into all optional editor sections. */
fun expandGameSections(compose:ComposeTestRule){
    listOf("identify","copy","purchase","metadata","progress","photos").forEach{id->
        val section=compose.onNodeWithTag("editor-section-$id")
        if(section.fetchSemanticsNode().config.getOrNull(SemanticsProperties.StateDescription)=="Contraído")section.performScrollTo().performClick()
    }
    compose.onNodeWithText("Buscar por nombre").performScrollTo()
}

/** Saving no longer puts a game first: find it through the lazy collection's scroll action. */
fun awaitCollectionGame(compose:ComposeTestRule,title:String){
    compose.waitUntil(10_000){compose.onAllNodesWithTag("collection-grid").fetchSemanticsNodes().isNotEmpty()}
    compose.onNodeWithTag("collection-grid").performScrollToNode(hasText(title))
    compose.onNodeWithText(title).assertIsDisplayed()
}

fun openGlobalCollection(compose:ComposeTestRule){
    compose.onNodeWithTag("nav-vault").performClick()
    compose.waitUntil(10_000){compose.onAllNodesWithTag("collection-grid").fetchSemanticsNodes().isNotEmpty()}
}