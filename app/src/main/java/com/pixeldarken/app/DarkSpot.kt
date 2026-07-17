package com.pixeldarken.app

data class DarkSpot(
    val id: Long,
    val xPx: Int,
    val yPx: Int,
    val radiusPx: Int = 24,
    val alpha: Int = 220
)
