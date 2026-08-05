package com.parodison.shared.resources

import com.parodison.shared.dto.SatGroup
import io.ktor.resources.Resource

@Resource("/satellites")
class SatelliteResources(
    val parent: Api = Api(),
    val searchText: String? = null,
    val page: Int = 1,
    val pageSize: Int = 100,
    val group: SatGroup? = null,
) {
    @Resource("{noradId}")
    class NoradId(
        val parent: SatelliteResources = SatelliteResources(),
        val noradId: Long
    )
}