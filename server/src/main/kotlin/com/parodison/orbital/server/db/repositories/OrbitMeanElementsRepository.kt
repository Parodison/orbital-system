package com.parodison.orbital.server.db.repositories

import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
import com.parodison.orbital.server.db.tables.OrbitMeanElementsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert

object OrbitMeanElementsRepository {
    fun Transaction.upsert(omm: OrbitMeanElementsMessage) {
        OrbitMeanElementsTable.upsert {
            it[noradCatId] = omm.noradCatId
            it[objectName] = omm.objectName
            it[objectId] = omm.objectId
            it[epoch] = omm.epoch
            it[meanMotion] = omm.meanMotion
            it[eccentricity] = omm.eccentricity
            it[inclination] = omm.inclination
            it[raOfAscNode] = omm.raOfAscNode
            it[argOfPericenter] = omm.argOfPericenter
            it[meanAnomaly] = omm.meanAnomaly
            it[ephemerisType] = omm.ephemerisType
            it[classificationType] = omm.classificationType
            it[elementSetNo] = omm.elementSetNo
            it[revAtEpoch] = omm.revAtEpoch
            it[bstar] = omm.bstar
            it[meanMotionDot] = omm.meanMotionDot
            it[meanMotionDdot] = omm.meanMotionDdot
        }
    }

    fun Transaction.findByNoradId(noradCatId: Long): OrbitMeanElementsMessage? =
        OrbitMeanElementsTable.selectAll()
            .where { OrbitMeanElementsTable.noradCatId eq noradCatId }
            .singleOrNull()
            ?.toOrbitMeanElementsMessage()

    fun Transaction.findAll(): List<OrbitMeanElementsMessage> =
        OrbitMeanElementsTable.selectAll().map { it.toOrbitMeanElementsMessage() }

    internal fun ResultRow.toOrbitMeanElementsMessage() = OrbitMeanElementsMessage(
        objectName = this[OrbitMeanElementsTable.objectName],
        objectId = this[OrbitMeanElementsTable.objectId],
        epoch = this[OrbitMeanElementsTable.epoch],
        meanMotion = this[OrbitMeanElementsTable.meanMotion],
        eccentricity = this[OrbitMeanElementsTable.eccentricity],
        inclination = this[OrbitMeanElementsTable.inclination],
        raOfAscNode = this[OrbitMeanElementsTable.raOfAscNode],
        argOfPericenter = this[OrbitMeanElementsTable.argOfPericenter],
        meanAnomaly = this[OrbitMeanElementsTable.meanAnomaly],
        ephemerisType = this[OrbitMeanElementsTable.ephemerisType],
        classificationType = this[OrbitMeanElementsTable.classificationType],
        noradCatId = this[OrbitMeanElementsTable.noradCatId],
        elementSetNo = this[OrbitMeanElementsTable.elementSetNo],
        revAtEpoch = this[OrbitMeanElementsTable.revAtEpoch],
        bstar = this[OrbitMeanElementsTable.bstar],
        meanMotionDot = this[OrbitMeanElementsTable.meanMotionDot],
        meanMotionDdot = this[OrbitMeanElementsTable.meanMotionDdot],
    )
}
