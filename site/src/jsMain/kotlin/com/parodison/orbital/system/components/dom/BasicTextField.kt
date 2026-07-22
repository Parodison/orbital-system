package com.parodison.orbital.system.components.dom

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.dom.ElementRefScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.caretColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.outline
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.placeholder
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Input
import org.w3c.dom.HTMLInputElement

@Composable
fun BasicInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    onRef: (HTMLInputElement) -> Unit = {},
) {
    Input(
        type = InputType.Text,
        attrs = BasicInputStyle.toModifier()
            .then(modifier)
            .toAttrs {
                ref { element -> 
                    onRef(element)
                    onDispose {  }
                }
                value(value)
                onInput { event ->
                    onValueChange(event.value)
                }
                placeholder?.let {
                    placeholder(it)
                }
            }
    )
}

val BasicInputStyle = CssStyle {
    base {
        Modifier
            .border(0.px)
            .outline(0.px)
            .backgroundColor(Colors.Transparent)
            .color(AppColors.OutlineGray)
            .margin(0.px)
            .padding(0.px)
            .color(Colors.White)
            .fontSize(16.px)
            .caretColor(AppColors.PrimaryRed)
    }

    placeholder {
        Modifier
            .color(Colors.LightGray)
            .opacity(1)
    }
}