package com.gamerdex

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.gamerdex.ui.*
import com.gamerdex.ui.theme.VisualTheme
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.util.UUID

class StartScreenPreferenceTest {
    @Test fun freshInstallsUseSystemsWhileExistingPreferencesOrDatabaseKeepGames() {
        val base=InstrumentationRegistry.getInstrumentation().targetContext
        val stores=mutableListOf<android.content.SharedPreferences>();val files=mutableListOf<File>()
        fun isolated():Context {
            val suffix=UUID.randomUUID().toString()
            return object:ContextWrapper(base){
                override fun getSharedPreferences(name:String,mode:Int)=base.getSharedPreferences("$name-$suffix",mode).also{if(it !in stores)stores+=it}
                override fun getDatabasePath(name:String)=File(base.cacheDir,"$suffix-$name").also{if(it !in files)files+=it}
            }
        }
        try {
            val fresh=isolated();val settings=AppPreferences(fresh);assertEquals(StartScreen.SYSTEMS,settings.startScreen.value)
            settings.select(VisualTheme.SEGA);assertEquals(StartScreen.SYSTEMS,AppPreferences(fresh).startScreen.value)
            settings.setStartScreen(StartScreen.GAMES);assertEquals(StartScreen.GAMES,AppPreferences(fresh).startScreen.value)
            fresh.getSharedPreferences("gamerdex_preferences",0).edit().putString("start_screen","FUTURE_VALUE").commit()
            assertEquals(StartScreen.SYSTEMS,AppPreferences(fresh).startScreen.value)
            val legacy=isolated();legacy.getSharedPreferences("gamerdex_preferences",0).edit().putBoolean("horizontal_collection",true).commit()
            assertEquals(StartScreen.GAMES,AppPreferences(legacy).startScreen.value);assertEquals(CollectionView.COVER_FLOW,AppPreferences(legacy).collectionView.value)
            val withDatabase=isolated();withDatabase.getDatabasePath("gamerdex.db").writeText("existing database marker")
            assertEquals(StartScreen.GAMES,AppPreferences(withDatabase).startScreen.value)
        }finally{stores.forEach{it.edit().clear().commit()};files.forEach{it.delete()}}
    }
}
