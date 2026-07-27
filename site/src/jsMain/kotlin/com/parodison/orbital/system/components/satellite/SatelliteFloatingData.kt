package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import com.huanshankeji.compose.html.material3.MdIconButton
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbital.system.core.roundTo
import com.parodison.sgp4.ObserverCoordinates
import com.parodison.sgp4.Satellite
import com.varabyte.kobweb.compose.css.Overflow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import com.varabyte.kobweb.compose.css.functions.blur
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backdropFilter
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.mdi.MdiClose
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px

@Composable
fun SatelliteFloatingContainer(
    modifier: Modifier = Modifier,
    satellite: Satellite,
    observer: ObserverCoordinates?,
    at: Instant,
    onCloseClicked: () -> Unit
) {
    val lookAngles = observer?.let { satellite.lookAnglesFrom(it, at) }
    val altitudeKm = satellite.altitudeAt(at)
    val position = satellite.subPointAt(at)

    Box(
        modifier
            .border(2.px, LineStyle.Solid, AppColors.OutlineGray)
            .padding(topBottom = 10.px, leftRight = 10.px)
            .border(2.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .overflow(Overflow.Hidden)
            .backgroundColor(AppColors.DarkBlue.copyf(alpha = 0.4f))
            .backdropFilter(blur(10.px))
            .styleModifier {
                property("-webkit-backdrop-filter", "blur(10px)")
            }
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.px),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpanText(satellite.orbitData.objectName)
                MdIconButton(
                    attrs = Modifier
                        .onClick { onCloseClicked() }
                        .toAttrs {  }
                ) {
                    MdiClose(
                        Modifier.color(Colors.LightGray)
                    )
                }
            }
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.px)
            ) {
                Metadata(label = "NORAD ID", value = satellite.orbitData.noradCatId.toString())
                Metadata(label = "LONGITUD", value = position.longitudeDeg.roundTo(2).toString())
                Metadata(label = "LATITUD", value = position.latitudeDeg.roundTo(2).toString())
                Metadata(label = "ALTITUD", value = "${altitudeKm.roundTo(1)} km")
                Metadata(label = "VELOCIDAD", value = "${satellite.speedAt(at).roundTo(2)} km/s")
                Metadata(label = "AZIMUT", value = lookAngles?.let { "${it.azimuthDeg.roundTo(2)}°" } ?: "—")
                Metadata(label = "ELEVACIÓN", value = lookAngles?.let { "${it.elevationDeg.roundTo(2)}°" } ?: "—")
                Metadata(label = "RANGO", value = lookAngles?.let { "${it.rangeKm.roundTo(1)} km" } ?: "—")
            }
        }
    }
}

@Composable
private fun ColumnScope.Metadata(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SpanText(label)
        SpanText(value)
    }
}