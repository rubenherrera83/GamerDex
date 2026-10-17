package com.gamerdex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.gamerdex.ui.theme.CyberCyan
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource

@Composable fun BrandLogo(name: String, url: String?, modifier: Modifier = Modifier, platform: Boolean = false) {
    val initials = name.split(' ', '-', '_').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) }.uppercase()
    val placeholder: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            val resource = if (platform) PlatformCatalog.logo(name) else null
            if (resource != null) Image(painterResource(resource), "Logotipo de $name", Modifier.fillMaxSize().padding(4.dp), colorFilter = ColorFilter.tint(CyberCyan))
            else Text(initials, color = CyberCyan, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
    Box(modifier.clip(RoundedCornerShape(6.dp))) {
        if (url == null) placeholder()
        else SubcomposeAsyncImage(model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
            contentDescription = "Logotipo de $name", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize(),
            loading = { Shimmer(Modifier.fillMaxSize()) }, error = { placeholder() })
    }
}
