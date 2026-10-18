package com.gamerdex.gamertag

import com.gamerdex.core.domain.Hardware
import com.gamerdex.ui.theme.VisualTheme

/** Public card fields only. Purchase details and serial numbers are never copied. */
fun hardwareGamertag(alias:String,item:Hardware,theme:VisualTheme,selected:List<Int>,includeNotes:Boolean=false):GamertagInput {
    val lines=selected.distinct().filter{it in item.controllers.indices}.take(10).map{index->
        val c=item.controllers[index]
        "${c.quantity} × ${c.name}"+if(includeNotes && c.notes.isNotBlank())" · ${c.notes}" else ""
    }
    return GamertagInput(alias,item.name,"${item.platform} / MI EQUIPO","",item.localPhotos.firstOrNull() ?: item.imageUrl,theme,hardwareCard=true,components=lines)
}
