package com.gamerdex.core.network

import com.gamerdex.core.domain.*
import kotlinx.coroutines.CancellationException

class CombinedBarcodeRepository(private val scanDex:BarcodeRepository,private val upcItemDb:BarcodeRepository):BarcodeRepository {
    override suspend fun lookup(code:String):BarcodeMatch? {
        require(validBarcode(code)){"Código EAN/UPC inválido. Comprueba todos los dígitos."}
        var scanFailure:String?=null
        try { scanDex.lookup(code)?.let{return it} }
        catch(e:CancellationException){throw e}
        catch(e:Exception){scanFailure=if(e is IllegalStateException)e.message else "No se pudo consultar ScanDex."}
        val fallback=try {upcItemDb.lookup(code)} catch(e:CancellationException){throw e} catch(e:Exception) {
            val reason=if(e is IllegalStateException)e.message else "No se pudo consultar UPCitemdb."
            error(listOfNotNull(scanFailure ?: "ScanDex no identificó este código.",reason,"Puedes buscar por título en RAWG o continuar manualmente.").joinToString(" "))
        }
        if(fallback!=null)return fallback.copy(notice=scanFailure?.let{"$it Se ha usado UPCitemdb como respaldo."})
        if(scanFailure!=null)error("$scanFailure UPCitemdb no encontró este código. Puedes buscar por título en RAWG o continuar manualmente.")
        return null
    }
    companion object {
        fun create(scanDexToken:String):BarcodeRepository=CombinedBarcodeRepository(ScanDexRepository.create(scanDexToken),UpcItemDbRepository.create())
    }
}
