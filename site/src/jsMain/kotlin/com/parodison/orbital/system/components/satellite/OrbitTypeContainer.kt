package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.core.AppColors
import com.parodison.sgp4.model.OrbitType
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
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
            .borderRadius(8.px)
            .backgroundColor(AppColors.PrimaryBlue.copyf(alpha = 0.2f))
            .border(1.px, LineStyle.Solid, AppColors.PrimaryBlue)
            .color(AppColors.PrimaryBlue.copy(green = 150, blue = 250))
    ) {
        SpanText(orbitType.name)
    }
}