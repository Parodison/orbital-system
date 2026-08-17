package com.parodison.orbital.system.core

import com.parodison.orbit.core.groundstation.dto.RotatorPosition
import kotlin.math.pow
import kotlin.math.round

fun Double.roundTo(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return round(this * factor) / factor
}

fun RotatorPosition?.toDisplayString(): String {
    if (this == null) return "Azimuth: --°, Elevación: --°"
    return "Azimuth: ${azimuthDegrees.roundTo(2)}°, Elevación: ${elevationDegrees.roundTo(2)}°"
}