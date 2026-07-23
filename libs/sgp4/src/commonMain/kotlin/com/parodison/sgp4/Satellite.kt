package com.parodison.sgp4

import com.parodison.sgp4.constants.WGS72Constants.EARTH_RADIUS_KM
import com.parodison.sgp4.constants.WGS72Constants.TWO_PI
import com.parodison.sgp4.geojson.Feature
import com.parodison.sgp4.geojson.LineString
import com.parodison.sgp4.geojson.toLineStringGeometry
import com.parodison.sgp4.geojson.toMultiLineStringGeometry
import com.parodison.sgp4.geojson.toPointGeometry
import com.parodison.sgp4.geojson.toPolygonGeometry
import com.parodison.sgp4.model.PositionVelocity
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import com.parodison.sgp4.model.*

/** Resultado de [Satellite.nextPassesFrom]: una pasada visible sobre un [GroundStation]. */
data class PassPrediction(
    val aos: Instant,
    val los: Instant,
    val tca: Instant,
    val maxElevationDeg: Double,
)

/**
 * Fachada de alto nivel sobre [SGP4Engine] + astronomía de posición ([PositionalAstronomy],
 * [subPointOf], [SolarPosition]): expone lo que necesita la UI (mapa, HUD, predicción de
 * pasadas) en lat/lon/altura, look angles, iluminación y GeoJSON, sin que el consumidor
 * tenga que conocer TEME/ECEF ni el propagador en sí.
 *
 * No expone ninguna API de coroutines/Flow a propósito: es una librería de cómputo puro,
 * multiplataforma y testeable de forma aislada. El "tiempo real" (un timer/Flow que llama
 * a estos métodos periódicamente y publica un StateFlow para Compose) es responsabilidad
 * de la capa de ViewModel en `shared`, no de este módulo.
 */
