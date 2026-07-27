package com.parodison.sgp4

import com.parodison.sgp4.constants.WGS72Constants.EARTH_RADIUS_KM
import com.parodison.sgp4.model.PositionVelocity
import com.parodison.sgp4.model.Vector3
import com.parodison.sgp4.time.greenwichSiderealTime
import com.parodison.sgp4.time.toJulianDate
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Instant

/** Ubicación de un observador en la superficie terrestre (estación terrena). */
data class ObserverCoordinates(
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val altitudeKm: Double,
)

/** Resultado de RAZEL: cómo se ve el satélite desde un [ObserverCoordinates] en un instante dado. */
data class LookAngles(
    val azimuthDeg: Double,
    val elevationDeg: Double,
    val rangeKm: Double,
    val rangeRateKmPerSec: Double,
)

/** Punto subsatelital: lat/lon/altura del satélite en sí, sin relación a ningún observador. */
data class GeodeticCoordinates(
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val altitudeKm: Double,
)

/** Punto subsatelital (lat/lon/alt) en un instante dado — no depende de ningún observador. */
fun subPointOf(satellite: PositionVelocity, at: Instant): GeodeticCoordinates {
    val gst = greenwichSiderealTime(at.toJulianDate())
    val (satelliteEcefPosition, _) = temeToEcef(satellite.position, satellite.velocity, gst)
    return ecefToGeodetic(satelliteEcefPosition)
}

/**
 * Astronomía de posición: transforma un vector de estado en TEME (el que devuelve
 * [com.parodison.sgp4.SGP4Engine.propagate]) a coordenadas topocéntricas horizontales
 * (azimuth/elevación/rango) vistas desde un observador en la Tierra — algoritmo RAZEL
 * de Vallado ("Fundamentals of Astrodynamics and Applications").
 *
 * Es una teoría aparte de SGP4 (astronomía de posición / geodesia, no astrodinámica):
 * solo necesita un vector de posición y velocidad, sin importar de qué propagador vino.
 */
class PositionalAstronomy(private val groundStation: ObserverCoordinates) {

    private val observerEcef = geodeticToEcef(groundStation)

    fun computeLookAngles(satellite: PositionVelocity, at: Instant): LookAngles {
        val gst = greenwichSiderealTime(at.toJulianDate())
        val (satelliteEcefPosition, satelliteEcefVelocity) = temeToEcef(satellite.position, satellite.velocity, gst)

        val rangeVectorEcef = Vector3(
            x = satelliteEcefPosition.x - observerEcef.x,
            y = satelliteEcefPosition.y - observerEcef.y,
            z = satelliteEcefPosition.z - observerEcef.z,
        )
        val range = sqrt(
            rangeVectorEcef.x * rangeVectorEcef.x +
                rangeVectorEcef.y * rangeVectorEcef.y +
                rangeVectorEcef.z * rangeVectorEcef.z,
        )

        // El observador está fijo en ECEF (velocidad cero ahí), así que la velocidad
        // relativa es directamente la del satélite; el producto punto es invariante
        // ante rotaciones, no hace falta pasar la velocidad a SEZ para el range rate.
        val rangeRate = (
            rangeVectorEcef.x * satelliteEcefVelocity.x +
                rangeVectorEcef.y * satelliteEcefVelocity.y +
                rangeVectorEcef.z * satelliteEcefVelocity.z
            ) / range

        val latitudeRad = groundStation.latitudeDeg.toRadians()
        val longitudeRad = groundStation.longitudeDeg.toRadians()
        val sez = ecefToSez(rangeVectorEcef, latitudeRad, longitudeRad)

        val elevationRad = asin((sez.z / range).coerceIn(-1.0, 1.0))
        var azimuthRad = atan2(sez.y, -sez.x)
        if (azimuthRad < 0.0) azimuthRad += 2.0 * PI

        return LookAngles(
            azimuthDeg = azimuthRad.toDegrees(),
            elevationDeg = elevationRad.toDegrees(),
            rangeKm = range,
            rangeRateKmPerSec = rangeRate,
        )
    }

}

