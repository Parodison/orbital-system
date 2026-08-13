package com.parodison.orbital.server

import com.parodison.orbital.server.plugin.*
import com.parodison.orbital.server.routes.groundStationRoutes
import com.parodison.orbital.server.routes.satelliteRoutes
import com.parodison.orbital.server.routes.websocketRoutes
import com.parodison.orbital.server.service.SatelliteCatalogService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    configureDatabases()
    configureStatusPages()
    configureResources()
    configureSerialization()
    configureCORS()
    configureWebsockets()

    launch {
        val satelliteCatalogService = SatelliteCatalogService()
        while (isActive) {
            satelliteCatalogService.syncSatellitesOmm()
            delay(3.hours)
        }
    }

    routing {
        satelliteRoutes()
        groundStationRoutes()
        route("/ws") {
            websocketRoutes()
        }
    }
}