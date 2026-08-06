package com.parodison.orbital.system.components.dom

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.padding
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text

/** Capped at 100 — 100 is already the max page size the server accepts. */
val PAGE_SIZE_OPTIONS = listOf(10, 25, 50, 100)

@Composable
fun PageSizeSelect(
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .backgroundColor(AppColors.DarkBluePrimary)
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .padding(topBottom = 4.px, leftRight = 6.px)
    ) {
        Select(
            attrs = {
                onChange { event -> event.value?.toIntOrNull()?.let(onPageSizeChange) }
                style {
                    property("background", "transparent")
                    property("border", "none")
                    property("outline", "none")
                    property("color", "white")
                    property("font-size", "12px")
                }
            }
        ) {
            PAGE_SIZE_OPTIONS.forEach { size ->
                Option(
                    value = size.toString(),
                    attrs = { if (size == pageSize) selected() }
                ) {
                    Text("$size por página")
                }
            }
        }
    }
}
