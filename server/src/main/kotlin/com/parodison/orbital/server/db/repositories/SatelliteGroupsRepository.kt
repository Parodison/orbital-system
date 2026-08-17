package com.parodison.orbital.server.db.repositories

import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
import com.parodison.orbital.server.db.repositories.OrbitMeanElementsRepository.toOrbitMeanElementsMessage
import com.parodison.orbital.server.db.tables.OrbitMeanElementsTable
import com.parodison.orbital.server.db.tables.SatelliteGroupsTable
import com.parodison.shared.dto.SatGroup
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll

object SatelliteGroupsRepository {
    /** Replaces the full membership of [group] with [noradCatIds] — used when refreshing a group from Celestrak. */
    fun Transaction.replaceMembers(group: SatGroup, noradCatIds: List<Long>) {
        SatelliteGroupsTable.deleteWhere { SatelliteGroupsTable.group eq group }
        SatelliteGroupsTable.batchInsert(noradCatIds) { noradCatId ->
            this[SatelliteGroupsTable.noradCatId] = noradCatId
            this[SatelliteGroupsTable.group] = group
        }
    }

    fun Transaction.findGroupsByNoradId(noradCatId: Long): Set<SatGroup> =
        SatelliteGroupsTable.selectAll()
            .where { SatelliteGroupsTable.noradCatId eq noradCatId }
            .mapTo(mutableSetOf()) { it[SatelliteGroupsTable.group] }

    fun Transaction.findNoradIdsByGroup(group: SatGroup): List<Long> =
        SatelliteGroupsTable.selectAll()
            .where { SatelliteGroupsTable.group eq group }
            .map { it[SatelliteGroupsTable.noradCatId] }

    /** Full satellite data (not just the norad id) for every member of [group]. */
    fun Transaction.findSatellitesByGroup(group: SatGroup): List<OrbitMeanElementsMessage> =
        (SatelliteGroupsTable innerJoin OrbitMeanElementsTable)
            .selectAll()
            .where { SatelliteGroupsTable.group eq group }
            .map { it.toOrbitMeanElementsMessage() }
}