class Satellite(
    val orbitData: OrbitData,
) {
    val engine = SGP4Engine(
        epoch = orbitData.epoch,
        meanMotionRadPerMin = orbitData.meanMotion * 2.0 * PI / MINUTES_PER_DAY,
        eccentricity = orbitData.eccentricity,
        inclinationRad = orbitData.inclination * DEGREES_TO_RADIANS,
        raanRad = orbitData.raOfAscNode * DEGREES_TO_RADIANS,
        argPerigeeRad = orbitData.argOfPericenter * DEGREES_TO_RADIANS,
        meanAnomalyRad = orbitData.meanAnomaly * DEGREES_TO_RADIANS,
        bstar = orbitData.bstar,
    )

    private var cachedInstant: Instant? = null
    private var cachedState: PositionVelocity? = null

    private fun stateAt(at: Instant): PositionVelocity {
        cachedState?.let { if (cachedInstant == at) return it }
        val computed = engine.propagate(at)
        cachedInstant = at
        cachedState = computed
        return computed
    }

    /** Posición/velocidad cruda en TEME. Para la mayoría de los usos conviene [subPointAt] o [lookAnglesFrom]. */
    fun positionAt(at: Instant): PositionVelocity = stateAt(at)

    /** Punto subsatelital (lat/lon/alt) en el instante [at]. */
    fun subPointAt(at: Instant): GeodeticCoordinates = subPointOf(stateAt(at), at)

    /** Altura sobre el elipsoide (km) en el instante [at]. */
    fun altitudeAt(at: Instant): Double = subPointAt(at).altitudeKm

    /** Rapidez orbital (km/s) en el instante [at]. */
    fun speedAt(at: Instant): Double {
        val velocity = stateAt(at).velocity
        return sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z)
    }

    /** Período orbital (minutos). No depende de [at]: es el mismo en todo el conjunto de elementos. */
    val periodMinutes: Double get() = engine.periodMinutes

    /** Azimuth/elevación/rango/range-rate del satélite visto desde [observer] en el instante [at]. */
    fun lookAnglesFrom(observer: GroundStation, at: Instant): LookAngles =
        PositionalAstronomy(observer).computeLookAngles(stateAt(at), at)

    /** True si el satélite está por encima de [minElevationDeg] visto desde [observer]. */
    fun isVisibleFrom(observer: GroundStation, at: Instant, minElevationDeg: Double = 0.0): Boolean =
        lookAnglesFrom(observer, at).elevationDeg >= minElevationDeg

    /** True si el satélite está en luz solar directa (no en la sombra cilíndrica de la Tierra). */
    fun isSunlitAt(at: Instant): Boolean = !SolarPosition.isInEarthShadow(stateAt(at).position, at)

    /**
     * True si el satélite es potencialmente visible a ojo desde [observer]: por encima del
     * horizonte, iluminado por el Sol, y con el observador en crepúsculo/noche (Sol por
     * debajo de [twilightSunElevationDeg], -6° = crepúsculo civil por defecto). Es la
     * condición real de "se puede ver a simple vista", distinta de [isVisibleFrom].
     */
    fun isVisuallyVisibleFrom(
        observer: GroundStation,
        at: Instant,
        minElevationDeg: Double = 10.0,
        twilightSunElevationDeg: Double = -6.0,
    ): Boolean {
        if (!isVisibleFrom(observer, at, minElevationDeg)) return false
        if (!isSunlitAt(at)) return false
        return SolarPosition.solarElevationDeg(observer, at) <= twilightSunElevationDeg
    }

    /**
     * Próximas pasadas visibles sobre [observer] entre [from] y [from] + [searchWindow], con
     * elevación máxima >= [minElevationDeg]. Escanea en pasos de [stepSeconds] y refina
     * AOS/LOS por bisección y TCA por búsqueda ternaria alrededor del cruce detectado.
     *
     * Si ya hay una pasada en curso al llegar a [from], se reporta con `aos = from` (no se
     * conoce el AOS real, anterior al inicio de la ventana). Si una pasada sigue en curso al
     * final de la ventana, se reporta con `los` en ese límite.
     */
    fun nextPassesFrom(
        observer: GroundStation,
        from: Instant,
        searchWindow: Duration,
        minElevationDeg: Double = 10.0,
        stepSeconds: Int = 30,
    ): List<PassPrediction> {
        require(searchWindow > Duration.ZERO) { "searchWindow debe ser positivo" }
        require(stepSeconds > 0) { "stepSeconds debe ser positivo" }

        val step = stepSeconds.seconds
        val until = from + searchWindow

        val passes = mutableListOf<PassPrediction>()
        var previousInstant = from
        val initialElevation = lookAnglesFrom(observer, previousInstant).elevationDeg

        var currentAos = previousInstant.takeIf { initialElevation >= minElevationDeg }
        var maxElevationInstant = previousInstant
        var maxElevationDeg = initialElevation

        var t = from + step
        while (t <= until) {
            val elevation = lookAnglesFrom(observer, t).elevationDeg

            if (currentAos == null && elevation >= minElevationDeg) {
                currentAos = bisectCrossing(observer, previousInstant, t, minElevationDeg, risingEdge = true)
                maxElevationInstant = t
                maxElevationDeg = elevation
            } else if (currentAos != null) {
                if (elevation > maxElevationDeg) {
                    maxElevationInstant = t
                    maxElevationDeg = elevation
                }
                if (elevation < minElevationDeg) {
                    val los = bisectCrossing(observer, previousInstant, t, minElevationDeg, risingEdge = false)
                    passes += buildPassPrediction(observer, currentAos, los, maxElevationInstant, step)
                    currentAos = null
                }
            }

            previousInstant = t
            t += step
        }

        if (currentAos != null) {
            passes += buildPassPrediction(observer, currentAos, previousInstant, maxElevationInstant, step)
        }

        return passes
    }

    private fun buildPassPrediction(
        observer: GroundStation,
        aos: Instant,
        los: Instant,
        approximateTca: Instant,
        step: Duration,
    ): PassPrediction {
        val tca = refineMaxElevation(observer, approximateTca, step)
        return PassPrediction(
            aos = aos,
            los = los,
            tca = tca,
            maxElevationDeg = lookAnglesFrom(observer, tca).elevationDeg,
        )
    }

    private fun bisectCrossing(
        observer: GroundStation,
        before: Instant,
        after: Instant,
        thresholdDeg: Double,
        risingEdge: Boolean,
    ): Instant {
        var lo = before
        var hi = after
        repeat(BISECTION_ITERATIONS) {
            val mid = lo + (hi - lo) / 2
            val isAboveThreshold = lookAnglesFrom(observer, mid).elevationDeg >= thresholdDeg
            if (isAboveThreshold == risingEdge) hi = mid else lo = mid
        }
        return hi
    }

    private fun refineMaxElevation(observer: GroundStation, aroundInstant: Instant, step: Duration): Instant {
        var lo = aroundInstant - step
        var hi = aroundInstant + step
        repeat(TERNARY_SEARCH_ITERATIONS) {
            val m1 = lo + (hi - lo) / 3
            val m2 = hi - (hi - lo) / 3
            val e1 = lookAnglesFrom(observer, m1).elevationDeg
            val e2 = lookAnglesFrom(observer, m2).elevationDeg
            if (e1 < e2) lo = m1 else hi = m2
        }
        return lo + (hi - lo) / 2
    }

    /** Ground track como lista de puntos, uno cada [step], entre [from] y [to] (incluidos). */
    fun groundTrack(from: Instant, to: Instant, step: Duration): List<GeodeticCoordinates> {
        require(step > Duration.ZERO) { "step debe ser positivo" }
        require(to >= from) { "to debe ser posterior o igual a from" }

        val points = mutableListOf<GeodeticCoordinates>()
        var t = from
        while (t <= to) {
            points += subPointAt(t)
            t += step
        }
        return points
    }

    /**
     * Ground track como GeoJSON `Feature`. Si el trayecto cruza el antimeridiano (±180°) se
     * arma un `MultiLineString` partido en cada cruce en vez de un `LineString` continuo — si
     * no, un renderer de mapas dibuja una línea espuria atravesando el mapa entero.
     */
    fun groundTrackFeature(from: Instant, to: Instant, step: Duration): Feature {
        val segments = splitAtAntimeridian(groundTrack(from, to, step))
        val geometry = if (segments.size <= 1) {
            segments.firstOrNull()?.toLineStringGeometry() ?: LineString(coordinates = emptyList())
        } else {
            segments.toMultiLineStringGeometry()
        }
        return Feature(geometry = geometry)
    }

    /** Punto subsatelital como GeoJSON `Feature`, para el marcador del satélite en el mapa. */
    fun subPointFeature(at: Instant): Feature = Feature(geometry = subPointAt(at).toPointGeometry())

    /**
     * Radio (km) del círculo de cobertura en tierra: el punto donde el satélite está
     * exactamente a [minElevationDeg] sobre el horizonte (0° = horizonte geométrico).
     */
    fun footprintRadiusKm(at: Instant, minElevationDeg: Double = 0.0): Double =
        EARTH_RADIUS_KM * footprintAngularRadiusRad(at, minElevationDeg)

    /** Círculo de cobertura como lista de puntos (aproximación poligonal, Tierra esférica). */
    fun footprintPolygon(at: Instant, minElevationDeg: Double = 0.0, points: Int = 64): List<GeodeticCoordinates> {
        require(points >= 3) { "un polígono necesita al menos 3 puntos" }

        val center = subPointAt(at)
        val angularRadiusRad = footprintAngularRadiusRad(at, minElevationDeg)
        val centerLatRad = center.latitudeDeg.toRadians()
        val centerLonRad = center.longitudeDeg.toRadians()

        return (0 until points).map { i ->
            val bearingRad = TWO_PI * i / points
            destinationPoint(centerLatRad, centerLonRad, angularRadiusRad, bearingRad)
        }
    }

    /** Círculo de cobertura como GeoJSON `Feature` (`Polygon`). */
    fun footprintFeature(at: Instant, minElevationDeg: Double = 0.0, points: Int = 64): Feature =
        Feature(geometry = footprintPolygon(at, minElevationDeg, points).toPolygonGeometry())

    private fun footprintAngularRadiusRad(at: Instant, minElevationDeg: Double): Double {
        val elevationRad = minElevationDeg.toRadians()
        val horizonAngleRad = asin((EARTH_RADIUS_KM / (EARTH_RADIUS_KM + altitudeAt(at))) * cos(elevationRad))
        return PI / 2.0 - elevationRad - horizonAngleRad
    }

    private fun destinationPoint(
        latRad: Double,
        lonRad: Double,
        angularDistanceRad: Double,
        bearingRad: Double,
    ): GeodeticCoordinates {
        val destLatRad = asin(
            sin(latRad) * cos(angularDistanceRad) + cos(latRad) * sin(angularDistanceRad) * cos(bearingRad),
        )
        val destLonRad = lonRad + atan2(
            sin(bearingRad) * sin(angularDistanceRad) * cos(latRad),
            cos(angularDistanceRad) - sin(latRad) * sin(destLatRad),
        )
        return GeodeticCoordinates(
            latitudeDeg = destLatRad.toDegrees(),
            longitudeDeg = normalizeLongitudeDeg(destLonRad.toDegrees()),
            altitudeKm = 0.0,
        )
    }

    /** Corta el ground track en segmentos nuevos cada vez que salta más de 180° en longitud. */
    private fun splitAtAntimeridian(points: List<GeodeticCoordinates>): List<List<GeodeticCoordinates>> {
        if (points.isEmpty()) return emptyList()

        val segments = mutableListOf(mutableListOf(points.first()))
        for (i in 1 until points.size) {
            val jumpDeg = abs(points[i].longitudeDeg - points[i - 1].longitudeDeg)
            if (jumpDeg > ANTIMERIDIAN_JUMP_THRESHOLD_DEG) segments.add(mutableListOf())
            segments.last().add(points[i])
        }
        return segments
    }

    private fun normalizeLongitudeDeg(lonDeg: Double): Double {
        var normalized = lonDeg
        while (normalized > 180.0) normalized -= 360.0
        while (normalized < -180.0) normalized += 360.0
        return normalized
    }

    private companion object {
        const val BISECTION_ITERATIONS = 20
        const val TERNARY_SEARCH_ITERATIONS = 20
        const val ANTIMERIDIAN_JUMP_THRESHOLD_DEG = 180.0
    }
}
