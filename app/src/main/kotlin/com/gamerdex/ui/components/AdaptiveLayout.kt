package com.gamerdex.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable fun windowSizeDp():DpSize {
    val size=LocalWindowInfo.current.containerSize
    return with(LocalDensity.current){DpSize(size.width.toDp(),size.height.toDp())}
}

/** Height, rather than orientation, also covers small split-screen windows. */
@Composable fun isShortWindow():Boolean=windowSizeDp().height<480.dp
