package com.parodison.shared.resources

import com.parodison.shared.dto.SatGroup
import io.ktor.resources.Resource

@Resource("/satellites")
class SatelliteResource(
    val parent: Api = Api(),
    val searchText: String? = null,
    val page: Int = 1,
    val pageSize: Int = 100,
    val group: SatGroup? = null,
) {
    @Resource("{noradId}")
    class NoradId(
        val parent: SatelliteResource = SatelliteResource(),
        val noradId: Long
    )

    @Resource("passes")
    class Passes(
        val parent: SatelliteResource = SatelliteResource(),
    ) {

        @Resource("upcoming")
        class Upcoming(
            val parent: Passes = Passes(),
            val lat: Double = 0.0,
            val lng: Double = 0.0,
            val group: SatGroup? = null,
        )
    }
}