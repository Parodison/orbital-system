package com.parodison.orbital.system.components.layouts

import androidx.compose.runtime.staticCompositionLocalOf

/** Tres niveles de tamaño de ventana: mobile, desktop, y pantallas más grandes que eso. */
enum class WindowSizeClass {
    Mobile,
    Desktop,
    Large,
}

data class WindowSize(
    val widthPx: Int,
    val sizeClass: WindowSizeClass,
)

private const val MOBILE_MAX_WIDTH_PX = 768
private const val DESKTOP_MAX_WIDTH_PX = 1440

fun windowSizeClassOf(widthPx: Int): WindowSizeClass = when {
    widthPx < MOBILE_MAX_WIDTH_PX -> WindowSizeClass.Mobile
    widthPx < DESKTOP_MAX_WIDTH_PX -> WindowSizeClass.Desktop
    else -> WindowSizeClass.Large
}

/** Provisto por [ResponsiveLayout] — cualquier composable hijo puede leer `LocalWindowSize.current`. */
val LocalWindowSize = staticCompositionLocalOf<WindowSize> {
    error("LocalWindowSize no fue provisto — ¿el composable está fuera de ResponsiveLayout?")
}
