package com.parodison.orbital.server.db.repositories

import com.parodison.orbit.core.groundstation.dto.GroundStation
import com.parodison.orbit.core.groundstation.dto.GroundStationStatus
import com.parodison.orbital.server.db.paginate
import com.parodison.orbital.server.db.tables.GroundStationsTable
import com.parodison.shared.dto.GroundStationDTO
import com.parodison.shared.dto.Page
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.Uuid

object GroundStationRepository {
    fun ResultRow.toGroundStation(): GroundStation {
        return GroundStation(
            id = this[GroundStationsTable.id].value,
            name = this[GroundStationsTable.name],
            status = this[GroundStationsTable.status],
            coordinates = null,
        )
    }

    fun Transaction.findGroundStationById(id: Uuid): GroundStation? {
        val query = GroundStationsTable.selectAll()
            .where { GroundStationsTable.id eq id }
            .singleOrNull()
        ?.toGroundStation()

        return query
    }

    fun Transaction.upsertGroundStation(groundStation: GroundStation) {
        GroundStationsTable.upsert {
            it[GroundStationsTable.id] = groundStation.id
            it[GroundStationsTable.name] = groundStation.name
            it[GroundStationsTable.status] = groundStation.status
        }
    }

    fun Transaction.updateGroundStationStatus(id: Uuid, status: GroundStationStatus) {
        GroundStationsTable.update({ GroundStationsTable.id eq id }) {
            it[GroundStationsTable.status] = status
        }
    }

    fun Transaction.findBySearch(
        searchText: String? = null,
        page: Int = 1,
        pageSize: Int = 100,
    ): Page<GroundStation> {
        val baseQuery = GroundStationsTable.selectAll()

        val conditions = listOfNotNull(
            searchText?.let { GroundStationsTable.name.lowerCase() like "%${it.lowercase()}%" },
        )

        val filtered = conditions.fold(baseQuery) { query, cond -> query.andWhere { cond } }
        return filtered.paginate(page, pageSize) { it.toGroundStation() }
    }

}