package com.parodison.orbital.system

import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.color

@InitSilk
fun initTheme(ctx: InitSilkContext) {
    ctx.config.initialColorMode = ColorMode.DARK

    ctx.theme.palettes.dark.apply {
        background = AppColors.DarkBlue
        color = Colors.White
    }

    ctx.stylesheet.registerStyleBase("body") {
        Modifier
            .backgroundColor(AppColors.DarkBlue)
            .color(Colors.White)
            .fontFamily("Inter")
    }

}
