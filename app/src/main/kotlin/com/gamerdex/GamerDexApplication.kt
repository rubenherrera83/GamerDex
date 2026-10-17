package com.gamerdex

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import android.content.Context
import okio.Path.Companion.toOkioPath

@HiltAndroidApp
class GamerDexApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
        .memoryCache { MemoryCache.Builder().maxSizePercent(context, .20).build() }
        .diskCache { DiskCache.Builder().directory(cacheDir.resolve("covers").toOkioPath()).maxSizeBytes(100L * 1024 * 1024).build() }
        .crossfade(true).build()
}
