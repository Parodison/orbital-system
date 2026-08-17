package com.parodison.orbital.system.components.sidebar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.TransitionTimingFunction
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.onMouseEnter
import com.varabyte.kobweb.compose.ui.modifiers.onMouseLeave
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.dpi
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.w3c.dom.HTMLElement

val SideBarElementStyle = CssStyle {
    base {
        Modifier
            .position(Position.Relative)
            .fillMaxWidth()
            .height(50.px)
            .backgroundColor(Colors.Transparent)
            .cursor(Cursor.Pointer)
            .styleModifier {
                property("--md-ripple-hover-color", "white")
                property("--md-ripple-hover-opacity", "0.12")
                property("--md-ripple-pressed-color", "white")
                property("--md-ripple-pressed-opacity", "0.24")
            }
    }

    hover {
        Modifier
    }
}

val SidebarSpanStyle = CssStyle {
    base {
        Modifier
            .width(4.px)
            .borderRadius(20.px)
            .height(0.px)
            .backgroundColor(Colors.Transparent)
            .transition {
                property("all")
                duration(200.ms)
                timingFunction(TransitionTimingFunction.EaseInOut)
            }
    }
}

@Composable
fun SidebarElement(
    selected: Boolean = false,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {

    Row(
        SideBarElementStyle.toModifier()
            .onClick { onClick() }
            .then( if (selected) Modifier.backgroundColor(AppColors.DarkBlueSecondary) else Modifier ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.px)
    ) {
        TagElement<HTMLElement>(
            tagName = "md-ripple",
            applyAttrs = null
        ) {

        }
        Span(
            attrs = SidebarSpanStyle.toModifier()
                .then(
                    if (selected) {
                        Modifier.backgroundColor(AppColors.PrimaryRed)
                            .fillMaxHeight()
                    } else {
                        Modifier
                    }
                )
                .toAttrs()
        )
        Row(
            Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.px)
        ) {
            icon()
            SpanText(label)
        }
    }
}

