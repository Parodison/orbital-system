package com.parodison.orbital.system.components.groundstation

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbit.core.groundstation.dto.GroundStationStatus
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px

val GroundStationStatus.isActive: Boolean get() = this is GroundStationStatus.Connected

private val ActiveGreen = Color.rgb(70, 224, 130)
@Composable
fun GroundStationActiveTag(active: Boolean) {
    val color = if (active) ActiveGreen else AppColors.OutlineGray
    Box(
        Modifier
            .padding(topBottom = 2.px, leftRight = 7.px)
            .borderRadius(6.px)
            .backgroundColor(color.copyf(alpha = 0.18f))
            .border(1.px, LineStyle.Solid, color.copyf(alpha = 0.5f))
    ) {
        SpanText(
            if (active) "CONECTADO" else "INACTIVO",
            modifier = Modifier
                .fontSize(11.px)
                .fontWeight(700)
                .color(if (active) color.lightened(0.1f) else Colors.LightGray)
        )
    }
}
