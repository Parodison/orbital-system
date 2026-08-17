package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbit.core.satellite.model.OrbitType
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
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

@Composable
fun OrbitTypeContainer(
    orbitType: OrbitType
) {
    Box(
        Modifier
            .padding(topBottom = 2.px, leftRight = 7.px)
            .borderRadius(6.px)
            .backgroundColor(AppColors.PrimaryBlue.copyf(alpha = 0.18f))
            .border(1.px, LineStyle.Solid, AppColors.PrimaryBlue.copyf(alpha = 0.5f))
    ) {
        SpanText(
            orbitType.name,
            modifier = Modifier
                .fontSize(11.px)
                .fontWeight(700)
                .color(AppColors.PrimaryBlue.lightened(0.3f))
        )
    }
}
