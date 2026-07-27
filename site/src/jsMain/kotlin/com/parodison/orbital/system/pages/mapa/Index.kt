package com.parodison.orbital.system.pages.mapa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.parodison.orbital.system.components.location.BlueDotContainer
import com.parodison.orbital.system.components.maplibre.*
import com.parodison.orbital.system.components.satellite.SatelliteFloatingContainer
import com.parodison.orbital.system.components.satellite.SatelliteTrackingMarker
import com.parodison.orbital.system.components.satellite.snapshotAt
import com.parodison.orbital.system.components.tracking.TrackingTimeline
import com.parodison.orbital.system.controllers.GeolocationController
import com.parodison.orbital.system.controllers.GeolocationState
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.orbital.system.core.*
import com.parodison.sgp4.ObserverCoordinates
import com.parodison.sgp4.Satellite
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.layout.Layout
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Page
@Layout(".components.layouts.ResponsiveLayout")
@Composable
fun MapScreen() {
    val geolocationController: GeolocationController = koinInject()
    val geolocationState by geolocationController.geolocationState.collectAsState()

    val satelliteTrackerController: SatelliteTrackerController = koinInject()
    val trackingSatellites by satelliteTrackerController.trackingSatellites.collectAsState()
    val trackingDelay by satelliteTrackerController.trackingDelay.collectAsState()
    var selectedSatellite by remember { mutableStateOf<Satellite?>(null) }

    var selectedInstant by remember { mutableStateOf(Clock.System.now()) }

    var minTimelineValue by remember { mutableStateOf(Clock.System.now()) }
    var maxTimelineValue by remember { mutableStateOf(minTimelineValue + 5.hours) }
    var timelineSteps by remember { mutableStateOf(3.minutes) }

    var tracking by remember { mutableStateOf(true) }
    var liveTracking by remember { mutableStateOf(true) }
    var paused by remember { mutableStateOf(false) }

    val observer = (geolocationState as? GeolocationState.Tracking)?.position?.coords?.let { coords ->
        ObserverCoordinates(
            latitudeDeg = coords.latitude,
            longitudeDeg = coords.longitude,
            altitudeKm = (coords.altitude ?: 0.0) / 1000.0,
        )
    }


    Box(
        Modifier.fillMaxSize()
            .border(2.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .overflow(Overflow.Hidden)
    ) {

        MapLibreMap(
            modifier = Modifier.fillMaxSize(),
            options = mapOptions(
                center = lngLatArray(0.0, 0.0),
                zoom = 2.0,
            )
        ) {
            if (geolocationState is GeolocationState.Tracking) {
                Marker(
                    lat = (geolocationState as GeolocationState.Tracking).position.coords.latitude,
                    lng = (geolocationState as GeolocationState.Tracking).position.coords.longitude,
                    anchor = "center"
                ) {
                    BlueDotContainer()
                }
            }

            trackingSatellites.forEach { sat ->
                val snapshot by produceState(
                    initialValue = sat.snapshotAt(selectedInstant),
                    sat,
                    selectedInstant,
                    liveTracking,
                    trackingDelay,
                    paused
                ) {
                    when {
                        paused -> {
                            value = sat.snapshotAt(selectedInstant)
                        }
                        liveTracking -> {
                            while (coroutineContext.isActive) {
                                val now = Clock.System.now()
                                selectedInstant = now
                                value = sat.snapshotAt(now)
                                delay(trackingDelay)
                            }
                        }
                        else -> {
                            while (coroutineContext.isActive) {
                                value = sat.snapshotAt(selectedInstant)
                                delay(trackingDelay)
                                selectedInstant += trackingDelay
                            }
                        }
                    }
                }


                SatelliteTrackingMarker(
                    sat,
                    snapshot,
                    selected = selectedSatellite == sat,
                    onSatelliteClicked = { selectedSatellite = it }
                )
                Polygon(
                    points = snapshot.footprint,
                    fillColor = AppColors.PrimaryBlue,
                    fillOpacity = 0.22,
                    outlineColor = AppColors.PrimaryBlue,
                )
            }

            selectedSatellite?.let { sat ->
                val track = remember(sat) {
                    val now = Clock.System.now()
                    sat.groundTrack(
                        from = now,
                        to = now + (sat.periodMinutes * 3).minutes,
                        step = 60.seconds
                    )
                }
                Line(
                    points = track,
                    color = AppColors.PrimaryRed,
                )

                SatelliteFloatingContainer(
                    modifier = Modifier
                        .width(350.px)
                        .align(Alignment.TopEnd)
                        .margin(right = 30.px, top = 30.px),
                    satellite = sat,
                    observer = observer,
                    at = selectedInstant,
                    onCloseClicked = { selectedSatellite = null },
                )

            }
        }

        if (trackingSatellites.isNotEmpty()) {
            TrackingTimeline(
                Modifier
                    .align(Alignment.BottomCenter)
                    .maxWidth(650.px)
                    .margin(bottom = 30.px),
                min = minTimelineValue.toEpochMilliseconds(),
                max = maxTimelineValue.toEpochMilliseconds(),
                step = timelineSteps.inWholeMilliseconds,
                value = selectedInstant.toEpochMilliseconds(),
                live = liveTracking,
                paused = paused,
                onPausedChange = {
                    paused = !paused
                    if (paused) liveTracking = false
                },
                onLiveClicked = {
                    liveTracking = !liveTracking
                    if (liveTracking) {
                        paused = false
                        val now = Clock.System.now()
                        val windowDuration = maxTimelineValue - minTimelineValue
                        minTimelineValue = now
                        maxTimelineValue = now + windowDuration
                        selectedInstant = now
                    }
                },
                onTimelineValueChange = { newEpochMillis ->
                    liveTracking = false
                    paused = true
                    selectedInstant = newEpochMillis
                },
                onCustomDateSelected = { customInstant ->
                    liveTracking = false
                    paused = true
                    val windowDuration = maxTimelineValue - minTimelineValue
                    minTimelineValue = customInstant
                    maxTimelineValue = customInstant + windowDuration
                    selectedInstant = customInstant
                }
            )
        }
    }
}