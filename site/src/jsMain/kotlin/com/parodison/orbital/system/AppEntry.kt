package com.parodison.orbital.system

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.components.layouts.ResponsiveLayout
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.style.common.SmoothColorStyle
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.SilkTheme
import com.varabyte.kobweb.silk.theme.colors.palette.background
import kotlinx.browser.window
import org.jetbrains.compose.web.css.vh


@App
@Composable
fun AppEntry(content: @Composable () -> Unit) {
    SilkApp {
        Surface(
            Modifier
                .fillMaxSize(),
        ) {
            content()
        }
    }
}
