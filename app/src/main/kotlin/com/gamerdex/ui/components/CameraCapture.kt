package com.gamerdex.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

@Composable fun CameraCaptureButton(enabled: Boolean, onCaptured: (String) -> Unit) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    var denied by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        visible = granted; denied = !granted
    }
    OutlinedButton(onClick = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) visible = true
        else permission.launch(Manifest.permission.CAMERA)
    }, enabled = enabled) { Text("Tomar foto") }
    if (denied) {
        Text("La cámara necesita permiso. Puedes seguir eligiendo fotos de tu biblioteca.", color = MaterialTheme.colorScheme.error)
        TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }) { Text("Abrir permisos") }
    }
    if (visible) CameraCaptureDialog(onCaptured = { visible = false; onCaptured(it) }, close = { visible = false })
}

@Composable private fun CameraCaptureDialog(onCaptured: (String) -> Unit, close: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE; scaleType = PreviewView.ScaleType.FIT_CENTER } }
    val preview = remember { Preview.Builder().build() }
    val capture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val active = remember { AtomicBoolean(true) }
    var ready by remember { mutableStateOf(false) }
    var taking by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    DisposableEffect(lifecycle) {
        var provider: ProcessCameraProvider? = null
        active.set(true)
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (active.get()) try {
                provider = future.get()
                val selector = when {
                    provider!!.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                    provider!!.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> throw IllegalStateException("No camera")
                }
                preview.setSurfaceProvider(previewView.surfaceProvider)
                provider!!.bindToLifecycle(lifecycle, selector, preview, capture)
                ready = true
            } catch (_: Exception) { error = "No se pudo abrir la cámara. Puedes elegir una foto de la biblioteca." }
        }, ContextCompat.getMainExecutor(context))
        onDispose { active.set(false); provider?.unbind(preview, capture) }
    }
    Dialog(onDismissRequest = { if (!taking) close() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Fotografía tu copia", style = MaterialTheme.typography.titleLarge)
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().weight(1f))
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = {
                    taking = true; error = null
                    capture.targetRotation = previewView.display?.rotation ?: android.view.Surface.ROTATION_0
                    val directory = File(context.filesDir, "photos")
                    if (!directory.exists() && !directory.mkdirs()) { taking = false; error = "No se pudo preparar el almacenamiento." }
                    else {
                        val file = File(directory, "${UUID.randomUUID()}.jpg")
                        try { capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                if (active.get()) { taking = false; onCaptured(Uri.fromFile(file).toString()) }
                                else file.delete()
                            }
                            override fun onError(exception: ImageCaptureException) {
                                file.delete()
                                if (active.get()) { taking = false; error = "No se pudo tomar la foto. Inténtalo de nuevo." }
                            }
                        }) } catch (_: Exception) {
                            file.delete(); taking = false; error = "La cámara no está disponible. Revisa su permiso y vuelve a intentarlo."
                        }
                    }
                }, enabled = ready && !taking, modifier = Modifier.fillMaxWidth()) { Text(if (taking) "Guardando foto…" else "Capturar foto") }
                TextButton(onClick = close, enabled = !taking) { Text("Cancelar cámara") }
            }
        }
    }
}
