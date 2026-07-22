package com.parodison.orbital.system.components.sidebar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.css.Cursor
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
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.dpi
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Span

@Composable
fun SidebarElement(
    selected: Boolean = false,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {

    var isHovered by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .height(50.px)

            .backgroundColor(if (isHovered) AppColors.DarkBlueSecondary else Colors.Transparent)
            .then(
                if (selected) {
                    Modifier
                        .backgroundColor(AppColors.DarkBlueSecondary)
                } else {
                    Modifier
                        .onMouseEnter { isHovered = true }
                        .onMouseLeave { isHovered = false }
                        .cursor(Cursor.Pointer)
                        .onClick { onClick() }
                }
            )
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.px)
    ) {
        Span(
            attrs = Modifier
                .width(4.px)
                .borderRadius(20.px)
                .fillMaxHeight()
                .then(
                    if (selected) {
                        Modifier.backgroundColor(AppColors.PrimaryRed)
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