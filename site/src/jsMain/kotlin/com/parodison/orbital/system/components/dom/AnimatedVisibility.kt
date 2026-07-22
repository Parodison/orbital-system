package com.parodison.orbital.system.components.dom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.onTransitionEnd
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.translateY
import kotlinx.browser.window
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.px

/**
 * Equivalente simplificado de `AnimatedVisibility` de Jetpack Compose para Kobweb.
 *
 * Compose HTML no anima elementos que ya salieron del árbol de composición, así que el truco
 * es el mismo que usa Compose UI por dentro: separar "¿sigue montado?" de "¿en estado final?".
 * Al ocultar, [content] sigue montado un instante más para que la transición CSS alcance a
 * jugar, y recién se desmonta cuando el navegador confirma el fin de la transición.
 */
@Composable
fun AnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMs: Int = 200,
    slideDistancePx: Int = -8,
    content: @Composable () -> Unit,
) {
    var mounted by remember { mutableStateOf(visible) }
    var animatedIn by remember { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        if (visible) {
            mounted = true
            window.requestAnimationFrame { animatedIn = true }
        } else {
            animatedIn = false
        }
    }

    if (mounted) {
        Box(
            modifier
                .opacity(if (animatedIn) 1.0 else 0.0)
                .translateY(if (animatedIn) 0.px else slideDistancePx.px)
                .transition {
                    property("opacity", "transform")
                    duration(durationMs.ms)
                }
                .onTransitionEnd {
                    if (!animatedIn) mounted = false
                }
        ) {
            content()
        }
    }
}
