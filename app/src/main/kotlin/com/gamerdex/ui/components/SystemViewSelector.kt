package com.gamerdex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gamerdex.ui.SystemView

@OptIn(ExperimentalLayoutApi::class)
@Composable fun SystemViewSelector(mode:SystemView,setMode:(SystemView)->Unit) {
    FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        SystemView.entries.forEach { candidate ->
            FilterChip(mode==candidate,{setMode(candidate)},label={Text(candidate.label)},modifier=Modifier.testTag("system-view-${candidate.name}"))
        }
    }
}
