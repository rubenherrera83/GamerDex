package com.gamerdex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.gamerdex.core.domain.*
import com.gamerdex.ui.screens.SpendingScreen
import com.gamerdex.ui.theme.*
import org.junit.Rule
import org.junit.Test
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import java.io.File

class SpendingFiltersTest {
    @get:Rule val compose=createComposeRule()
    @Test fun yearAndPlatformSelectorsIntersectAndCanBeCleared() {
        val entries=listOf(Purchase(PurchaseKind.GAME,1,"Old","PC",1000,"EUR","2020-01-01"),Purchase(PurchaseKind.GAME,2,"New","PC",2000,"EUR","2021-01-01"),
            Purchase(PurchaseKind.HARDWARE,3,"Computer","PC",10000,"EUR","2021-03-01"),Purchase(PurchaseKind.GAME,4,"Japan","PS4",300000,"JPY","2021-03-02"),Purchase(PurchaseKind.GAME,5,"Unknown","PC",500,"EUR"))
        compose.setContent{GamerDexTheme{SpendingScreen(emptyList(),emptyList(),entries,{}, {})}}
        compose.onNodeWithText("Año: Todos los años").performClick();compose.onNode(hasText("2021") and hasClickAction()).performClick()
        compose.onNodeWithText("Plataforma: Todas las plataformas").performClick();compose.onNode(hasText("PC") and hasClickAction()).performClick()
        compose.onNodeWithTag("spending-selected-total-EUR").assertTextEquals("${formatPrice(12000,"EUR")} EUR")
        compose.waitForIdle()
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val dir=File(instrumentation.targetContext.getExternalFilesDir(null),"render-tests").apply{mkdirs()}
        instrumentation.uiAutomation.takeScreenshot().let{bitmap->File(dir,"spending-filters.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
        compose.onNodeWithText("Año: 2021").performClick();compose.onNode(hasText("Año desconocido") and hasClickAction()).performClick()
        compose.onNodeWithTag("spending-selected-total-EUR").assertTextEquals("${formatPrice(500,"EUR")} EUR")
        compose.onNodeWithText("Año: Año desconocido").performClick();compose.onNode(hasText("2020") and hasClickAction()).performClick()
        compose.onNodeWithText("Plataforma: PC").performClick();compose.onNode(hasText("PS4") and hasClickAction()).performClick()
        compose.onNodeWithText("Sin compras para estos filtros.").assertExists()
        compose.onNodeWithText("Limpiar filtros de gasto").performClick()
        compose.onNodeWithTag("spending-selection-summary").assert(hasAnyDescendant(hasText("JPY",substring=true)))
    }
}
