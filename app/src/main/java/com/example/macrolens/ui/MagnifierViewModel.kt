package com.example.macrolens.ui

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.FocusMeteringResult
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

enum class CameraError {
    INITIALIZATION,
    OPEN
}

enum class FreezeFilter {
    NONE,
    GRAYSCALE,
    INVERTED
}

data class MagnifierUiState(
    val zoom: Float = 0f,
    val isTorchOn: Boolean = false,
    val hasFlashUnit: Boolean = false,
    val isFrozen: Boolean = false,
    val frozenImage: ImageBitmap? = null,
    val freezeFilter: FreezeFilter = FreezeFilter.NONE,
    val hasCameraPermission: Boolean = false,
    val minZoomRatio: Float = 1f,
    val maxZoomRatio: Float = 1f,
    val currentZoomRatio: Float = 1f,
    val error: CameraError? = null,
    val focusPoint: Offset? = null,
    val isFocusing: Boolean = false,
    val lastFocusSuccess: Boolean? = null,
    val lastFocusEventId: Int = 0,
    val useFrontCamera: Boolean = false,
    val supportsFrontCamera: Boolean = false,
    val isReadingLineEnabled: Boolean = false,
    val readingLineFraction: Float = 0.5f
)

internal fun zoomRatioForLinearZoom(minRatio: Float, maxRatio: Float, linearZoom: Float): Float {
    val clamped = linearZoom.coerceIn(0f, 1f)
    return minRatio + (maxRatio - minRatio) * clamped
}

class MagnifierViewModel : ViewModel() {

    private val _state = MutableStateFlow(MagnifierUiState())
    val state: StateFlow<MagnifierUiState> = _state.asStateFlow()

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var boundPreviewView: PreviewView? = null
    private var boundLifecycleOwner: LifecycleOwner? = null
    private val freezeMutex = Mutex()

    fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        boundLifecycleOwner = lifecycleOwner
        boundPreviewView = previewView
        val context = previewView.context.applicationContext
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = try {
                providerFuture.get()
            } catch (t: Throwable) {
                _state.update { it.copy(error = CameraError.INITIALIZATION) }
                return@addListener
            }
            cameraProvider = provider
            val supportsFront = try {
                provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            } catch (_: Throwable) {
                false
            }
            val desiredSelector = if (_state.value.useFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            provider.unbindAll()
            camera = null
            val newPreview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cam = try {
                provider.bindToLifecycle(lifecycleOwner, desiredSelector, newPreview)
            } catch (t: Throwable) {
                _state.update { it.copy(supportsFrontCamera = supportsFront, error = CameraError.OPEN) }
                return@addListener
            }
            camera = cam
            boundLifecycleOwner = lifecycleOwner
            boundPreviewView = previewView
            _state.update {
                it.copy(
                    supportsFrontCamera = supportsFront,
                    hasFlashUnit = cam.cameraInfo.hasFlashUnit() && !it.useFrontCamera,
                    isTorchOn = if (it.useFrontCamera) false else it.isTorchOn,
                    zoom = 0f,
                    minZoomRatio = 1f,
                    maxZoomRatio = 1f,
                    currentZoomRatio = 1f,
                    error = null
                )
            }
            cam.cameraInfo.zoomState.observe(lifecycleOwner) { zs ->
                if (zs != null) {
                    val newMin = zs.minZoomRatio
                    val newMax = zs.maxZoomRatio
                    val currentLinear = _state.value.zoom
                    val ratio = newMin + (newMax - newMin) * currentLinear
                    _state.update {
                        it.copy(
                            minZoomRatio = newMin,
                            maxZoomRatio = newMax,
                            currentZoomRatio = ratio
                        )
                    }
                }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun onZoomChange(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        camera?.cameraControl?.setLinearZoom(clamped)
        _state.update {
            val ratio = zoomRatioForLinearZoom(it.minZoomRatio, it.maxZoomRatio, clamped)
            it.copy(zoom = clamped, currentZoomRatio = ratio)
        }
    }

    fun onPinchZoom(scale: Float) {
        if (scale <= 0f) return
        onZoomChange(_state.value.zoom + (scale - 1f) * 0.18f)
    }

    fun toggleCamera() {
        val owner = boundLifecycleOwner
        val preview = boundPreviewView
        val target = !_state.value.useFrontCamera
        _state.update { it.copy(useFrontCamera = target) }
        if (owner != null && preview != null) {
            bindCamera(owner, preview)
        }
    }

    fun retryCamera() {
        val owner = boundLifecycleOwner ?: return
        val preview = boundPreviewView ?: return
        cameraProvider?.unbindAll()
        camera = null
        bindCamera(owner, preview)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun toggleTorch() {
        val cam = camera ?: return
        if (_state.value.useFrontCamera) return
        val context = boundPreviewView?.context ?: return
        if (!_state.value.hasFlashUnit) return
        val next = !_state.value.isTorchOn
        val future = cam.cameraControl.enableTorch(next)
        future.addListener({
            try {
                future.get()
                _state.update { it.copy(isTorchOn = next) }
            } catch (_: Throwable) {
                _state.update { it.copy(isTorchOn = false) }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun toggleFreeze() {
        val previewView = boundPreviewView ?: return
        viewModelScope.launch {
            freezeMutex.withLock {
                val current = _state.value
                if (current.isFrozen) {
                    _state.update { it.copy(isFrozen = false, frozenImage = null, freezeFilter = FreezeFilter.NONE) }
                } else {
                    val img = withContext(Dispatchers.Main) {
                        previewView.getBitmap()?.asImageBitmap()
                    } ?: return@withLock
                    _state.update { it.copy(isFrozen = true, frozenImage = img, freezeFilter = FreezeFilter.NONE) }
                }
            }
        }
    }

    fun setFreezeFilter(filter: FreezeFilter) {
        _state.update { it.copy(freezeFilter = filter) }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(hasCameraPermission = granted) }
    }

    fun focusAt(x: Float, y: Float) {
        val cam = camera ?: return
        val pv = boundPreviewView ?: return
        if (pv.width <= 0 || pv.height <= 0) return
        val clampedX = x.coerceIn(0f, pv.width.toFloat())
        val clampedY = y.coerceIn(0f, pv.height.toFloat())
        val factory = pv.meteringPointFactory
        val point = factory.createPoint(clampedX, clampedY)
        val action = FocusMeteringAction.Builder(
            point,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
        ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
        _state.update { it.copy(focusPoint = Offset(clampedX, clampedY), isFocusing = true) }
        val future = cam.cameraControl.startFocusAndMetering(action)
        future.addListener({
            val success: Boolean? = try {
                future.get().isFocusSuccessful
            } catch (_: Throwable) {
                null
            }
            _state.update {
                it.copy(
                    isFocusing = false,
                    lastFocusSuccess = success,
                    lastFocusEventId = it.lastFocusEventId + 1
                )
            }
        }, ContextCompat.getMainExecutor(pv.context))
    }

    fun clearFocusIndicator() {
        _state.update { it.copy(focusPoint = null, isFocusing = false) }
    }

    fun setReadingLineEnabled(enabled: Boolean) {
        _state.update { it.copy(isReadingLineEnabled = enabled) }
    }

    fun setReadingLineFraction(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        _state.update { it.copy(readingLineFraction = clamped) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraProvider?.unbindAll()
        camera = null
        cameraProvider = null
        boundLifecycleOwner = null
        boundPreviewView = null
    }
}
