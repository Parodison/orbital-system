package com.parodison.orbital.system.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

/**
 * Badge tipo "EN VIVO": pastilla chica con un punto que pulsa (escala + glow) mientras [live]
 * sea true. No hay `@keyframes` disponible en esta versión de Kobweb, así que el pulso se
 * simula alternando un estado con `LaunchedEffect` + `transition` CSS, en vez de una animación
 * continua nativa.
 */
@Composable
fun LiveContainer(
    modifier: Modifier = Modifier,
    live: Boolean = true,
    onLiveClicked: () -> Unit = {},
) {
    val liveColor = if (live) AppColors.PrimaryRed else AppColors.OutlineGray
    var pulsed by remember(live) { mutableStateOf(false) }

    LaunchedEffect(live) {
        if (!live) return@LaunchedEffect
        while (true) {
            pulsed = !pulsed
            delay(900.milliseconds)
        }
    }

    Row(
        modifier
            .border(1.px, LineStyle.Solid, liveColor)
            .borderRadius(8.px)
            .backgroundColor(AppColors.DarkBluePrimary)
            .padding(topBottom = 4.px, leftRight = 10.px)
            .onClick {
                if (!live) onLiveClicked()
            }
            .then( if (!live) Modifier.cursor(Cursor.Pointer) else Modifier ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.px),
    ) {
        Box(
            Modifier
                .size(8.px)
                .borderRadius(50.percent)
                .backgroundColor(liveColor)
                .styleModifier {
                    property("box-shadow", if (live) "0 0 ${if (pulsed) 8 else 2}px $liveColor" else "none")
                    property("transform", "scale(${if (live && pulsed) 1.3 else 1.0})")
                    property("transition", "all 0.9s ease-in-out")
                }
        )
        SpanText(
            "LIVE",
            modifier = Modifier
                .color(liveColor)
                .fontSize(11.px)
                .fontWeight(700)
                .styleModifier { property("letter-spacing", "0.05em") }
        )
    }
}
