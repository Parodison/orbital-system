package com.parodison.sgp4.propagation

import com.parodison.sgp4.constants.WGS72Constants.J2
import com.parodison.sgp4.init.RecoveredElements
import com.parodison.sgp4.init.SecularDragCoefficients
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal data class ShortPeriodState(
    val radius: Double, // en radios terrestres
    val argLatitude: Double,
    val node: Double,
    val inclination: Double,
    val radialVelocity: Double,
    val transverseVelocity: Double,
)

/**
 * Agrega las correcciones periódicas de corto período (variación dentro de una sola
 * órbita) al radio, inclinación, nodo y argumento del perigeo.
 */
internal fun computeShortPeriodPeriodics(
    secularEffects: SecularEffects,
    longPeriod: LongPeriodPeriodics,
    eccentricAnomaly: Double,
    recovered: RecoveredElements,
    drag: SecularDragCoefficients,
    inclinationRad: Double,
    xke: Double,
): ShortPeriodState {
    val axnl = longPeriod.axnl
    val aynl = longPeriod.aynl
    val sineo1 = sin(eccentricAnomaly)
    val coseo1 = cos(eccentricAnomaly)

    val am = secularEffects.semiMajorAxis
    val nm = secularEffects.meanMotion

    val ecose = axnl * coseo1 + aynl * sineo1
    val esine = axnl * sineo1 - aynl * coseo1
    val el2 = axnl * axnl + aynl * aynl
    val pl = am * (1.0 - el2)
    check(pl >= 0.0) { "Semi-latus rectum negativo: la órbita degeneró durante la propagación" }

    val rl = am * (1.0 - ecose)
    val rdotl = sqrt(am) * esine / rl
    val rvdotl = sqrt(pl) / rl
    val betal = sqrt(1.0 - el2)
    val temp = esine / (1.0 + betal)
    val sinu = am / rl * (sineo1 - aynl - axnl * temp)
    val cosu = am / rl * (coseo1 - axnl + aynl * temp)
    var su = atan2(sinu, cosu)
    val sin2u = (cosu + cosu) * sinu
    val cos2u = 1.0 - 2.0 * sinu * sinu
    val temp1 = 0.5 * J2 / pl
    val temp2 = temp1 / pl

    val mrt = rl * (1.0 - 1.5 * temp2 * betal * recovered.con41) + 0.5 * temp1 * drag.x1mth2 * cos2u
    su -= 0.25 * temp2 * drag.x7thm1 * sin2u
    val xnode = secularEffects.node + 1.5 * temp2 * recovered.cosInclination * sin2u
    val xinc = inclinationRad + 1.5 * temp2 * recovered.cosInclination * recovered.sinInclination * cos2u
    val mvt = rdotl - nm * temp1 * drag.x1mth2 * sin2u / xke
    val rvdot = rvdotl + nm * temp1 * (drag.x1mth2 * cos2u + 1.5 * recovered.con41) / xke

    return ShortPeriodState(
        radius = mrt,
        argLatitude = su,
        node = xnode,
        inclination = xinc,
        radialVelocity = mvt,
        transverseVelocity = rvdot,
    )
}
