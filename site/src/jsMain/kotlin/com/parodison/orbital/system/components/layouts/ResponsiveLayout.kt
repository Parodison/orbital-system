package com.parodison.orbital.system.components.layouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.parodison.orbital.system.components.sidebar.Sidebar
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.css.BoxSizing
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderBottom
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.borderRight
import com.varabyte.kobweb.compose.ui.modifiers.borderTop
import com.varabyte.kobweb.compose.ui.modifiers.boxSizing
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.maxHeight
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.compose.ui.modifiers.objectFit
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.browser.window
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.w3c.dom.events.Event

@Layout
@Composable
fun ResponsiveLayout(
    context: PageContext,
    content: @Composable () -> Unit
) {
    var width by remember { mutableStateOf(window.innerWidth) }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = { width = window.innerWidth }
        window.addEventListener("resize", listener)
        onDispose {
            window.removeEventListener("resize", listener)
        }
    }

    val windowSize = remember(width) { WindowSize(width, windowSizeClassOf(width)) }

    CompositionLocalProvider(LocalWindowSize provides windowSize) {
        Surface(
            modifier = Modifier.fillMaxWidth()
                .height(100.vh)
                .maxHeight(100.vh)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .maxHeight(100.percent)
                    .boxSizing(BoxSizing.BorderBox),
            ) {
                if (windowSize.sizeClass == WindowSizeClass.Mobile) {
                    MobileLayout(context) {
                        content()
                    }
                } else {
                    DesktopLayout(context) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.MobileLayout(
    context: PageContext,
    content: @Composable () -> Unit
) {
    Box(
        Modifier.weight(1f).fillMaxWidth().padding(15.px)
    ) {
        content()
    }
}

@Composable
private fun ColumnScope.DesktopLayout(
    context: PageContext,
    content: @Composable () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(20.px),
        horizontalArrangement = Arrangement.spacedBy(10.px),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(10.px)
        ) {
            SpanText(
                "ORBITAL SYSTEM",
                modifier = Modifier
                    .fontSize(20.px)
                    .fontWeight(FontWeight.Bold)
            )
        }
    }
    Row(
        Modifier.fillMaxWidth().weight(1f).minHeight(0.px),
        horizontalArrangement = Arrangement.spacedBy(25.px)
    ) {

        Sidebar(
            context,
            Modifier
                .width(350.px)
                .fillMaxHeight()
                .backgroundColor(AppColors.DarkBluePrimary)
                .borderTop(2.px, LineStyle.Solid, AppColors.OutlineGray)
                .borderBottom(2.px, LineStyle.Solid, AppColors.OutlineGray)
                .borderRight(2.px, LineStyle.Solid, AppColors.OutlineGray)
                .borderRadius(topRight = 15.px, bottomRight = 15.px)
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .overflow(overflowX = Overflow.Hidden, overflowY = Overflow.Scroll)
        ) {
            content()
        }
    }
}