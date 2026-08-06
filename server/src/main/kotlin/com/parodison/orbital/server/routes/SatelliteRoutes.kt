package com.parodison.orbital.server.routes

import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.findByNoradId
import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.findBySearch
import com.parodison.shared.resources.SatelliteResource
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.routing.Route
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

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
}