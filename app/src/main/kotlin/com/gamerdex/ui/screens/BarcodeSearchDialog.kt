package com.gamerdex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gamerdex.core.domain.validBarcode
import com.google.mlkit.vision.codescanner.*
import com.google.mlkit.vision.barcode.common.Barcode

@Composable fun BarcodeSearchDialog(scanImmediately:Boolean=false,dismiss:()->Unit,lookup:(String)->Unit) {
    var code by rememberSaveable{mutableStateOf("")};var error by remember{mutableStateOf<String?>(null)}
    val context=LocalContext.current
    val scan:()->Unit={
        val options=GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_EAN_13,Barcode.FORMAT_EAN_8,Barcode.FORMAT_UPC_A).enableAutoZoom().build()
        GmsBarcodeScanning.getClient(context,options).startScan().addOnSuccessListener { result ->
            result.rawValue?.let { value->code=value;if(validBarcode(value))lookup(value)else error="La lectura no contiene un EAN/UPC válido. Revisa todas las cifras." }
        }.addOnFailureListener{error="No se pudo abrir el lector. Comprueba Google Play Services o escribe el código."}
    }
    LaunchedEffect(Unit){if(scanImmediately)scan()}
    AlertDialog(onDismissRequest=dismiss,title={Text("Identificar por código")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("Escanea o escribe todas las cifras del código de la caja, incluidas las que aparecen fuera de las barras. ScanDex no incluye todas las ediciones.")
        OutlinedTextField(code,{code=it.filter(Char::isDigit).take(14)},label={Text("Código EAN / UPC")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth(),isError=code.isNotEmpty() && !validBarcode(code),supportingText={if(code.isNotEmpty() && !validBarcode(code))Text("Código incompleto o dígito de control incorrecto. No se modifica automáticamente.")})
        TextButton(onClick=scan){Text("Escanear código")}
        error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
    }},confirmButton={TextButton(onClick={lookup(code)},enabled=validBarcode(code)){Text("Buscar código")}},dismissButton={TextButton(onClick=dismiss){Text("Cancelar")}})
}
