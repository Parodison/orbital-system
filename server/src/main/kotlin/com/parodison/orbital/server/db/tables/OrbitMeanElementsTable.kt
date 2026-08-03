package com.parodison.orbital.server.db.tables

import com.parodison.shared.dto.SatGroup
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.*

object OrbitMeanElementsTable : Table("orbit_mean_elements") {
    val noradCatId = long("norad_cat_id")
    val objectName = varchar("object_name", 128)
    val objectId = varchar("object_id", 32)
    val epoch = timestamp("epoch")
    val meanMotion = double("mean_motion")
    val eccentricity = double("eccentricity")
    val inclination = double("inclination")
    val raOfAscNode = double("ra_of_asc_node")
    val argOfPericenter = double("arg_of_pericenter")
    val meanAnomaly = double("mean_anomaly")
    val ephemerisType = long("ephemeris_type")
    val classificationType = char("classification_type", 1)
    val elementSetNo = long("element_set_no")
    val revAtEpoch = long("rev_at_epoch")
    val bstar = double("bstar")
    val meanMotionDot = double("mean_motion_dot")
    val meanMotionDdot = double("mean_motion_ddot")

    override val primaryKey = PrimaryKey(noradCatId, name = "PK_OrbitMean_elements_primary_key")
}

object SatelliteGroupsTable : Table("satellite_groups") {
    val noradCatId = long("norad_cat_id").references(OrbitMeanElementsTable.noradCatId)
    val group = enumerationByName("group_name", 32, SatGroup::class)

    override val primaryKey = PrimaryKey(noradCatId, group, name = "PK_satellite_groups")
}