package com.parodison.orbital.server.routes

import com.parodison.orbital.server.db.repositories.GroundStationRepository.findBySearch
import com.parodison.orbital.server.db.repositories.GroundStationRepository.findGroundStationById
import com.parodison.shared.resources.GroundStationResource
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.routing.Route
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

fun Route.groundStationRoutes() {
    get<GroundStationResource> { resource ->
        val groundStations = with(resource) {
            suspendTransaction {
                findBySearch(searchText, page, pageSize)
            }
        }
        call.respond(groundStations)
    }

    get<GroundStationResource.Id> { resource ->
        val id = resource.id

        val groundStation = suspendTransaction {
            findGroundStationById(id)
        } ?: throw NotFoundException("Ground station not found")

        call.respond(groundStation)
    }
}
