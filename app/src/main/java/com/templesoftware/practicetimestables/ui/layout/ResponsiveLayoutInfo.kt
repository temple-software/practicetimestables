package com.templesoftware.practicetimestables.ui.layout

import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo

data class ResponsiveLayoutInfo(
    val orientation: Int,
    val widthDp: Int,
    val heightDp: Int,
    val displayRotation: Int,
) {
    val isLandscape: Boolean get() = orientation == Configuration.ORIENTATION_LANDSCAPE
    val isPortrait: Boolean get() = orientation == Configuration.ORIENTATION_PORTRAIT
    val isExpandedWidth: Boolean get() = widthDp >= 600
    val isReverseRotation: Boolean
        get() = displayRotation == Surface.ROTATION_180 || displayRotation == Surface.ROTATION_270
}

@Composable
fun currentResponsiveLayoutInfo(): ResponsiveLayoutInfo {
    val configuration = LocalConfiguration.current
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val view = LocalView.current
    val displayManager = remember(view) {
        view.context.getSystemService(DisplayManager::class.java)
    }
    var displayRotation by remember(view) {
        mutableIntStateOf(view.display?.rotation ?: Surface.ROTATION_0)
    }

    DisposableEffect(view, displayManager) {
        val displayId = view.display?.displayId
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit

            override fun onDisplayChanged(changedDisplayId: Int) {
                if (displayId == null || changedDisplayId == displayId) {
                    displayRotation = view.display?.rotation ?: Surface.ROTATION_0
                }
            }
        }
        displayRotation = view.display?.rotation ?: Surface.ROTATION_0
        displayManager.registerDisplayListener(listener, null)
        onDispose { displayManager.unregisterDisplayListener(listener) }
    }

    return ResponsiveLayoutInfo(
        orientation = configuration.orientation,
        widthDp = with(density) { containerSize.width.toDp().value.toInt() },
        heightDp = with(density) { containerSize.height.toDp().value.toInt() },
        displayRotation = displayRotation,
    )
}
