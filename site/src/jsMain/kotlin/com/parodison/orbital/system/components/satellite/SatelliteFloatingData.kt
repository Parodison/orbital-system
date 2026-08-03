package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import com.huanshankeji.compose.html.material3.MdIconButton
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbital.system.core.roundTo
import com.parodison.orbit.core.satellite.ObserverCoordinates
import com.parodison.orbit.core.satellite.Satellite
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.functions.blur
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backdropFilter
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderBottom
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.mdi.MdiClose
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSatelliteAlt
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import kotlin.time.Instant

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

    val instantLocal = at.toLocalDateTime(TimeZone.currentSystemDefault())
    val instantLabel = "${instantLocal.date.day.toString().padStart(2, '0')}/" +
        "${instantLocal.date.month.toString().padStart(2, '0')} " +
        "${instantLocal.hour.toString().padStart(2, '0')}:" +
        instantLocal.minute.toString().padStart(2, '0')

    Box(
        modifier
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(12.px)
            .overflow(Overflow.Hidden)
            .backgroundColor(AppColors.DarkBlue.copyf(alpha = 0.55f))
            .backdropFilter(blur(14.px))
            .styleModifier {
                property("-webkit-backdrop-filter", "blur(14px)")
                property("box-shadow", "0 8px 24px rgba(0, 0, 0, 0.35)")
            }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(15.px)
                    .borderBottom(1.px, LineStyle.Solid, AppColors.OutlineGray),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.px),
                ) {
                    MdiSatelliteAlt(
                        modifier = Modifier.fontSize(28.px).color(Colors.White)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.px)) {
                        SpanText(
                            satellite.omm.objectName,
                            modifier = Modifier.fontWeight(600).fontSize(15.px)
                        )
                        SpanText(
                            "NORAD ${satellite.omm.noradCatId}",
                            modifier = Modifier.fontSize(11.px).color(Colors.Gray)
                        )
                    }
                }
                MdIconButton(
                    attrs = Modifier
                        .onClick { onCloseClicked() }
                        .toAttrs {  }
                ) {
                    MdiClose(Modifier.color(Colors.LightGray))
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(15.px)
                    .borderBottom(1.px, LineStyle.Solid, AppColors.OutlineGray),
                horizontalArrangement = Arrangement.spacedBy(10.px),
            ) {
                if (lookAngles != null) {
                    LookAngleStat(
                        label = "AZIMUT",
                        value = "${lookAngles.azimuthDeg.roundTo(1)}°",
                    )
                    LookAngleStat(
                        label = "ELEVACIÓN",
                        value = "${lookAngles.elevationDeg.roundTo(1)}°",
                    )
                    LookAngleStat(
                        label = "RANGO",
                        value = "${lookAngles.rangeKm.roundTo(0)} km",
                    )
                } else {
                    SpanText(
                        "Sin ubicación del observador — activá la geolocalización para ver azimuth/elevación",
                        modifier = Modifier.fontSize(12.px).color(Colors.Gray)
                    )
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(15.px),
                verticalArrangement = Arrangement.spacedBy(12.px),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.px)) {
                    StatItem(label = "ALTITUD", value = "${altitudeKm.roundTo(1)} km")
                    StatItem(label = "VELOCIDAD", value = "${satellite.speedAt(at).roundTo(2)} km/s")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.px)) {
                    StatItem(label = "LATITUD", value = "${position.latitudeDeg.roundTo(2)}°")
                    StatItem(label = "LONGITUD", value = "${position.longitudeDeg.roundTo(2)}°")
                }
            }
        }
    }
}

@Composable
private fun RowScope.LookAngleStat(label: String, value: String) {
    Column(
        Modifier
            .weight(1f)
            .borderRadius(8.px)
            .backgroundColor(AppColors.DarkBlueSecondary)
            .padding(10.px),
        verticalArrangement = Arrangement.spacedBy(4.px),
    ) {
        SpanText(label, modifier = Modifier.fontSize(10.px).color(Colors.Gray))
        SpanText(
            value,
            modifier = Modifier
                .fontSize(20.px)
                .fontWeight(700)
                .color(Colors.White)
        )
    }
}

@Composable
private fun RowScope.StatItem(label: String, value: String) {
    Column(
        Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.px),
    ) {
        SpanText(label, modifier = Modifier.fontSize(11.px).color(Colors.Gray))
        SpanText(value, modifier = Modifier.fontSize(14.px).fontWeight(500))
    }
}
