package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbital.system.models.OrbitData
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.TransitionBehavior
import com.varabyte.kobweb.compose.css.TransitionTimingFunction
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.icons.mdi.IconStyle
import com.varabyte.kobweb.silk.components.icons.mdi.MdiArrowRight
import com.varabyte.kobweb.silk.components.icons.mdi.MdiKeyboardArrowRight
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSatelliteAlt
import com.varabyte.kobweb.silk.components.icons.mdi.MdiStar
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba

@Composable
fun SatelliteCard(
    modifier: Modifier = Modifier,
    data: OrbitData,
    selected: Boolean = false,
    onSatelliteSelected: (OrbitData) -> Unit,
    onSatelliteResumeRequested: (OrbitData) -> Unit = {},
) {
    Box(
        modifier = SatelliteCardContainerStyle
            .toModifier()
            .thenIf(selected) {
                Modifier.border(2.px, LineStyle.Solid, AppColors.PrimaryRed)
            }
            .then(modifier),
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
                    SpanText(data.objectName)
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
                        SpanText(data.noradCatId.toString())
                    }
                    Column(
                        verticalArrangement = columnArrangement,
                    ) {
                        SpanText("INCLINACIÓN")
                        SpanText("${data.inclination}°")
                    }
                }
            }

            Row(
                Modifier,
                horizontalArrangement = Arrangement.spacedBy(5.px),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MdiStar(
                    modifier = IconHoverStyle.toModifier()
                        .onClick { onSatelliteSelected(data) },
                    style = IconStyle.OUTLINED
                )
                MdiKeyboardArrowRight(
                    modifier = IconHoverStyle
                        .toModifier()
                        .onClick { onSatelliteResumeRequested(data) },
                )
            }
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
            // Usamos rgba para el gris (158, 158, 158) con 15% de opacidad (0.15)
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