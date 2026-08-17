package com.parodison.orbital.system.components.dom

import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.px
import org.jetbrains.skia.FontWeight

val ButtonStyle = CssStyle {
    base {
        Modifier
            .position(Position.Relative)
            .padding(topBottom = 10.px, leftRight = 10.px)
            .border(0.px, LineStyle.None, Colors.Transparent)
            .borderRadius(8.px)
            .overflow(Overflow.Hidden)
            .cursor(Cursor.Pointer)
            .fontWeight(600)
            .fontSize(14.px)
    }

    // Definimos el ::after de forma limpia usando cssRule para que pertenezca al botón
    cssRule("::after") {
        Modifier
            .styleModifier {
                property("content", "''")
                property("position", "absolute")
                property("inset", "0")
                property("background-color", "rgb(128 128 128 / 0.15)")
                property("opacity", "0")
                property("pointer-events", "none")
                property("border-radius", "inherit")
                property("transition", "opacity 0.15s ease")
            }
    }

    // Al hacer hover, simplemente encendemos la opacidad del ::after ya existente
    cssRule(":hover::after") {
        Modifier
            .styleModifier {
                property("opacity", "1")
            }
    }
}