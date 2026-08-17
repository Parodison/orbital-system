package com.parodison.orbital.system.components.material3

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.styleModifier
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.dom.TagElement
import org.w3c.dom.HTMLElement

fun Modifier.rippleColors(
    hoverColor: Color.Rgb = Colors.White,
    hoverOpacity: Number = 0.12,
    pressedColor: Color.Rgb = hoverColor,
    pressedOpacity: Number = 0.24,
): Modifier = this.styleModifier {
    property("--md-ripple-hover-color", hoverColor.toString())
    property("--md-ripple-hover-opacity", hoverOpacity.toString())
    property("--md-ripple-pressed-color", pressedColor.toString())
    property("--md-ripple-pressed-opacity", pressedOpacity.toString())
}

@Composable
fun RippleSurface(
    modifier: Modifier = Modifier,
    hoverColor: Color.Rgb = Colors.White,
    hoverOpacity: Number = 0.12,
    pressedColor: Color.Rgb = hoverColor,
    pressedOpacity: Number = 0.24,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .position(Position.Relative)
            .rippleColors(hoverColor, hoverOpacity, pressedColor, pressedOpacity)
    ) {
        TagElement<HTMLElement>(tagName = "md-ripple", applyAttrs = null) {}
        content()
    }
}