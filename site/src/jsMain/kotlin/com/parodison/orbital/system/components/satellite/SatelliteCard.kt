package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.*
import com.parodison.orbital.system.components.bootstrap.BootstrapIcon
import com.parodison.orbital.system.components.bootstrap.BootstrapIconStyle
import com.parodison.orbital.system.components.layouts.LocalWindowSize
import com.parodison.orbital.system.components.layouts.WindowSizeClass
import com.parodison.orbital.system.components.material3.RippleSurface
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbital.system.core.roundTo
import com.parodison.sgp4.Satellite
import com.parodison.sgp4.model.OrbitData
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.TextOverflow
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.icons.mdi.IconStyle
import com.varabyte.kobweb.silk.components.icons.mdi.MdiKeyboardArrowRight
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSatelliteAlt
import com.varabyte.kobweb.silk.components.icons.mdi.MdiStar
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.*
import org.koin.compose.koinInject
import kotlin.time.Clock

@Composable
fun SatelliteCard(
    modifier: Modifier = Modifier,
    data: Satellite,
    selected: Boolean = false,
    favorite: Boolean = true,
    onSatelliteSelected: (Satellite) -> Unit,
    onSatelliteResumeRequested: (OrbitData) -> Unit = {},
) {
    val satelliteTrackerController: SatelliteTrackerController = koinInject()

    RippleSurface(
        modifier = Modifier.fillMaxWidth()
            .borderRadius(8.px)
    ) {
        Box(
            modifier = SatelliteCardContainerStyle
                .toModifier()
                .onClick { onSatelliteResumeRequested(data.orbitData) }
                .thenIf(selected) {
                    Modifier.border(2.px, LineStyle.Solid, AppColors.PrimaryRed)
                }
                .then(modifier),
        ) {
            SatelliteCardDesktopContent(data,
                onSatelliteResumeRequested)
        }
    }
}

@Composable
private fun SatelliteCardDesktopContent(
    data: Satellite,
    onSatelliteResumeRequested: (OrbitData) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .width(260.px)
                .whiteSpace(WhiteSpace.NoWrap)
                .overflow(Overflow.Hidden)
                .textOverflow(TextOverflow.Ellipsis)
        ) {
            MdiSatelliteAlt(
                modifier = Modifier
                    .width(50.px)
                    .color(Colors.White)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.px),
            ) {
                SpanText(data.orbitData.objectName)
            }
        }
        Row(
            Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val columnArrangement = Arrangement.spacedBy(10.px)
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.px)
            ) {
                
                Column(
                    verticalArrangement = columnArrangement,
                ) {
                    SpanText("NORAD ID")
                    SpanText(data.orbitData.noradCatId.toString())
                }
                Column(
                    verticalArrangement = columnArrangement,
                ) {
                    SpanText("ORBITA")
                    OrbitTypeContainer(data.orbitData.orbitType)
                }
                Column(
                    verticalArrangement = columnArrangement,
                ) {
                    SpanText("INCLINACIÓN")
                    SpanText("${data.orbitData.inclination.roundTo(2)}°")
                }
                Column(
                    verticalArrangement = columnArrangement,
                ) {
                    SpanText("ALTITUD")
                    SpanText("${data.altitudeAt(Clock.System.now()).roundTo(2)} km")
                }
            }
        }

        Row(
            Modifier,
            horizontalArrangement = Arrangement.spacedBy(5.px),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MdiKeyboardArrowRight(
                modifier = IconHoverStyle
                    .toModifier()
                    .onClick { onSatelliteResumeRequested(data.orbitData) },
            )
        }
    }
}


val IconHoverStyle = CssStyle {
    base {
        Modifier
            .padding(3.px)
            .borderRadius(50.percent)
            .cursor(Cursor.Pointer)
    }
    hover {
        Modifier
            .backgroundColor(rgba(158, 158, 158, 0.15f))
    }
}

val SatelliteCardContainerStyle = CssStyle {
    base {
        Modifier
            .backgroundColor(AppColors.DarkBluePrimary)
            .border(1.5.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .overflow(Overflow.Hidden)
            .cursor(Cursor.Pointer)
    }

    hover {
        Modifier
            .border(1.5.px, LineStyle.Solid, AppColors.PrimaryBlue)
    }
}
