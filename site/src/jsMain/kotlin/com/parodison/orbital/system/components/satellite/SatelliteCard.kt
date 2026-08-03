package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.*
import com.parodison.orbital.system.components.layouts.LocalWindowSize
import com.parodison.orbital.system.components.layouts.WindowSizeClass
import com.parodison.orbital.system.components.material3.RippleSurface
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbital.system.core.roundTo
import com.parodison.orbit.core.satellite.Satellite
import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
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
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.icons.mdi.IconStyle
import com.varabyte.kobweb.silk.components.icons.mdi.MdiKeyboardArrowRight
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSatelliteAlt
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
    onSatelliteResumeRequested: (OrbitMeanElementsMessage) -> Unit = {},
) {
    val satelliteTrackerController: SatelliteTrackerController = koinInject()
    val isMobile = LocalWindowSize.current.sizeClass == WindowSizeClass.Mobile

    RippleSurface(
        modifier = Modifier.fillMaxWidth()
            .borderRadius(8.px)
    ) {
        Box(
            modifier = SatelliteCardContainerStyle
                .toModifier()
                .onClick { onSatelliteResumeRequested(data.omm) }
                .thenIf(selected) {
                    Modifier.border(2.px, LineStyle.Solid, AppColors.PrimaryRed)
                }
                .then(modifier),
        ) {
            if (isMobile) {
                SatelliteCardMobileContent(data)
            } else {
                SatelliteCardDesktopContent(
                    data,
                    onSatelliteResumeRequested
                )
            }
        }
    }
}

@Composable
private fun SatelliteCardDesktopContent(
    data: Satellite,
    onSatelliteResumeRequested: (OrbitMeanElementsMessage) -> Unit,
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
                .textOverflow(TextOverflow.Ellipsis),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.px),
        ) {
            MdiSatelliteAlt(
                modifier = Modifier
                    .width(50.px)
                    .color(Colors.LightGray)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.px),
            ) {
                SpanText(
                    data.omm.objectName,
                    modifier = Modifier
                        .fontWeight(600)
                        .fontSize(15.px)
                        .color(Colors.White)
                )
            }
        }
        Row(
            Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val columnArrangement = Arrangement.spacedBy(6.px)
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.px)
            ) {
                Column(
                    verticalArrangement = columnArrangement,
                ) {
                    CardFieldLabel("NORAD ID")
                    CardFieldValue(data.omm.noradCatId.toString())
                }
                Column(
                    verticalArrangement = columnArrangement,
                ) {
                    CardFieldLabel("ÓRBITA")
                    OrbitTypeContainer(data.omm.orbitType)
                }
                Column(
                    verticalArrangement = columnArrangement,
                ) {
                    CardFieldLabel("INCLINACIÓN")
                    CardFieldValue("${data.omm.inclination.roundTo(2)}°")
                }
                Column(
                    verticalArrangement = columnArrangement,
                ) {
                    CardFieldLabel("ALTITUD")
                    CardFieldValue("${data.altitudeAt(Clock.System.now()).roundTo(2)} km")
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
                    .color(Colors.LightGray)
                    .onClick { onSatelliteResumeRequested(data.omm) },
            )
        }
    }
}

@Composable
private fun SatelliteCardMobileContent(
    data: Satellite,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.px),
        verticalArrangement = Arrangement.spacedBy(12.px),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.px),
        ) {
            MdiSatelliteAlt(
                modifier = Modifier
                    .width(34.px)
                    .color(Colors.LightGray)
            )
            SpanText(
                data.omm.objectName,
                modifier = Modifier
                    .weight(1f)
                    .whiteSpace(WhiteSpace.NoWrap)
                    .overflow(Overflow.Hidden)
                    .textOverflow(TextOverflow.Ellipsis)
                    .fontWeight(600)
                    .fontSize(15.px)
                    .color(Colors.White)
            )
            MdiKeyboardArrowRight(modifier = Modifier.color(Colors.LightGray))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.px)) {
                CardFieldLabel("NORAD ID")
                CardFieldValue(data.omm.noradCatId.toString())
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.px)) {
                CardFieldLabel("ÓRBITA")
                OrbitTypeContainer(data.omm.orbitType)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.px)) {
                CardFieldLabel("INCLINACIÓN")
                CardFieldValue("${data.omm.inclination.roundTo(1)}°")
            }
        }
    }
}

@Composable
private fun CardFieldLabel(text: String) {
    SpanText(
        text,
        modifier = Modifier
            .fontSize(10.px)
            .fontWeight(500)
            .color(Colors.Gray)
            .styleModifier { property("letter-spacing", "0.03em") }
    )
}

@Composable
private fun CardFieldValue(text: String) {
    SpanText(
        text,
        modifier = Modifier
            .fontSize(13.px)
            .fontWeight(500)
            .color(Colors.White)
    )
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
