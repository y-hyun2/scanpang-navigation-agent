package com.scanpang.app.screens.ar

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangThemeAccessor

/**
 * ARCore 연동 전: 후면 카메라 프리뷰만 표시 (RN AR 화면 대체).
 */
@Composable
fun ArCameraPreview(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val c = ScanPangThemeAccessor.colors

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { ok -> granted = ok }

    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = modifier.background(c.backgroundAR)) {
        if (!granted) {
            Text(
                "카메라 권한이 필요합니다.",
                color = c.textOnDark,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(ScanPangSpacing.S5),
            )
            return@Box
        }

        var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        previewViewRef?.let { pv ->
            CameraLifecycleBinder(previewView = pv, lifecycleOwner = lifecycleOwner)
        }
    }
}

@Composable
private fun CameraLifecycleBinder(
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
) {
    val context = LocalContext.current
    DisposableEffect(lifecycleOwner, previewView) {
        val executor = ContextCompat.getMainExecutor(context)
        val future = ProcessCameraProvider.getInstance(context)
        val bind = Runnable {
            runCatching {
                val provider = future.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                )
            }
        }
        future.addListener(bind, executor)
        onDispose {
            runCatching { future.get().unbindAll() }
        }
    }
}
