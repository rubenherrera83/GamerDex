package com.gamerdex.core.network

import com.gamerdex.core.domain.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.HttpException
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@Serializable data class ScanDexPlatform(val id: Long, val name: String)
@Serializable data class ScanDexGame(val name: String, val platform: ScanDexPlatform)
@Serializable data class ScanDexResponse(@SerialName("igdb_metadata") val metadata: ScanDexGame? = null)
interface ScanDexApi {
    @GET("lookup") suspend fun lookup(@Query("value") value: String, @Header("Authorization") token: String): ScanDexResponse
}
class ScanDexRepository(private val api: ScanDexApi, private val token: String) : BarcodeRepository {
    override suspend fun lookup(code: String): BarcodeMatch? {
        require(validBarcode(code)) { "Código EAN/UPC inválido. Comprueba todos los dígitos." }
        check(token.isNotBlank()) { "ScanDex no está activado en esta instalación. Puedes buscar por título." }
        val response = try { api.lookup(code,token) } catch(e:HttpException) {
            when(e.code()) { 404 -> return null; 401,403 -> error("ScanDex no ha autorizado el token configurado."); 429 -> error("Has alcanzado el límite de consultas de ScanDex. Reintenta más tarde."); else -> throw e }
        }
        return response.metadata?.takeIf { it.name.isNotBlank() && it.platform.name.isNotBlank() }?.let { BarcodeMatch(it.name,it.platform.name) }
    }
    companion object {
        fun create(token:String): BarcodeRepository {
            val client=OkHttpClient.Builder().callTimeout(20,TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).build()
            return ScanDexRepository(Retrofit.Builder().baseUrl("https://scandex.gamery.app/api/v2/").client(client)
                .addConverterFactory(Json{ignoreUnknownKeys=true}.asConverterFactory("application/json".toMediaType())).build().create(ScanDexApi::class.java),token)
        }
    }
}
