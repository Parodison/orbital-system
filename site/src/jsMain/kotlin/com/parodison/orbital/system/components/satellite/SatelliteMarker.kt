package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.parodison.orbital.system.components.maplibre.MapLibreScope
import com.parodison.orbital.system.components.maplibre.Marker
import com.parodison.orbital.system.core.lngLatArray
import com.parodison.sgp4.GeodeticCoordinates
import com.parodison.sgp4.Satellite
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.silk.components.icons.mdi.IconStyle
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSatelliteAlt
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/**
 * Todo lo que cambia con el tiempo para un [Satellite] en un instante dado, calculado de una
 * sola pasada — evita recomputar `subPointAt` por separado para posición/altitud/footprint como
 * pasaría llamando a los métodos sueltos de [Satellite] de forma independiente.
 */
data class SatelliteSnapshot(
    val position: GeodeticCoordinates,
    val footprint: List<GeodeticCoordinates>,
    val speedKmPerSec: Double,
)

fun Satellite.snapshotAt(at: Instant = Clock.System.now()): SatelliteSnapshot = SatelliteSnapshot(
    position = subPointAt(at),
    footprint = footprintPolygon(at),
    speedKmPerSec = speedAt(at),
)

@Composable
fun MapLibreScope.SatelliteTrackingMarker(
    satellite: Satellite,
    snapshot : SatelliteSnapshot,
    selected: Boolean = false,
    instantTime: Instant = Clock.System.now(),
    onSatelliteClicked: (Satellite) -> Unit,
) {
    LaunchedEffect(selected) {
        if (selected) {
            map.setCenter(
                center = lngLatArray(snapshot.position.longitudeDeg, snapshot.position.latitudeDeg)
            )
        }
    }

    Marker(
        lng = snapshot.position.longitudeDeg,
        lat = snapshot.position.latitudeDeg,
    ) {
        Column(
            modifier = Modifier
                .onClick {
                    onSatelliteClicked(satellite)
                },
            verticalArrangement = Arrangement.Center,
        ) {
            MdiSatelliteAlt(
                style = if (selected) IconStyle.FILLED else IconStyle.OUTLINED
            )
            SpanText(satellite.orbitData.objectName)
        }
    }
}