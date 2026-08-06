package com.parodison.orbital.system.components.dom

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.components.satellite.IconHoverStyle
import com.varabyte.kobweb.compose.css.PointerEvents
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.pointerEvents
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.icons.mdi.MdiKeyboardArrowLeft
import com.varabyte.kobweb.silk.components.icons.mdi.MdiKeyboardArrowRight
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.px

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.px),
    ) {
        MdiKeyboardArrowLeft(
            modifier = IconHoverStyle.toModifier()
                .color(Colors.LightGray)
                .thenIf(!hasPrevious) { Modifier.opacity(0.3f).pointerEvents(PointerEvents.None) }
                .onClick { onPreviousPage() }
        )
        SpanText(
            "$currentPage / ${totalPages.coerceAtLeast(1)}",
            modifier = Modifier
                .fontSize(12.px)
                .color(Colors.LightGray)
        )
        MdiKeyboardArrowRight(
            modifier = IconHoverStyle.toModifier()
                .color(Colors.LightGray)
                .thenIf(!hasNext) { Modifier.opacity(0.3f).pointerEvents(PointerEvents.None) }
                .onClick { onNextPage() }
        )
    }
}
