package com.parodison.sgp4.propagation

import com.parodison.sgp4.init.SecularDragCoefficients
import kotlin.math.cos
import kotlin.math.sin

internal data class LongPeriodPeriodics(
    val axnl: Double,
    val aynl: Double,
    val meanLongitude: Double,
)

/** Agrega las correcciones periódicas de largo período (J3) antes de resolver Kepler. */
internal fun applyLongPeriodPeriodics(
    secularEffects: SecularEffects,
    drag: SecularDragCoefficients,
): LongPeriodPeriodics {
    val axnl = secularEffects.eccentricity * cos(secularEffects.argPerigee)
    val temp = 1.0 / (secularEffects.semiMajorAxis * (1.0 - secularEffects.eccentricity * secularEffects.eccentricity))
    val aynl = secularEffects.eccentricity * sin(secularEffects.argPerigee) + temp * drag.aycof
    val meanLongitude = secularEffects.meanAnomaly + secularEffects.argPerigee + secularEffects.node +
        temp * drag.xlcof * axnl

    return LongPeriodPeriodics(axnl, aynl, meanLongitude)
}
