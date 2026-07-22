package com.parodison.sgp4

import com.parodison.sgp4.constants.WGS72Constants.TWO_PI
import com.parodison.sgp4.constants.WGS72Constants.XKE
import com.parodison.sgp4.init.OrbitInitializer
import com.parodison.sgp4.init.OrbitRegime
import com.parodison.sgp4.init.SecularCoefficientsCalculator
import com.parodison.sgp4.model.MeanElements
import com.parodison.sgp4.model.PositionVelocity
import com.parodison.sgp4.propagation.KeplerSolver
import com.parodison.sgp4.propagation.SecularPropagator
import com.parodison.sgp4.propagation.applyLongPeriodPeriodics
import com.parodison.sgp4.propagation.computeOrientationVectors
import com.parodison.sgp4.propagation.computePositionAndVelocity
import com.parodison.sgp4.propagation.computeShortPeriodPeriodics
import kotlin.time.DurationUnit
import kotlin.time.Instant

/**
 * Propagador SGP4 (near-earth, período orbital < 225 min) según la revisión de
 * Vallado/Crawford/Hujsak/Kelso de Spacetrack Report #3 (WGS-72).
 *
 * Los parámetros ya deben venir convertidos a las unidades internas del algoritmo
 * (radianes, rad/min); ver [com.parodison.sgp4.model.MeanElements].
 */
class SGP4Engine(
    epoch: Instant,
    meanMotionRadPerMin: Double,
    eccentricity: Double,
    inclinationRad: Double,
    raanRad: Double,
    argPerigeeRad: Double,
    meanAnomalyRad: Double,
    bstar: Double,
) {
    private val meanElements = MeanElements(
        epoch = epoch,
        meanMotionRadPerMin = meanMotionRadPerMin,
        eccentricity = eccentricity,
        inclinationRad = inclinationRad,
        raanRad = raanRad,
        argPerigeeRad = argPerigeeRad,
        meanAnomalyRad = meanAnomalyRad,
        bstar = bstar,
    )

    private val recoveredElements = OrbitInitializer.recoverMeanMotionAndSemiMajorAxis(meanElements)
    private val orbitRegime = OrbitInitializer.classifyOrbitRegime(recoveredElements)
    private val secularGravity = SecularCoefficientsCalculator.computeSecularGravityCoefficients(recoveredElements)
    private val secularDrag = SecularCoefficientsCalculator.computeSecularDragCoefficients(meanElements, recoveredElements)

    init {
        check(orbitRegime == OrbitRegime.NEAR_EARTH) {
            "SGP4Engine sólo soporta órbitas near-earth (período orbital < 225 min). " +
                "Este conjunto de elementos requiere el modelo deep-space (SDP4), no implementado."
        }
    }

    /** Período orbital (minutos), a partir del mean motion ya corregido por el efecto Kozai. */
    val periodMinutes: Double = TWO_PI / recoveredElements.meanMotionRadPerMin

    /** Posición/velocidad (TEME, km y km/s) del satélite en el instante [at]. */
    fun propagate(at: Instant): PositionVelocity {
        val minutesSinceEpoch = (at - meanElements.epoch).toDouble(DurationUnit.MINUTES)

        val secularEffects = SecularPropagator.computeSecularEffects(
            minutesSinceEpoch, meanElements, recoveredElements, secularGravity, secularDrag,
        )
        val longPeriod = applyLongPeriodPeriodics(secularEffects, secularDrag)
        val eccentricAnomaly = KeplerSolver.solveKeplerEquation(
            longPeriod.meanLongitude, secularEffects.node, longPeriod.axnl, longPeriod.aynl,
        )
        val shortPeriod = computeShortPeriodPeriodics(
            secularEffects, longPeriod, eccentricAnomaly, recoveredElements, secularDrag,
            meanElements.inclinationRad, XKE,
        )
        val orientation = computeOrientationVectors(shortPeriod)
        return computePositionAndVelocity(shortPeriod, orientation)
    }
}