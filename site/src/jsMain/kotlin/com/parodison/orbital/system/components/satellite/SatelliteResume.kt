package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.parodison.orbital.system.components.MetaDataItem
import com.parodison.orbital.system.components.maplibre.AnimatedLine
import com.parodison.orbital.system.components.maplibre.Line
import com.parodison.orbital.system.components.maplibre.MapLibreMap
import com.parodison.orbital.system.components.maplibre.Marker
import com.parodison.orbital.system.components.maplibre.Polygon
import com.parodison.orbital.system.components.maplibre.rememberMapLibreState
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbital.system.core.lngLatArray
import com.parodison.orbital.system.core.mapOptions
import com.parodison.orbital.system.core.roundTo
import com.parodison.sgp4.GeodeticCoordinates
import com.parodison.sgp4.Satellite
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSatelliteAlt
import com.varabyte.kobweb.silk.components.icons.mdi.MdiStar
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
fun SatelliteResume(satellite: Satellite) {

    Column(
        Modifier.fillMaxWidth()
            .fillMaxHeight()
            .backgroundColor(AppColors.DarkBluePrimary)
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(10.px)
            .padding(15.px),
        verticalArrangement = Arrangement.spacedBy(10.px),
    ) {
        SatelliteResumeHeader(satellite)
        ActualLocationComponent(satellite)
    }
}


@Composable
private fun ColumnScope.SatelliteResumeHeader(
    satellite: Satellite,
) {

    Row(
        Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.px)
        ) {
            MdiSatelliteAlt(
                modifier = Modifier
                    .fontSize(50.px)
                    .align(Alignment.CenterVertically)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.px)
            ) {
                SpanText(
                    satellite.objectName,
                    modifier = Modifier
                        .fontWeight(600)
                        .fontSize(20.px)
                )
                SpanText(
                    "NORAD ID: ${satellite.noradCatId}",
                    modifier = Modifier
                        .fontWeight(500)
                        .fontSize(12.px)
                        .color(Colors.Gray)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.px)
        ) {
            MdiStar()
        }
    }
}

/**
 * Todo lo que cambia con el tiempo para un [Satellite] en un instante dado, calculado de una
 * sola pasada — evita recomputar `subPointAt` por separado para posición/altitud/footprint como
 * pasaría llamando a los métodos sueltos de [Satellite] de forma independiente.
 */
private data class SatelliteSnapshot(
    val position: GeodeticCoordinates,
    val footprint: List<GeodeticCoordinates>,
    val speedKmPerSec: Double,
)

private fun Satellite.snapshotAt(at: Instant): SatelliteSnapshot = SatelliteSnapshot(
    position = subPointAt(at),
    footprint = footprintPolygon(at),
    speedKmPerSec = speedAt(at),
)

private val SATELLITE_UPDATE_DELAY = 300.milliseconds

@Composable
private fun ColumnScope.ActualLocationComponent(satellite: Satellite) {
    val maplibreState = rememberMapLibreState()

    val snapshot by produceState<SatelliteSnapshot?>(initialValue = null, satellite) {
        while (true) {
            value = satellite.snapshotAt(Clock.System.now())
            delay(SATELLITE_UPDATE_DELAY)
        }
    }

    val orbitTrack = remember(satellite) {
        val now = Clock.System.now()
        satellite.groundTrack(from = now, to = now + satellite.periodMinutes.minutes, step = 60.seconds)
    }

    Box(
        Modifier
            .fillMaxWidth()
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(10.px),

    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.px)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.px),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SpanText(
                        "Ubicación actual",
                        modifier = Modifier
                            .fontWeight(600)
                    )
                }
                Box(
                    Modifier.fillMaxWidth().height(350.px)
                        .borderTop(1.px, LineStyle.Solid, AppColors.OutlineGray)
                        .borderBottom(1.px, LineStyle.Solid, AppColors.OutlineGray),
                ) {
                    MapLibreMap(
                        modifier = Modifier.fillMaxSize(),
                        options = mapOptions(
                            center = lngLatArray(-1.9615482016707801, 34.47336717352307),
                            zoom = 0.18,
                            interactive = false,
                            attributionControl = false,
                        ),
                        state = maplibreState
                    ) {
                        Line(
                            points = orbitTrack,
                            color = AppColors.PrimaryRed,
                        )
                        snapshot?.let { snap ->
                            Polygon(
                                points = snap.footprint,
                                fillColor = AppColors.PrimaryBlue,
                                fillOpacity = 0.22,
                                outlineColor = AppColors.PrimaryBlue,
                            )
                            Marker(
                                snap.position.longitudeDeg,
                                snap.position.latitudeDeg,
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    MdiSatelliteAlt()
                                    SpanText(satellite.objectName, modifier = Modifier)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MetaDataItem(
                        modifier = Modifier
                            .weight(1f)
                            .borderRight(1.px, LineStyle.Solid, AppColors.OutlineGray),
                        name = "ALTITUD",
                        value = snapshot?.let { "${it.position.altitudeKm.roundTo(2)} km" } ?: "—"
                    )
                    MetaDataItem(
                        modifier = Modifier.weight(1f).borderRight(1.px, LineStyle.Solid, AppColors.OutlineGray),
                        name = "VELOCIDAD",
                        value = snapshot?.let { "${it.speedKmPerSec.roundTo(2)} km/s" } ?: "—"
                    )
                    MetaDataItem(
                        modifier = Modifier.weight(1f),
                        name = "PERIODO ORBITAL",
                        value = "${satellite.periodMinutes.roundTo(2)} min"
                    )
                }

            }
        }
    }
}