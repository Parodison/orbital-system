package com.parodison.orbital.server.routes

import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.findAll
import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.findByNoradId
import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.findBySearch
import com.parodison.orbital.server.db.repositories.SatelliteGroupsRepository.findSatellitesByGroup
import com.parodison.shared.resources.SatelliteResources
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.routing.Route
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

fun Route.satelliteRoutes() {
    get<SatelliteResources> { resource ->
        val satellites = suspendTransaction {
            findBySearch(
                resource.searchText,
                resource.group,
                resource.page,
                resource.pageSize,
            )
        }
        call.respond(satellites)
    }

    get<SatelliteResources.NoradId> { resource ->
        val noradId = resource.noradId

        val satellite = suspendTransaction {
            findByNoradId(noradId)
        } ?: throw NotFoundException("Satellite not found")

        call.respond(satellite)
    }
}