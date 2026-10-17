package com.gamerdex.core.network

import android.content.Context
import com.gamerdex.core.domain.MetadataRepository
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object NetworkFactory {
    private val json = Json { ignoreUnknownKeys = true }
    fun create(context: Context, apiKey: String): MetadataRepository {
        val client = OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "metadata_http"), 20L * 1024 * 1024))
            .callTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val url = chain.request().url.newBuilder().addQueryParameter("key", apiKey).build()
                val request = chain.request().newBuilder().url(url).header("Accept", "application/json").build()
                try { chain.proceed(request) }
                catch (error: IOException) {
                    val cached = chain.proceed(request.newBuilder().cacheControl(CacheControl.Builder().onlyIfCached().maxStale(7, TimeUnit.DAYS).build()).build())
                    if (cached.code == 504) { cached.close(); throw error }
                    cached
                }
            }.build()
        val api = Retrofit.Builder().baseUrl("https://api.rawg.io/api/").client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(RawgApi::class.java)
        return RawgRepository(api, apiKey.isNotBlank())
    }
}
