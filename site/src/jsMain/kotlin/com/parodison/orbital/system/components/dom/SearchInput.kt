package com.parodison.orbital.system.components.dom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.flex
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSearch
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.w3c.dom.HTMLInputElement

@Composable
fun SearchInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
) {
    var inputElement by remember { mutableStateOf<HTMLInputElement?>(null) }

    Box(
        modifier = modifier
            .backgroundColor(AppColors.DarkBluePrimary)
            .border(1.5.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .overflow { Overflow.Hidden }
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MdiSearch(
                modifier = Modifier
                    .backgroundColor(Colors.Transparent)
                    .color(Colors.LightGray)
                    .margin(leftRight = 10.px)
            )
            BasicInput(
                modifier = Modifier
                    .flex(1)
                    .onClick { inputElement?.focus() },
                value = value,
                onValueChange = onValueChange,
                onRef = {
                    inputElement = it
                },
                placeholder = placeholder
            )
        }
    }
}