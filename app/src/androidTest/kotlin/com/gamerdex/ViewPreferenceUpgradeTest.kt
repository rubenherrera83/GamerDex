package com.gamerdex

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.ui.AppPreferences
import com.gamerdex.ui.CollectionView
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewPreferenceUpgradeTest {
    @Test fun legacyChoiceSurvivesAndNewGridIsRemembered() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = context.getSharedPreferences("gamerdex_preferences", 0)
        val oldMode = store.getString("collection_view", null)
        val hadHorizontal = store.contains("horizontal_collection")
        val oldHorizontal = store.getBoolean("horizontal_collection", false)
        try {
            store.edit().remove("collection_view").putBoolean("horizontal_collection", false).commit()
            assertEquals(CollectionView.LIST, AppPreferences(context).collectionView.value)
            store.edit().putBoolean("horizontal_collection", true).commit()
            assertEquals(CollectionView.COVER_FLOW, AppPreferences(context).collectionView.value)
            AppPreferences(context).setCollectionView(CollectionView.GRID)
            assertEquals(CollectionView.GRID, AppPreferences(context).collectionView.value)
        } finally {
            store.edit().apply {
                if (oldMode == null) remove("collection_view") else putString("collection_view", oldMode)
                if (hadHorizontal) putBoolean("horizontal_collection", oldHorizontal) else remove("horizontal_collection")
            }.commit()
        }
    }
}
