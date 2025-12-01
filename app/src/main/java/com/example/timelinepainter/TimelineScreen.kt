package com.example.timelinepainter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun TimelineScreen() {
    val timelineState = rememberTimelineState()
    var showOverlay by remember { mutableStateOf(true) }

    // Constants
    val stages = Stage.values()
    val startTimeHour = 12
    val endTimeHour = 23
    val totalHours = endTimeHour - startTimeHour

    // Dimensions
    val baseHourHeight = 100.dp
    val stageHeaderHeight = 56.dp
    val gridTopMargin = 28.dp
    val timeColumnWidth = 44.dp
    val timeColumnLeftPadding = 12.dp

    // Colors
    val creamBackground = Color(0xFFFFFFF0)
    val darkBrownText = Color(0xFF421E17)
    val gridLineColor = Color(0xFFE8DBC3)
    val timeLabelColor = Color(0xFF786B68)
    val overlayColor = Color(0xBF221513)

    LaunchedEffect(Unit) {
        delay(2500)
        showOverlay = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(creamBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Title
            Text(
                text = "Festival Schedule",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = timeLabelColor
                ),
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp, bottom = 16.dp)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                val screenWidth = constraints.maxWidth.toFloat()
                val screenHeight = constraints.maxHeight.toFloat()

                val density = androidx.compose.ui.platform.LocalDensity.current
                val baseHourHeightPx = with(density) { baseHourHeight.toPx() }
                val stageHeaderHeightPx = with(density) { stageHeaderHeight.toPx() }
                val gridTopMarginPx = with(density) { gridTopMargin.toPx() }
                val timeColumnWidthPx = with(density) { (12.dp + (32.dp * timelineState.zoom)).toPx() }

                val baseColumnWidth = (screenWidth - timeColumnWidthPx) / stages.size

                val currentColumnWidth = baseColumnWidth * timelineState.zoom
                val currentHourHeight = baseHourHeightPx * timelineState.zoom

                val totalContentWidthUnzoomed = timeColumnWidthPx + (baseColumnWidth * stages.size)
                val totalContentHeightUnzoomed = stageHeaderHeightPx + gridTopMarginPx + (baseHourHeightPx * totalHours) + 50f

                val textMeasurer = rememberTextMeasurer()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                onGesture = { _, pan, zoomChange, _ ->
                                    timelineState.transform(
                                        panChange = pan,
                                        zoomChange = zoomChange,
                                        containerSize = Size(screenWidth, screenHeight),
                                        contentSize = Size(totalContentWidthUnzoomed, totalContentHeightUnzoomed)
                                    )
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.all { !it.pressed }) {
                                        timelineState.onInteractionEnd()
                                    }
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        clipRect {
                            withTransform({ translate(left = timelineState.offsetX, top = timelineState.offsetY) }) {

                                val totalHalfHours = totalHours * 2
                                for (i in 0..totalHalfHours) {
                                    val y = stageHeaderHeightPx + gridTopMarginPx + (i * (currentHourHeight / 2))
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(timeColumnWidthPx, y),
                                        end = Offset(timeColumnWidthPx + (stages.size * currentColumnWidth), y),
                                        strokeWidth = 1f
                                    )
                                }

                                stages.forEachIndexed { index, _ ->
                                    val x = timeColumnWidthPx + (index * currentColumnWidth)
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(x, stageHeaderHeightPx),
                                        end = Offset(x, stageHeaderHeightPx + gridTopMarginPx + (totalHours * currentHourHeight)),
                                        strokeWidth = 1f
                                    )
                                }
                                val lastX = timeColumnWidthPx + (stages.size * currentColumnWidth)
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(lastX, stageHeaderHeightPx),
                                    end = Offset(lastX, stageHeaderHeightPx + gridTopMarginPx + (totalHours * currentHourHeight)),
                                    strokeWidth = 1f
                                )

                                sampleEvents.forEach { event ->
                                    val stageIndex = stages.indexOf(event.stage)
                                    if (stageIndex != -1) {
                                        val startHourDiff = ChronoUnit.MINUTES.between(
                                            java.time.LocalTime.of(startTimeHour, 0),
                                            event.startTime
                                        ) / 60f
                                        val durationHours = ChronoUnit.MINUTES.between(event.startTime, event.endTime) / 60f

                                        val x = timeColumnWidthPx + (stageIndex * currentColumnWidth)
                                        val y = stageHeaderHeightPx + gridTopMarginPx + (startHourDiff * currentHourHeight)
                                        val width = currentColumnWidth
                                        val height = durationHours * currentHourHeight

                                        val padding = 4f * timelineState.zoom
                                        val eventRectSize = Size(width - 2 * padding, height - 2 * padding)
                                        val eventTopLeft = Offset(x + padding, y + padding)

                                        drawRoundRect(
                                            color = event.color,
                                            topLeft = eventTopLeft,
                                            size = eventRectSize,
                                            cornerRadius = CornerRadius(6f * timelineState.zoom)
                                        )

                                        clipRect(
                                            left = eventTopLeft.x,
                                            top = eventTopLeft.y,
                                            right = eventTopLeft.x + eventRectSize.width,
                                            bottom = eventTopLeft.y + eventRectSize.height
                                        ) {
                                            val timeString = "${event.startTime}-${event.endTime}"
                                            val timeText = textMeasurer.measure(
                                                text = timeString,
                                                style = TextStyle(
                                                    fontSize = (12 * timelineState.zoom).sp,
                                                    color = darkBrownText
                                                )
                                            )

                                            val artistText = textMeasurer.measure(
                                                text = event.artistName,
                                                style = TextStyle(
                                                    fontSize = (16 * timelineState.zoom).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = darkBrownText
                                                )
                                            )

                                            drawText(
                                                timeText,
                                                topLeft = eventTopLeft + Offset(8f, 8f)
                                            )
                                            drawText(
                                                artistText,
                                                topLeft = eventTopLeft + Offset(8f, 8f + timeText.size.height)
                                            )
                                        }
                                    }
                                }
                            }

                            drawRect(
                                color = creamBackground,
                                topLeft = Offset(0f, 0f),
                                size = Size(screenWidth, stageHeaderHeightPx)
                            )

                            withTransform({ translate(left = timelineState.offsetX, top = 0f) }) {
                                stages.forEachIndexed { index, stage ->
                                    val x = timeColumnWidthPx + (index * currentColumnWidth)

                                    val textResult = textMeasurer.measure(
                                        text = stage.displayName,
                                        style = TextStyle(
                                            fontSize = (18 * timelineState.zoom).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = darkBrownText
                                        )
                                    )

                                    drawText(
                                        textResult,
                                        topLeft = Offset(
                                            x + (currentColumnWidth - textResult.size.width) / 2,
                                            (stageHeaderHeightPx - textResult.size.height) / 2
                                        )
                                    )
                                }
                            }

                            drawRect(
                                color = creamBackground,
                                topLeft = Offset(0f, stageHeaderHeightPx),
                                size = Size(timeColumnWidthPx, screenHeight)
                            )

                            withTransform({ translate(left = 0f, top = timelineState.offsetY) }) {
                                val timeColumnLeftPaddingPx = with(density) { timeColumnLeftPadding.toPx() }
                                for (i in 0..totalHours) {
                                    val y = stageHeaderHeightPx + gridTopMarginPx + (i * currentHourHeight)
                                    val timeText = String.format("%02d:00", startTimeHour + i)
                                    val textResult = textMeasurer.measure(
                                        text = timeText,
                                        style = TextStyle(
                                            fontSize = (10 * timelineState.zoom).sp,
                                            color = timeLabelColor
                                        )
                                    )

                                    drawText(
                                        textResult,
                                        topLeft = Offset(
                                            timeColumnLeftPaddingPx,
                                            y - (textResult.size.height / 2)
                                        )
                                    )
                                }
                            }

                            drawRect(
                                color = creamBackground,
                                topLeft = Offset(0f, 0f),
                                size = Size(timeColumnWidthPx, stageHeaderHeightPx)
                            )
                        }
                    }
                }

                // Overlay (Pinch to Zoom)
                androidx.compose.animation.AnimatedVisibility(
                    visible = showOverlay,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = stageHeaderHeight)
                            .background(overlayColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pinch to Zoom",
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Zoom Indicator
        var showZoomIndicator by remember { mutableStateOf(false) }
        
        LaunchedEffect(timelineState.isZooming) {
            if (timelineState.isZooming) {
                showZoomIndicator = true
            } else {
                delay(1500)
                showZoomIndicator = false
            }
        }

        AnimatedVisibility(
            visible = showZoomIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 108.dp, height = 32.dp)
                    .background(
                        color = Color(0xBF221513),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val zoomPercent = (timelineState.zoom * 10).roundToInt() * 10
                Text(
                    text = "Zoom: $zoomPercent%",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFFFFFFF0),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimelineScreenPreview() {
    TimelineScreen()
}
