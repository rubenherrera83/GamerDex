package com.gamerdex.ui.components

import androidx.compose.foundation.layout.size
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

@Composable fun CollectionNavigation(selected: String?, navigate: (String) -> Unit, more: () -> Unit) {
    NavigationBar {
        listOf("systems" to "Sistemas","vault" to "Juegos","statistics" to "Estadísticas","more" to "Más").forEachIndexed { index,(route,title) ->
            NavigationBarItem(selected=selected==route,onClick={if(route=="more")more()else navigate(route)},
                icon={Icon(navigationIcons[index],contentDescription=null,modifier=Modifier.size(24.dp))},
                label={Text(title,maxLines=1)},modifier=Modifier.testTag("nav-$route"))
        }
    }
}
