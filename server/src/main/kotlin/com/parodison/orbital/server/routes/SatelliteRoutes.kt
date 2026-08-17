package com.parodison.orbital.server.routes

import com.parodison.orbit.core.satellite.ObserverCoordinates
import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
import com.parodison.orbit.core.satellite.model.toSatellite
import com.parodison.orbit.core.sgp4.SGP4PropagationException
import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.findAllByGroup
import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.findByNoradId
import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.findBySearch
import com.parodison.shared.dto.SatellitePass
import com.parodison.shared.resources.SatelliteResource
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.routing.Route
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

fun Route.satelliteRoutes() {
    get<SatelliteResource> { resource ->
        val satellites = with(resource) {
            suspendTransaction {
                findBySearch(searchText, group, page, pageSize)
            }
        }
        call.respond(satellites)
    }

    get<SatelliteResource.NoradId> { resource ->
        val noradId = resource.noradId

        val satellite = suspendTransaction {
            findByNoradId(noradId)
        } ?: throw NotFoundException("Satellite not found")

        call.respond(satellite)
    }

    get<SatelliteResource.Passes.Upcoming> { resource ->
        val observer = ObserverCoordinates(
            latitudeDeg = resource.lat,
            longitudeDeg = resource.lng,
            altitudeKm = 0.0,
        )

        val satellites = suspendTransaction {
            findAllByGroup(resource.group)
        }
        val candidates = satellites.filter { it.couldBeVisibleFrom(resource.lat) }

        val now = Clock.System.now()
        val upcomingPasses = coroutineScope {
            candidates
                .map { omm ->
                    async(Dispatchers.Default) {
                        val earliestAos = try {
                            omm.toSatellite().nextPassesFrom(observer, now, 24.hours).minOfOrNull { it.aos }
                        } catch (e: SGP4PropagationException) {
                            null
                        }
                        earliestAos?.let { SatellitePass(it, omm) }
                    }
                }
                .awaitAll()
                .filterNotNull()
                .sortedBy { it.aos }
        }

        call.respond(upcomingPasses)
    }
}

// Rough constants, only for the footprint estimate below — not precise enough for propagation.
private const val EARTH_RADIUS_KM = 6378.137
private const val EARTH_MU_KM3_S2 = 398600.4418

// Cheap pre-filter (no SGP4): discards satellites whose ground track + footprint can never reach this latitude.
private fun OrbitMeanElementsMessage.couldBeVisibleFrom(observerLatitudeDeg: Double): Boolean {
    if (meanMotion <= 0.0) return true

    val meanMotionRadPerSec = meanMotion * 2.0 * PI / 86400.0
    val semiMajorAxisKm = (EARTH_MU_KM3_S2 / (meanMotionRadPerSec * meanMotionRadPerSec)).pow(1.0 / 3.0)
    if (semiMajorAxisKm <= EARTH_RADIUS_KM) return true

    val footprintRadiusDeg = acos(EARTH_RADIUS_KM / semiMajorAxisKm) * 180.0 / PI
    val maxGroundTrackLatitudeDeg = 90.0 - abs(90.0 - inclination)

    return abs(observerLatitudeDeg) <= maxGroundTrackLatitudeDeg + footprintRadiusDeg
}