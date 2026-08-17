package com.parodison.shared.resources

import io.ktor.resources.Resource
import kotlin.uuid.Uuid

@Resource("groundstations")
class GroundStationResource(
    val parent: Api = Api(),
    val searchText: String? = null,
    val page: Int = 1,
    val pageSize: Int = 100,
) {
    @Resource("{id}")
    class Id(
        val parent: GroundStationResource = GroundStationResource(),
        val id: Uuid
    )
}