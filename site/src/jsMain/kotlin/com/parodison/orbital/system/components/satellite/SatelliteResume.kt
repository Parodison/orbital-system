package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.huanshankeji.compose.html.material3.MdFilledButton
import com.parodison.orbital.system.components.MetaDataItem
import com.parodison.orbital.system.components.maplibre.Line
import com.parodison.orbital.system.components.maplibre.MapLibreMap
import com.parodison.orbital.system.components.maplibre.Marker
import com.parodison.orbital.system.components.maplibre.Polygon
import com.parodison.orbital.system.components.maplibre.rememberMapLibreState
import com.parodison.orbital.system.controllers.SatelliteTrackerController
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
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.components.icons.mdi.MdiClose
import com.varabyte.kobweb.silk.components.icons.mdi.MdiMyLocation
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSatelliteAlt
import com.varabyte.kobweb.silk.components.icons.mdi.MdiStar
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
fun SatelliteResume(satellite: Satellite) {
    val satelliteTrackerController: SatelliteTrackerController = koinInject()
    val trackingSatellites by satelliteTrackerController.trackingSatellites.collectAsState()
    val trackingDelay by satelliteTrackerController.trackingDelay.collectAsState()

    Column(
        Modifier.fillMaxWidth()
            .fillMaxHeight()
            .backgroundColor(AppColors.DarkBluePrimary)
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(10.px)
            .padding(15.px),
        verticalArrangement = Arrangement.spacedBy(10.px),
    ) {
        SatelliteResumeHeader(
            satellite,
            tracking = trackingSatellites.any { it.orbitData.noradCatId == satellite.orbitData.noradCatId },
            onCloseRequested = {
                satelliteTrackerController.clearSelectedSatellite()
            },
            onTrackSattelliteRequested = { satelliteTrackerController.handleSatelliteTrack(it) },
        )
        ActualLocationComponent(
            satellite,
            trackingDelay
        )
    }
}


@Composable
private fun ColumnScope.SatelliteResumeHeader(
    satellite: Satellite,
    tracking: Boolean,
    onCloseRequested: () -> Unit,
    onTrackSattelliteRequested: (Satellite) -> Unit,
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
                    satellite.orbitData.objectName,
                    modifier = Modifier
                        .fontWeight(600)
                        .fontSize(20.px)
                )
                SpanText(
                    "NORAD ID: ${satellite.orbitData.noradCatId}",
                    modifier = Modifier
                        .fontWeight(500)
                        .fontSize(12.px)
                        .color(Colors.Gray)
                )
                MdFilledButton(
                    attrs = Modifier
                        .styleModifier {
                            val containerColor = if (tracking) {
                                "rgba(11, 42, 87, 0.95)" // azul oscuro activo
                            } else {
                                AppColors.PrimaryBlue.toString()
                            }

                            val borderColor = if (tracking) {
                                "rgba(29, 123, 255, 0.95)"
                            } else {
                                "rgba(29, 123, 255, 0.45)"
                            }

                            property("--md-filled-button-container-color", containerColor)
                            property("--md-filled-button-label-text-color", "#EAF1FF")
                            property("--md-filled-button-icon-color", "#EAF1FF")

                            property("--md-filled-button-label-text-font", "Inter")
                            property("--md-filled-button-label-text-size", "14px")
                            property("--md-filled-button-label-text-weight", "600")

                            property("--md-filled-button-container-height", "38px")
                            property("--md-filled-button-container-shape", "999px")

                            property("--md-filled-button-hover-state-layer-color", "#FFFFFF")
                            property("--md-filled-button-hover-state-layer-opacity", "0.08")
                            property("--md-filled-button-pressed-state-layer-opacity", "0.14")

                            property("border", "1px solid $borderColor")
                            property("box-shadow", if (tracking) {
                                "0 0 18px rgba(29, 123, 255, 0.22)"
                            } else {
                                "0 6px 16px rgba(0, 0, 0, 0.22)"
                            })
                            property("transition", "all 180ms ease")
                        }
                        .borderRadius(8.px)
                        .fontFamily("Inter")
                        .onClick {
                           onTrackSattelliteRequested(satellite)
                        }
                        .toAttrs()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.px),
                    ) {
                        Box(
                            Modifier
                                .size(8.px)
                                .borderRadius(50.percent)
                                .backgroundColor(
                                    if (tracking) rgb(70, 224, 130)
                                    else rgb(142, 164, 200)
                                )
                                .styleModifier {
                                    property("box-shadow", if (tracking) "0 0 10px rgba(70, 224, 130, 0.75)" else "none")
                                }

                        )
                        MdiMyLocation()
                        SpanText(
                            if (tracking) "Rastreando" else "Rastrear"
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.px)
        ) {
            MdiClose(
                modifier = IconHoverStyle.toModifier()
                    .color(Colors.White)
                    .onClick { onCloseRequested() },
            )
        }
    }
}

@Composable
private fun ColumnScope.ActualLocationComponent(
    satellite: Satellite,
    trackingDelay: Duration,
) {
    val maplibreState = rememberMapLibreState()

    val snapshot by produceState<SatelliteSnapshot?>(initialValue = null, satellite) {
        while (true) {
            value = satellite.snapshotAt(Clock.System.now())
            delay(trackingDelay)
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
                                    SpanText(satellite.orbitData.objectName, modifier = Modifier)
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