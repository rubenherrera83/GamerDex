package com.gamerdex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

private val navigationIcons = listOf(
    ImageVector.Builder("Systems",24.dp,24.dp,24f,24f).apply { path(fill=SolidColor(Color.Black)) {
        moveTo(5f,5f);lineTo(19f,5f);lineTo(22f,18f);lineTo(16f,18f);lineTo(14f,15f);lineTo(10f,15f);lineTo(8f,18f);lineTo(2f,18f);close()
        moveTo(7f,8f);lineTo(7f,10f);lineTo(5f,10f);lineTo(5f,12f);lineTo(7f,12f);lineTo(7f,14f);lineTo(9f,14f);lineTo(9f,12f);lineTo(11f,12f);lineTo(11f,10f);lineTo(9f,10f);lineTo(9f,8f);close()
    }}.build(),
    ImageVector.Builder("Games",24.dp,24.dp,24f,24f).apply { path(fill=SolidColor(Color.Black)) {
        listOf(3f to 3f,13f to 3f,3f to 13f,13f to 13f).forEach{(x,y)->moveTo(x,y);lineTo(x+8,y);lineTo(x+8,y+8);lineTo(x,y+8);close()}
    }}.build(),
    ImageVector.Builder("Statistics",24.dp,24.dp,24f,24f).apply { path(fill=SolidColor(Color.Black)) {
        listOf(3f to 13f,10f to 7f,17f to 3f).forEach{(x,y)->moveTo(x,y);lineTo(x+4,y);lineTo(x+4,21f);lineTo(x,21f);close()}
    }}.build(),
    ImageVector.Builder("More",24.dp,24.dp,24f,24f).apply { path(fill=SolidColor(Color.Black)) {
        listOf(3f,10f,17f).forEach{x->moveTo(x,10f);lineTo(x+4,10f);lineTo(x+4,14f);lineTo(x,14f);close()}
    }}.build(),
)

@Composable fun CollectionNavigation(selected:String?,navigate:(String)->Unit,more:()->Unit){
    val destinations=listOf("systems" to "Sistemas","vault" to "Juegos","statistics" to "Estadísticas","more" to "Más")
    fun open(route:String){if(route=="more")more()else navigate(route)}
    val short=isShortWindow()
    val measurer=rememberTextMeasurer()
    val density=LocalDensity.current
    val labelStyle=MaterialTheme.typography.labelMedium
    val labelWidth=with(density){destinations.maxOf{measurer.measure(AnnotatedString(it.second),labelStyle).size.width}.toDp()}+24.dp
    BoxWithConstraints {
        if(short || maxWidth/4<labelWidth){
            val columns=if(maxWidth-16.dp>=labelWidth*4+24.dp)4 else if(maxWidth-16.dp>=labelWidth*2+8.dp)2 else 1
            Surface(color=MaterialTheme.colorScheme.surface){
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=8.dp,vertical=4.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                    destinations.chunked(columns).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        row.forEach{(route,title)->Surface(color=if(selected==route)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            contentColor=if(selected==route)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape=MaterialTheme.shapes.small,modifier=Modifier.weight(1f).testTag("nav-$route")
                                .selectable(selected==route,role=Role.Tab,onClick={open(route)})){
                            Box(Modifier.fillMaxWidth().heightIn(min=48.dp).padding(8.dp),contentAlignment=androidx.compose.ui.Alignment.Center){Text(title,modifier=Modifier.fillMaxWidth(),style=labelStyle,textAlign=TextAlign.Center)}
                        }}
                    }}
                }
            }
        }else NavigationBar {
            destinations.forEachIndexed{index,(route,title)->NavigationBarItem(selected=selected==route,onClick={open(route)},
                icon={Icon(navigationIcons[index],contentDescription=null,modifier=Modifier.size(24.dp))},label={Text(title,maxLines=1)},modifier=Modifier.testTag("nav-$route"))}
        }
    }
}
