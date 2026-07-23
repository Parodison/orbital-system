package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.*
import com.parodison.orbital.system.components.bootstrap.BootstrapIcon
import com.parodison.orbital.system.components.bootstrap.BootstrapIconStyle
import com.parodison.orbital.system.components.layouts.LocalWindowSize
import com.parodison.orbital.system.components.layouts.WindowSizeClass
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.orbital.system.core.AppColors
import com.parodison.sgp4.Satellite
import com.parodison.sgp4.model.OrbitData
import com.varabyte.kobweb.compose.css.Cursor
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
    val isMobile = LocalWindowSize.current.sizeClass == WindowSizeClass.Mobile

    Box(
        modifier = SatelliteCardContainerStyle
            .toModifier()
            .thenIf(selected) {
                Modifier.border(2.px, LineStyle.Solid, AppColors.PrimaryRed)
            }
            .then(modifier),
    ) {
        SatelliteCardDesktopContent(data, favorite, onSatelliteSelected, onSatelliteResumeRequested)
    }
}

@Composable
private fun SatelliteCardDesktopContent(
    data: Satellite,
    favorite: Boolean = false,
    onSatelliteSelected: (Satellite) -> Unit,
    onSatelliteResumeRequested: (OrbitData) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.px),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.px)
    ) {
        Row() {
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
                    SpanText("INCLINACIÓN")
                    SpanText("${data.orbitData.inclination}°")
                }
            }
        }

        Row(
            Modifier,
            horizontalArrangement = Arrangement.spacedBy(5.px),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var starHover by remember { mutableStateOf(false) }

            BootstrapIcon(
                name = "bi bi-star",
                modifier = IconHoverStyle.toModifier()
                    .title(
                        if (favorite) "Eliminar de favoritos" else "Añadir a favoritos",
                    )
                    .fontSize(20.px)
                    .onClick { onSatelliteSelected(data) }
                    .onMouseEnter {starHover = true}
                    .onMouseLeave {starHover = false}
                    .then(if (favorite) Modifier.color(rgb(246, 60, 54)) else Modifier),
                style = if (starHover || favorite) BootstrapIconStyle.FILLED else BootstrapIconStyle.OUTLINED
            )
            MdiKeyboardArrowRight(
                modifier = IconHoverStyle
                    .toModifier()
                    .onClick { onSatelliteResumeRequested(data.orbitData) },
            )
        }
    }
}

@Composable
private fun SatelliteCardMobileContent(
    data: OrbitData,
    onSatelliteSelected: (OrbitData) -> Unit,
    onSatelliteResumeRequested: (OrbitData) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(10.px),
        verticalArrangement = Arrangement.spacedBy(10.px),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.px),
            ) {
                MdiSatelliteAlt(
                    modifier = Modifier
                        .width(36.px)
                        .color(Colors.White)
                )
                SpanText(data.objectName)
            }
            MdiStar(
                modifier = IconHoverStyle.toModifier()
                    .onClick { onSatelliteSelected(data) },
                style = IconStyle.OUTLINED
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .cursor(Cursor.Pointer)
                .onClick { onSatelliteResumeRequested(data) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.px)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.px)) {
                    SpanText("NORAD ID", modifier = Modifier.color(Colors.Gray))
                    SpanText(data.noradCatId.toString())
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.px)) {
                    SpanText("INCLINACIÓN", modifier = Modifier.color(Colors.Gray))
                    SpanText("${data.inclination}°")
                }
            }
            MdiKeyboardArrowRight()
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
    }

    hover {
        Modifier
            .border(1.5.px, LineStyle.Solid, AppColors.PrimaryBlue)
    }
}