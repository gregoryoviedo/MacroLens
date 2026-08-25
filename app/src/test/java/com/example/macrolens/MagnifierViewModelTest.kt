package com.example.macrolens

import com.example.macrolens.ui.FreezeFilter
import com.example.macrolens.ui.MagnifierViewModel
import com.example.macrolens.ui.zoomRatioForLinearZoom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MagnifierViewModelTest {
    @Test
    fun zoomRatioIsClampedToHardwareRange() {
        assertEquals(1f, zoomRatioForLinearZoom(1f, 10f, -1f), 0.001f)
        assertEquals(10f, zoomRatioForLinearZoom(1f, 10f, 2f), 0.001f)
        assertEquals(5.5f, zoomRatioForLinearZoom(1f, 10f, 0.5f), 0.001f)
    }

    @Test
    fun pinchZoomUpdatesLinearStateWithoutHardwareCamera() {
        val viewModel = MagnifierViewModel()

        viewModel.onPinchZoom(2f)

        assertEquals(0.18f, viewModel.state.value.zoom, 0.001f)
        assertTrue(viewModel.state.value.currentZoomRatio >= 1f)
    }

    @Test
    fun permissionStateCanBeUpdated() {
        val viewModel = MagnifierViewModel()

        viewModel.onPermissionResult(true)

        assertTrue(viewModel.state.value.hasCameraPermission)
    }

    @Test
    fun readingLineStateTogglesAndClampsFraction() {
        val viewModel = MagnifierViewModel()

        assertFalse(viewModel.state.value.isReadingLineEnabled)

        viewModel.setReadingLineEnabled(true)
        assertTrue(viewModel.state.value.isReadingLineEnabled)

        viewModel.setReadingLineFraction(-0.5f)
        assertEquals(0f, viewModel.state.value.readingLineFraction, 0.001f)

        viewModel.setReadingLineFraction(1.7f)
        assertEquals(1f, viewModel.state.value.readingLineFraction, 0.001f)

        viewModel.setReadingLineFraction(0.42f)
        assertEquals(0.42f, viewModel.state.value.readingLineFraction, 0.001f)
    }

    @Test
    fun freezeFilterUpdatesState() {
        val viewModel = MagnifierViewModel()

        assertEquals(FreezeFilter.NONE, viewModel.state.value.freezeFilter)

        viewModel.setFreezeFilter(FreezeFilter.GRAYSCALE)
        assertEquals(FreezeFilter.GRAYSCALE, viewModel.state.value.freezeFilter)

        viewModel.setFreezeFilter(FreezeFilter.INVERTED)
        assertEquals(FreezeFilter.INVERTED, viewModel.state.value.freezeFilter)

        viewModel.setFreezeFilter(FreezeFilter.NONE)
        assertEquals(FreezeFilter.NONE, viewModel.state.value.freezeFilter)
    }

    @Test
    fun toggleCameraFlipsFrontCameraFlagWithoutBinding() {
        val viewModel = MagnifierViewModel()

        assertFalse(viewModel.state.value.useFrontCamera)

        viewModel.toggleCamera()
        assertTrue(viewModel.state.value.useFrontCamera)

        viewModel.toggleCamera()
        assertFalse(viewModel.state.value.useFrontCamera)
    }
}
