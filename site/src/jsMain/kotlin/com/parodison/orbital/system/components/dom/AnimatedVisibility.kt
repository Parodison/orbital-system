package com.parodison.orbital.system.components.dom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.onTransitionEnd
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.silk.style.CssStyle
import org.jetbrains.compose.web.css.AnimationTimingFunction
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.px

class AnimatedVisibilityState internal constructor() {
    var visible by mutableStateOf(true)
        private set
    private var onHidden: (() -> Unit)? = null

    fun hide(onHidden: () -> Unit = {}) {
        this.onHidden = onHidden
        visible = false
    }

    fun show() {
        onHidden = null
        visible = true
    }

    internal fun notifyHidden() {
        onHidden?.invoke()
        onHidden = null
    }
}

@Composable
fun rememberAnimatedVisibilityState() = remember { AnimatedVisibilityState() }

@Composable
fun AnimatedVisibility(
    state: AnimatedVisibilityState = rememberAnimatedVisibilityState(),
    modifier: Modifier = Modifier,
    durationMs: CSSSizeValue<CSSUnit.ms> = 200.ms,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var contentMounted by remember { mutableStateOf(false) }

    LaunchedEffect(state.visible) {
        if (state.visible) {
            contentMounted = true
            expanded = true
        } else {
            expanded = false
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
            .onTransitionEnd {
                if (!expanded) {
                    contentMounted = false
                    state.notifyHidden()
                }
            }
            .then(if (expanded) modifier.opacity(1) else Modifier)
    ) {
        if (contentMounted) {
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
