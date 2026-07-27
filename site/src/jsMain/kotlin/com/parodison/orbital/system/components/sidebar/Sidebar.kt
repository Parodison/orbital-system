package com.parodison.orbital.system.components.sidebar

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.silk.components.icons.mdi.IconStyle
import com.varabyte.kobweb.silk.components.icons.mdi.MdIcon
import com.varabyte.kobweb.silk.components.layout.Surface
import org.jetbrains.compose.web.css.px

data class RouteItem(
    val label: String,
    val icon: String,
    val route: String
)

val routeItems = listOf<RouteItem>(
    RouteItem(
        label = "Satélites",
        icon = "satellite_alt",
        route = "/"
    ),
    RouteItem(
        label = "Mapa",
        icon = "map",
        route = "/mapa/"
    )

)


@Composable
fun RowScope.Sidebar(context: PageContext, modifier: Modifier = Modifier) {
    println(context.route.path)
    Surface(
        modifier = modifier
            .backgroundColor(AppColors.DarkBluePrimary)
            .overflow(Overflow.Scroll)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.px),
            modifier = Modifier.margin(top = 20.px)
        ) {

            routeItems.forEach { item ->
                println("Estamos en la ruta del item: ${item.label}?: ${context.route.path == item.route}")
                SidebarElement(
                    label = item.label,
                    icon = {
                        MdIcon(
                            item.icon,
                            modifier = Modifier.width(30.px),
                            style = if (context.route.path == item.route) IconStyle.FILLED else IconStyle.OUTLINED
                        )
                    },
                    selected = context.route.path == item.route,
                    onClick = {
                        context.router.navigateTo(item.route)
                    }
                )
            }
        }
    }
}