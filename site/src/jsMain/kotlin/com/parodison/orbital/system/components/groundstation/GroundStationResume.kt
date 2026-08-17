package com.parodison.orbital.system.components.groundstation

import androidx.compose.runtime.*
import com.parodison.orbit.core.groundstation.dto.GroundStation
import com.parodison.orbit.core.groundstation.dto.GroundStationStatus
import com.parodison.orbital.system.controllers.GroundStationController
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbital.system.core.roundTo
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.mdi.MdiClose
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject

val subtitleTextModifier = Modifier.fontSize(11.px).color(Colors.Gray)
@Composable
fun GroundStationResume(station: GroundStation, onCloseRequested: () -> Unit) {
    val controller: GroundStationController = koinInject()
    var showTrackDialog by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth()
            .fillMaxHeight()
            .backgroundColor(AppColors.DarkBluePrimary)
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .padding(10.px),
        verticalArrangement = Arrangement.spacedBy(10.px),
    ) {
        GroundStationHeader(station, onCloseRequested)
        if (station.id == MockTestGroundStationId) {
            MockStatusToggle(
                currentStatus = station.status,
                onClick = { controller.cycleMockTestStationStatus() },
            )
        }
        if (station.status.isActive) {
            GroundStationStatusContainer(station)
        }
    }

    if (showTrackDialog) {
        TrackSatelliteDialog(
            onDismissRequest = { showTrackDialog = false },
            onSatelliteChosen = { omm ->
                controller.trackSatellite(station.id, omm)
                showTrackDialog = false
            },
        )
    }
}

@Composable
private fun GroundStationHeader(station: GroundStation, onCloseRequested: () -> Unit) {
    Row(

        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.px),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.px)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.px),
                ) {
                    Column {
                        SpanText(station.name, modifier = Modifier.fontWeight(600).fontSize(18.px))
                        station.coordinates?.let {
                            SpanText(
                                "Latitud: ${station.coordinates!!.latitudeDeg}, Longitud: ${station.coordinates!!.longitudeDeg}",
                                modifier = Modifier.fontSize(11.px).color(Colors.Gray),
                            )
                        }
                    }
                    GroundStationActiveTag(active = station.status.isActive)
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.px),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            station.rotatorPosition?.let { position ->
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.px)) {
                    SpanText("ROTOR", modifier = Modifier.fontSize(10.px).fontWeight(600).color(Colors.Gray))
                    SpanText(
                        "AZ ${position.azimuthDegrees.roundTo(1)}° · EL ${position.elevationDegrees.roundTo(1)}°",
                        modifier = Modifier.fontSize(12.px).color(Colors.White),
                    )
                }
            }
            MdiClose(
                modifier = Modifier
                    .cursor(Cursor.Pointer)
                    .color(Colors.White)
                    .onClick { onCloseRequested() },
            )
        }
    }
}

/** Botón de solo desarrollo para alternar rápidamente el status de la "Estación prueba" mock. */
@Composable
private fun MockStatusToggle(currentStatus: GroundStationStatus, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .cursor(Cursor.Pointer)
            .padding(topBottom = 6.px, leftRight = 10.px)
            .borderRadius(6.px)
            .backgroundColor(AppColors.DarkBlueSecondary)
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .onClick { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpanText("MOCK STATUS", modifier = Modifier.fontSize(10.px).fontWeight(700).color(Colors.Gray))
        SpanText(
            currentStatus.mockLabel(),
            modifier = Modifier.fontSize(12.px).fontWeight(600).color(Colors.White),
        )
    }
}