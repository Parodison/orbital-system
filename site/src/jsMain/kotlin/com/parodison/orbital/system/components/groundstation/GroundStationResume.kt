package com.parodison.orbital.system.components.groundstation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.huanshankeji.compose.html.material3.MdFilledButton
import com.parodison.orbital.system.components.MetaDataItem
import com.parodison.orbital.system.components.dom.BasicInput
import com.parodison.orbital.system.controllers.GroundStationController
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbital.system.core.roundTo
import com.parodison.orbit.core.groundstation.dto.GroundStation
import com.parodison.orbit.core.groundstation.dto.GroundStationStatus
import com.parodison.orbit.core.satellite.ObserverCoordinates
import com.parodison.orbit.core.satellite.model.toSatellite
import com.parodison.orbital.system.core.toDisplayString
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.PointerEvents
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderBottom
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.pointerEvents
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.mdi.MdIcon
import com.varabyte.kobweb.silk.components.icons.mdi.MdiClose
import com.varabyte.kobweb.silk.components.icons.mdi.MdiMyLocation
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSatelliteAlt
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

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
            .borderRadius(10.px)
            .padding(15.px),
        verticalArrangement = Arrangement.spacedBy(10.px),
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.px)
        ) {
            GroundStationHeader(station, onCloseRequested)
            SpanText(
                "Posición del rotador: ${station.rotatorPosition.toDisplayString()}",
                modifier = subtitleTextModifier
            )
        }
        if (station.status.isActive) {
            StationLocationCard(station)
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
                if (station.status.isActive) {
                    SpanText(
                        station.status.label(),
                        modifier = Modifier.fontSize(12.px).color(Colors.Gray),
                    )
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

@Composable
fun StationLocationCard(station: GroundStation, modifier: Modifier = Modifier) {
    val controller: GroundStationController = koinInject()
    val coordinates = station.coordinates ?: ObserverCoordinates(0.0, 0.0, 0.0)

    Column(
        modifier
            .fillMaxWidth()
            .backgroundColor(AppColors.DarkBluePrimary)
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .padding(10.px),
        verticalArrangement = Arrangement.spacedBy(6.px),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.px)) {
            MdiMyLocation(modifier = Modifier.color(Colors.Gray))
            SpanText("UBICACIÓN", modifier = Modifier.fontSize(11.px).fontWeight(600).color(Colors.Gray))
        }

        EditableCoordinateField(
            label = "Latitud", value = coordinates.latitudeDeg, decimals = 4, suffix = "°",
            onCommit = { controller.updateStationLocation(station.id, coordinates.copy(latitudeDeg = it)) },
        )
        EditableCoordinateField(
            label = "Longitud", value = coordinates.longitudeDeg, decimals = 4, suffix = "°",
            onCommit = { controller.updateStationLocation(station.id, coordinates.copy(longitudeDeg = it)) },
        )
        EditableCoordinateField(
            label = "Altitud", value = coordinates.altitudeKm * 1000, decimals = 0, suffix = " m",
            onCommit = { meters -> controller.updateStationLocation(station.id, coordinates.copy(altitudeKm = meters / 1000)) },
        )
    }
}