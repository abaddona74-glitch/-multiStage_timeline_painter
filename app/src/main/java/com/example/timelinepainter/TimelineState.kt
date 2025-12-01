package com.example.timelinepainter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min

class TimelineState(
    initialZoom: Float = 1f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f
) {
    var zoom by mutableFloatStateOf(initialZoom)
        private set
    var offsetX by mutableFloatStateOf(initialOffsetX)
        private set
    var offsetY by mutableFloatStateOf(initialOffsetY)
        private set
    
    var isInteracting by mutableStateOf(false)
        private set
    
    var isZooming by mutableStateOf(false)
        private set

    val minZoom = 1f
    val maxZoom = 2.5f

    fun transform(panChange: androidx.compose.ui.geometry.Offset, zoomChange: Float, containerSize: androidx.compose.ui.geometry.Size, contentSize: androidx.compose.ui.geometry.Size) {
        isInteracting = true
        if (kotlin.math.abs(zoomChange - 1f) > 0.0001f) {
            isZooming = true
        }
        val oldZoom = zoom
        val newZoom = (zoom * zoomChange).coerceIn(minZoom, maxZoom)
        
        val currentContentWidth = contentSize.width * newZoom
        val currentContentHeight = contentSize.height * newZoom

        val minOffsetX = min(0f, containerSize.width - currentContentWidth)
        val maxOffsetX = 0f
        val minOffsetY = min(0f, containerSize.height - currentContentHeight)
        val maxOffsetY = 0f

        zoom = newZoom

        var newOffsetX = offsetX + panChange.x
        var newOffsetY = offsetY + panChange.y

        if (currentContentWidth < containerSize.width) {
            newOffsetX = 0f
        }
        if (currentContentHeight < containerSize.height) {
            newOffsetY = 0f
        }

        offsetX = newOffsetX.coerceIn(minOffsetX, maxOffsetX)
        offsetY = newOffsetY.coerceIn(minOffsetY, maxOffsetY)
    }

    fun onInteractionEnd() {
        isInteracting = false
        isZooming = false
    }

    companion object {
        val Saver: Saver<TimelineState, *> = Saver(
            save = { listOf(it.zoom, it.offsetX, it.offsetY) },
            restore = { TimelineState(it[0] as Float, it[1] as Float, it[2] as Float) }
        )
    }
}

@Composable
fun rememberTimelineState(): TimelineState {
    return rememberSaveable(saver = TimelineState.Saver) {
        TimelineState()
    }
}
