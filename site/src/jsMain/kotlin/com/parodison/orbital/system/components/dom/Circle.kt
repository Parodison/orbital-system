package com.parodison.orbital.system.components.dom

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.styleModifier
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.CSSNumericValue
import org.jetbrains.compose.web.css.CSSUnitLengthOrPercentage
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb

@Composable
fun Circle(
    size: CSSNumericValue<out CSSUnitLengthOrPercentage> = 8.px,
    color: CSSColorValue = AppColors.PrimaryBlue,
    showShadow: Boolean = false,
) {
    Box(
        Modifier
            .size(size)
            .borderRadius(50.percent)
            .backgroundColor(color)
            .styleModifier {
                if (showShadow) {
                    property("box-shadow", "0 0 10px rgba(70, 224, 130, 0.75)")
                }
            }
    )
}