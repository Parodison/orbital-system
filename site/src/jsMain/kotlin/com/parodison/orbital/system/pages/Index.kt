package com.parodison.orbital.system.pages

import androidx.compose.runtime.*
import com.parodison.orbital.system.components.dom.AnimatedVisibility
import com.parodison.orbital.system.components.layouts.LocalWindowSize
import com.parodison.orbital.system.components.layouts.WindowSizeClass
import com.parodison.orbital.system.components.satellite.SatelliteListScreen
import com.parodison.orbital.system.components.satellite.SatelliteResume
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.sgp4.Satellite
import com.parodison.sgp4.model.OrbitData
import com.parodison.sgp4.model.toSatellite
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.layout.Layout
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject

data class SatelliteScreenActions(
    val onSatelliteSelected: (Satellite) -> Unit,
    val onSatelliteResumeRequested: (OrbitData) -> Unit,
)

val LocalSatelliteScreenActions = staticCompositionLocalOf<SatelliteScreenActions> {
    error("LocalSatelliteScreenActions not provided")
}


@Page
@Layout(".components.layouts.ResponsiveLayout")
@Composable
fun HomePage() {
    val satelliteController: SatelliteTrackerController = koinInject()
    val satelliteSelectedForResume by satelliteController.selectedSatellite.collectAsState()


    val satelliteScreenActions = SatelliteScreenActions(
        onSatelliteSelected = {
            satelliteController.addSatelliteToFavorites(it)
        },
        onSatelliteResumeRequested = {
            satelliteController.updateSelectedSatellite(it.toSatellite())
        },
    )
    val isMobile = LocalWindowSize.current.sizeClass == WindowSizeClass.Mobile

    CompositionLocalProvider(LocalSatelliteScreenActions provides satelliteScreenActions) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(right = 20.px),
            horizontalArrangement = Arrangement.spacedBy(10.px),
        ) {
            SatelliteListScreen()
            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(90.percent),
                visible = satelliteSelectedForResume != null,
                durationMs = 300.ms
            ) {
                satelliteSelectedForResume?.let { satelliteResume ->
                    SatelliteResume(satelliteResume)
                }
            }
        }
    }
    """
        when (val s = satelliteListState) {
        is SatelliteListState.Idle -> {
            SpanText("En espera")
        }
        is SatelliteListState.Loading -> {
            SpanText("Cargando...")
        }
        is SatelliteListState.Success -> {
            SatelliteListScreen(s.data)
        }
        is SatelliteListState.Error -> {
            SpanText(s.message)
        }
    }
    """.trimIndent()
}
