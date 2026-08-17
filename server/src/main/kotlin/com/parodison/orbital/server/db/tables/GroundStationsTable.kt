package com.parodison.orbital.server.db.tables

import com.parodison.orbit.core.groundstation.dto.GroundStationStatus
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import kotlin.time.Clock

object GroundStationsTable : UuidTable("ground_stations") {
    val name = varchar("name", 255)
    val addedAt = timestamp("added_at").clientDefault { Clock.System.now() }
    val status = jsonb<GroundStationStatus>("status", Json { prettyPrint = true })
}
