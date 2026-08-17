package com.parodison.orbital.server.service

import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository
import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.upsert
import com.parodison.orbital.server.db.repositories.SatelliteGroupsRepository.replaceMembers
import com.parodison.shared.client.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class SatelliteCatalogService(
    private val client: CelestrakClient = celestrakClient
) {
    suspend fun syncSatellitesOmm() {
        println("Sincronizando elementos satelitales")
        val groupsWithOmm = client.getAllGroups()
        suspendTransaction {
            groupsWithOmm.forEach { (group, omms) ->
                omms.forEach { omm ->
                    upsert(omm)
                }
                replaceMembers(group, omms.map { it.noradCatId })
            }
        }
    }
}