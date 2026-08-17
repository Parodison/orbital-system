package com.parodison.orbital.system.components.location

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.styleModifier
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb

/** Réplica del "blue dot" de Google Maps: círculo sólido con borde blanco + halo/sombreado difuso alrededor. Nada más. */
@Composable
fun BlueDotContainer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(44.px),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(16.px)
                .borderRadius(50.percent)
                .backgroundColor(rgb(66, 133, 244))
                .border(2.px, LineStyle.Solid, Colors.White)
                .styleModifier {
                    property("box-shadow", "0 1px 4px rgba(0, 0, 0, 0.35)")
                }
        )
    }
}
