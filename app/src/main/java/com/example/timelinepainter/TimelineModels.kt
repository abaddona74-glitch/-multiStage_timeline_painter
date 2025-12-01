package com.example.timelinepainter

import androidx.compose.ui.graphics.Color
import java.time.LocalTime

enum class Stage(val displayName: String, val color: Color) {
    Main("Main", Color(0xFF8D6E63)),   // Brown/Rust
    Rock("Rock", Color(0xFF9575CD)),   // Muted Purple
    Electro("Electro", Color(0xFFC0CA33)) // Olive/Mustard
}

data class FestivalEvent(
    val artistName: String,
    val stage: Stage,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val color: Color = stage.color
)

val sampleEvents = listOf(
    FestivalEvent("DJ A", Stage.Electro, LocalTime.of(12, 0), LocalTime.of(13, 0)),
    FestivalEvent("Band X", Stage.Main, LocalTime.of(13, 0), LocalTime.of(14, 30)),
    FestivalEvent("RockZ", Stage.Rock, LocalTime.of(14, 0), LocalTime.of(15, 0)),
    FestivalEvent("Ambient Line", Stage.Electro, LocalTime.of(15, 0), LocalTime.of(16, 30)),
    FestivalEvent("Florence + The Machine", Stage.Main, LocalTime.of(16, 30), LocalTime.of(18, 0)),
    FestivalEvent("The National", Stage.Rock, LocalTime.of(17, 0), LocalTime.of(18, 0)),
    FestivalEvent("Jamie xx", Stage.Electro, LocalTime.of(18, 0), LocalTime.of(19, 0)),
    FestivalEvent("Tame Impala", Stage.Main, LocalTime.of(19, 0), LocalTime.of(20, 30)),
    FestivalEvent("Arctic Monkeys", Stage.Rock, LocalTime.of(20, 0), LocalTime.of(21, 30)),
    FestivalEvent("Radiohead", Stage.Main, LocalTime.of(21, 30), LocalTime.of(23, 0))
)
