package com.parodison.sgp4

import com.parodison.sgp4.constants.WGS72Constants.EARTH_RADIUS_KM
import com.parodison.sgp4.model.Vector3
import com.parodison.sgp4.time.greenwichSiderealTime
import com.parodison.sgp4.time.toJulianDate
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Instant

/**
 * Posición solar de baja precisión (Meeus, "Astronomical Algorithms" cap. 25; exactitud
 * ~0.01°, de sobra para iluminación/crepúsculo) y test de sombra cilíndrica de la Tierra.
 *
 * Trata el marco ecuatorial resultante como equivalente a TEME (el que usa [SGP4Engine]):
 * la diferencia real es solo nutación, del orden de arcosegundos — irrelevante frente a
 * la escala de grados que maneja este módulo.
 */
internal object SolarPosition {

    /** Posición geocéntrica del Sol (km) en el instante [at], en el mismo marco que TEME. */
    fun positionEci(at: Instant): Vector3 {
        val t = julianCenturies(at)

        val meanLongitudeDeg = normalizeDegrees(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val meanAnomalyDeg = normalizeDegrees(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val meanAnomalyRad = meanAnomalyDeg.toRadians()

        val equationOfCenterDeg = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(meanAnomalyRad) +
            (0.019993 - 0.000101 * t) * sin(2.0 * meanAnomalyRad) +
            0.000289 * sin(3.0 * meanAnomalyRad)

        val trueLongitudeRad = (meanLongitudeDeg + equationOfCenterDeg).toRadians()
        val obliquityRad = (23.439291 - 0.0130042 * t).toRadians()
        val distanceAu = 1.000140 - 0.016708 * cos(meanAnomalyRad) - 0.000141 * cos(2.0 * meanAnomalyRad)

        val rightAscensionRad = atan2(cos(obliquityRad) * sin(trueLongitudeRad), cos(trueLongitudeRad))
        val declinationRad = asin(sin(obliquityRad) * sin(trueLongitudeRad))

        val distanceKm = distanceAu * ASTRONOMICAL_UNIT_KM
        return Vector3(
            x = distanceKm * cos(declinationRad) * cos(rightAscensionRad),
            y = distanceKm * cos(declinationRad) * sin(rightAscensionRad),
            z = distanceKm * sin(declinationRad),
        )
    }

    /**
     * Modelo cilíndrico de sombra terrestre (sin penumbra/umbra cónica): un satélite está
     * eclipsado si está del lado nocturno y dentro del cilindro de radio = radio terrestre
     * proyectado en la dirección del Sol. Suficiente para "¿se puede ver a simple vista?".
     */
    fun isInEarthShadow(satellitePositionEci: Vector3, at: Instant): Boolean {
        val sunDirection = positionEci(at).normalized()
        val alongSunAxis = satellitePositionEci.dot(sunDirection)
        if (alongSunAxis > 0.0) return false

        val perpendicular = satellitePositionEci - sunDirection.scale(alongSunAxis)
        return perpendicular.magnitude() < EARTH_RADIUS_KM
    }

    /** Elevación del Sol (grados) vista desde [observer] en el instante [at]. */
    fun solarElevationDeg(observer: ObserverCoordinates, at: Instant): Double {
        val sun = positionEci(at)
        val rightAscensionRad = atan2(sun.y, sun.x)
        val declinationRad = asin(sun.z / sun.magnitude())

        val localSiderealTimeRad = greenwichSiderealTime(at.toJulianDate()) + observer.longitudeDeg.toRadians()
        val hourAngleRad = localSiderealTimeRad - rightAscensionRad
        val latitudeRad = observer.latitudeDeg.toRadians()

        val elevationRad = asin(
            sin(latitudeRad) * sin(declinationRad) + cos(latitudeRad) * cos(declinationRad) * cos(hourAngleRad),
        )
        return elevationRad.toDegrees()
    }

    private fun julianCenturies(at: Instant): Double = (at.toJulianDate() - 2451545.0) / 36525.0

    private fun normalizeDegrees(degrees: Double): Double {
        val mod = degrees % 360.0
        return if (mod < 0.0) mod + 360.0 else mod
    }

    private const val ASTRONOMICAL_UNIT_KM = 149_597_870.7
}
