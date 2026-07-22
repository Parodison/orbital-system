package com.parodison.orbital.system.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.px

@Composable
fun MetaDataItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    name: String,
    value: String
) {
    Row(
        modifier = modifier.padding(10.px).width(200.px),
        horizontalArrangement = Arrangement.spacedBy(10.px)
    ) {
        icon?.invoke()
        Column(
            verticalArrangement = Arrangement.spacedBy(10.px),
        ) {
            SpanText(
                name,
                modifier = Modifier.color(Colors.Gray)
            )
            SpanText(value)
        }
    }
}