package com.gamerdex.gamertag

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object GamertagSharing {
    suspend fun intent(context: Context, bitmap: Bitmap): Intent = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "shared-gamertags").apply { check(isDirectory || mkdirs()) }
        val expiry = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        directory.listFiles()?.filter { it.isFile && it.lastModified() < expiry }?.forEach { it.delete() }
        val file = File(directory, "GamerDex-${UUID.randomUUID()}.png")
        file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, "Mi gamertag GamerDex")
            clipData = ClipData.newUri(context.contentResolver, "Gamertag GamerDex", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
