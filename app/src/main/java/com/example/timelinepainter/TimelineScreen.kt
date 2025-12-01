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
    val baseHourHeight = 100f
    val stageHeaderHeight = 60f
    val timeColumnWidth = 80f // Increased from 50f to prevent overlap
    // Removed topBarHeight as title is removed

    // Colors (Light / Cream Theme)
    val creamBackground = Color(0xFFFFFBF2)
    val darkBrownText = Color(0xFF3E2723)
    val gridLineColor = Color(0xFFE0E0E0) // Very faint
    val timeLabelColor = Color(0xFF757575)
    val overlayColor = Color(0x993E2723) // Semi-transparent brown

    LaunchedEffect(Unit) {
        delay(2500)
        showOverlay = false
    }

    // ИСПРАВЛЕНИЕ: Оборачиваем все в Box для управления слоями
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(creamBackground)
    ) {
        // --- Слой с контентом (Временная шкала) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Timeline Area
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                val screenWidth = constraints.maxWidth.toFloat()
                val screenHeight = constraints.maxHeight.toFloat()

                val baseColumnWidth = (screenWidth - timeColumnWidth) / stages.size

                // Zoomed Dimensions
                val currentColumnWidth = baseColumnWidth * timelineState.zoom
                val currentHourHeight = baseHourHeight * timelineState.zoom

                val totalContentWidthUnzoomed = timeColumnWidth + (baseColumnWidth * stages.size)
                val totalContentHeightUnzoomed = stageHeaderHeight + (baseHourHeight * totalHours) + 50f

                val textMeasurer = rememberTextMeasurer()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoomChange, _ ->
                                timelineState.transform(
                                    panChange = pan,
                                    zoomChange = zoomChange,
                                    containerSize = Size(screenWidth, screenHeight),
                                    contentSize = Size(totalContentWidthUnzoomed, totalContentHeightUnzoomed)
                                )
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        clipRect {
                            // Content Layer (Scrolled)
                            withTransform({ translate(left = timelineState.offsetX, top = timelineState.offsetY) }) {

                                // 1. Grid Lines (Horizontal)
                                for (i in 0..totalHours) {
                                    val y = stageHeaderHeight + (i * currentHourHeight)
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(timeColumnWidth, y),
                                        end = Offset(timeColumnWidth + (stages.size * currentColumnWidth), y),
                                        strokeWidth = 1f
                                    )
                                }

                                // 2. Grid Lines (Vertical)
                                stages.forEachIndexed { index, _ ->
                                    val x = timeColumnWidth + (index * currentColumnWidth)
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x, stageHeaderHeight + (totalHours * currentHourHeight)),
                                        strokeWidth = 1f
                                    )
                                }
                                // Last vertical line
                                val lastX = timeColumnWidth + (stages.size * currentColumnWidth)
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(lastX, 0f),
                                    end = Offset(lastX, stageHeaderHeight + (totalHours * currentHourHeight)),
                                    strokeWidth = 1f
                                )

                                // 3. Events
                                sampleEvents.forEach { event ->
                                    val stageIndex = stages.indexOf(event.stage)
                                    if (stageIndex != -1) {
                                        val startHourDiff = ChronoUnit.MINUTES.between(
                                            java.time.LocalTime.of(startTimeHour, 0),
                                            event.startTime
                                        ) / 60f
                                        val durationHours = ChronoUnit.MINUTES.between(event.startTime, event.endTime) / 60f

                                        val x = timeColumnWidth + (stageIndex * currentColumnWidth)
                                        val y = stageHeaderHeight + (startHourDiff * currentHourHeight)
                                        val width = currentColumnWidth
                                        val height = durationHours * currentHourHeight

                                        val padding = 4f * timelineState.zoom
                                        val eventRectSize = Size(width - 2 * padding, height - 2 * padding)
                                        val eventTopLeft = Offset(x + padding, y + padding)

                                        // Event Card
                                        drawRoundRect(
                                            color = event.color,
                                            topLeft = eventTopLeft,
                                            size = eventRectSize,
                                            cornerRadius = CornerRadius(6f * timelineState.zoom)
                                        )

                                        // Text
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
                                                    fontSize = (10 * timelineState.zoom).sp,
                                                    color = Color.Black.copy(alpha = 0.6f)
                                                )
                                            )

                                            val artistText = textMeasurer.measure(
                                                text = event.artistName,
                                                style = TextStyle(
                                                    fontSize = (14 * timelineState.zoom).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black.copy(alpha = 0.8f)
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

                            // Sticky Headers (Stages) - X scrolls, Y fixed
                            drawRect(
                                color = creamBackground,
                                topLeft = Offset(0f, 0f),
                                size = Size(screenWidth, stageHeaderHeight)
                            )

                            withTransform({ translate(left = timelineState.offsetX, top = 0f) }) {
                                stages.forEachIndexed { index, stage ->
                                    val x = timeColumnWidth + (index * currentColumnWidth)

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
                                            (stageHeaderHeight - textResult.size.height) / 2
                                        )
                                    )
                                }
                            }

                            // Sticky Time Column - Y scrolls, X fixed
                            drawRect(
                                color = creamBackground,
                                topLeft = Offset(0f, stageHeaderHeight),
                                size = Size(timeColumnWidth, screenHeight)
                            )

                            withTransform({ translate(left = 0f, top = timelineState.offsetY) }) {
                                for (i in 0..totalHours) {
                                    val y = stageHeaderHeight + (i * currentHourHeight)
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
                                            (timeColumnWidth - textResult.size.width) / 2,
                                            y - (textResult.size.height / 2)
                                        )
                                    )
                                }
                            }

                            // Corner Cover
                            drawRect(
                                color = creamBackground,
                                topLeft = Offset(0f, 0f),
                                size = Size(timeColumnWidth, stageHeaderHeight)
                            )
                        }
                    }
                }
            }
        }

        // --- Слой с подсказкой (Оверлей) ---
        // ИСПРАВЛЕНИЕ: AnimatedVisibility теперь находится в Box и не конфликтует по скоупу
        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pinch to Zoom",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
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
