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