/** Posición (y velocidad, corrigiendo por la rotación terrestre) TEME -> ECEF/PEF. */
private fun temeToEcef(position: Vector3, velocity: Vector3, gst: Double): Pair<Vector3, Vector3> {
    val cosGst = cos(gst)
    val sinGst = sin(gst)

    val x = position.x * cosGst + position.y * sinGst
    val y = -position.x * sinGst + position.y * cosGst
    val z = position.z

    val rotatedVx = velocity.x * cosGst + velocity.y * sinGst
    val rotatedVy = -velocity.x * sinGst + velocity.y * cosGst

    val vx = rotatedVx + EARTH_ROTATION_RATE_RAD_PER_SEC * y
    val vy = rotatedVy - EARTH_ROTATION_RATE_RAD_PER_SEC * x
    val vz = velocity.z

    return Vector3(x, y, z) to Vector3(vx, vy, vz)
}

/** Lat/lon/alt (WGS-72, elipsoidal) -> ECEF, en km. */
private fun geodeticToEcef(station: ObserverCoordinates): Vector3 {
    val latitudeRad = station.latitudeDeg.toRadians()
    val longitudeRad = station.longitudeDeg.toRadians()
    val sinLat = sin(latitudeRad)
    val cosLat = cos(latitudeRad)

    val eccentricitySquared = 2.0 * EARTH_FLATTENING - EARTH_FLATTENING * EARTH_FLATTENING
    val primeVerticalRadius = EARTH_RADIUS_KM / sqrt(1.0 - eccentricitySquared * sinLat * sinLat)

    val x = (primeVerticalRadius + station.altitudeKm) * cosLat * cos(longitudeRad)
    val y = (primeVerticalRadius + station.altitudeKm) * cosLat * sin(longitudeRad)
    val z = (primeVerticalRadius * (1.0 - eccentricitySquared) + station.altitudeKm) * sinLat

    return Vector3(x, y, z)
}

/** ECEF (km) -> lat/lon/alt (WGS-72, elipsoidal), inverso de [geodeticToEcef] (iterativo, Bowring). */
private fun ecefToGeodetic(position: Vector3): GeodeticCoordinates {
    val longitudeRad = atan2(position.y, position.x)
    val eccentricitySquared = 2.0 * EARTH_FLATTENING - EARTH_FLATTENING * EARTH_FLATTENING
    val equatorialDistance = sqrt(position.x * position.x + position.y * position.y)

    var latitudeRad = atan2(position.z, equatorialDistance * (1.0 - eccentricitySquared))
    var altitudeKm = 0.0

    repeat(GEODETIC_ITERATIONS) {
        val primeVerticalRadius = EARTH_RADIUS_KM / sqrt(1.0 - eccentricitySquared * sin(latitudeRad) * sin(latitudeRad))
        altitudeKm = equatorialDistance / cos(latitudeRad) - primeVerticalRadius
        latitudeRad = atan2(
            position.z,
            equatorialDistance * (1.0 - eccentricitySquared * primeVerticalRadius / (primeVerticalRadius + altitudeKm)),
        )
    }

    return GeodeticCoordinates(
        latitudeDeg = latitudeRad.toDegrees(),
        longitudeDeg = longitudeRad.toDegrees(),
        altitudeKm = altitudeKm,
    )
}

/** Rota un vector ECEF al marco topocéntrico local Sur-Este-Zenit (SEZ) del observador. */
private fun ecefToSez(vector: Vector3, latitudeRad: Double, longitudeRad: Double): Vector3 {
    val sinLat = sin(latitudeRad)
    val cosLat = cos(latitudeRad)
    val sinLon = sin(longitudeRad)
    val cosLon = cos(longitudeRad)

    val south = sinLat * cosLon * vector.x + sinLat * sinLon * vector.y - cosLat * vector.z
    val east = -sinLon * vector.x + cosLon * vector.y
    val zenith = cosLat * cosLon * vector.x + cosLat * sinLon * vector.y + sinLat * vector.z

    return Vector3(south, east, zenith)
}

internal fun Double.toRadians(): Double = this * PI / 180.0
internal fun Double.toDegrees(): Double = this * 180.0 / PI

// Achatamiento WGS-72 (el mismo elipsoide de referencia que usa SGP4Engine).
private const val EARTH_FLATTENING = 1.0 / 298.26
// Velocidad de rotación sideral de la Tierra, rad/s.
private const val EARTH_ROTATION_RATE_RAD_PER_SEC = 7.292115e-5
// Iteraciones para la conversión ECEF -> geodésico; converge en pocas iteraciones.
private const val GEODETIC_ITERATIONS = 5
