package com.gamerdex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamerdex.branding.GamerDexLogo

/** Shared, fixed screen chrome. The wordmark follows the theme; titles remain legible. */
@Composable fun ScreenHeader(title:String,menu:(()->Unit)?=null,back:(()->Unit)?=null,
    navigationEnabled:Boolean=true,actions:@Composable RowScope.()->Unit={}) {
    val short=isShortWindow()
    Surface(color=MaterialTheme.colorScheme.surface,modifier=Modifier.testTag("screen-header")) {
        Row(Modifier.statusBarsPadding().fillMaxWidth().heightIn(min=if(short)64.dp else 80.dp).padding(end=8.dp),
            verticalAlignment=Alignment.CenterVertically) {
            if(back!=null)TextButton(onClick=back,enabled=navigationEnabled){Text("Volver")}
            else if(menu!=null)IconButton(onClick=menu,enabled=navigationEnabled,
                modifier=Modifier.semantics{contentDescription="Abrir menú"}){
                Text("☰",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.headlineSmall)
            }
            Column(Modifier.weight(1f).padding(horizontal=8.dp,vertical=if(short)6.dp else 10.dp),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                GamerDexLogo(Modifier.widthIn(max=if(short)112.dp else 148.dp).fillMaxWidth())
                Text(title,modifier=Modifier.testTag("screen-header-title").semantics{heading()},
                    fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.SemiBold,fontSize=20.sp,lineHeight=25.sp,
                    maxLines=2,overflow=TextOverflow.Ellipsis)
            }
            actions()
        }
    }
}
