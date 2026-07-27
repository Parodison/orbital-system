package com.parodison.orbital.system.components.dom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.onTransitionEnd
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.textWrap
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.translateY
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.css.AnimationTimingFunction
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.mm
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.px
import kotlin.time.Duration.Companion.milliseconds

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
    durationMs: CSSSizeValue<CSSUnit.ms> = 200.ms,
    content: @Composable () -> Unit,
) {
    var mounted by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            mounted = true
        } else {
            delay(durationMs.value.toLong().milliseconds)
            mounted = false
        }
    }


    Box(
        modifier = Modifier
            .width(0.px)
            .opacity(0)
            .whiteSpace(WhiteSpace.NoWrap)
            .transition {
                property("width")
                duration(durationMs)
                timingFunction(AnimationTimingFunction("cubic-bezier(0.4, 0, 0.2, 1)"))
            }
            .then(if (visible) modifier.opacity(1) else Modifier)
    ) {
        if (mounted) {
            content()
        }
    }
}

val AnimatedVisibilityStyle = CssStyle {
    base {
        Modifier
            .width(0.px)
            .opacity(0)
            .overflow(Overflow.Hidden)
            .whiteSpace(WhiteSpace.NoWrap)
    }

}
