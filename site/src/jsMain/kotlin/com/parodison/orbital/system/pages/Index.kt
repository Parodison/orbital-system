package com.parodison.orbital.system.pages

import androidx.compose.runtime.*
import com.parodison.orbital.system.components.dom.AnimatedVisibility
import com.parodison.orbital.system.components.dom.ModalBottomSheet
import com.parodison.orbital.system.components.dom.rememberAnimatedVisibilityState
import com.parodison.orbital.system.components.dom.rememberModalBottomSheetState
import com.parodison.orbital.system.components.layouts.LocalWindowSize
import com.parodison.orbital.system.components.layouts.WindowSizeClass
import com.parodison.orbital.system.components.satellite.SatelliteListScreen
import com.parodison.orbital.system.components.satellite.SatelliteResume
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbit.core.satellite.Satellite
import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
import com.parodison.orbit.core.satellite.model.toSatellite
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.layout.Layout
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject

data class SatelliteScreenActions(
    val onSatelliteSelected: (Satellite) -> Unit,
    val onSatelliteResumeRequested: (OrbitMeanElementsMessage) -> Unit,
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
    val resumeVisibilityState = rememberAnimatedVisibilityState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val windowSize = LocalWindowSize.current

    LaunchedEffect(satelliteSelectedForResume) {
        if (satelliteSelectedForResume != null) {
            resumeVisibilityState.show()
        }
    }


    val satelliteScreenActions = SatelliteScreenActions(
        onSatelliteSelected = {
            satelliteController.trackSatellite(it)
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
                .overflow(Overflow.Hidden)
                .padding(leftRight = 16.px),
            horizontalArrangement = Arrangement.spacedBy(10.px),
        ) {
            SatelliteListScreen(modifier = Modifier.fillMaxHeight())
            satelliteSelectedForResume?.let {
                if (windowSize.sizeClass == WindowSizeClass.Mobile) {
                    ModalBottomSheet(
                        onDismissRequest = { satelliteController.clearSelectedSatellite() },
                        sheetState = bottomSheetState,
                    ) {
                        SatelliteResume(
                            it,
                            onCloseRequested = {
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                    satelliteController.clearSelectedSatellite()
                                }
                            },
                        )
                    }
                } else {
                    AnimatedVisibility(
                        state = resumeVisibilityState,
                        modifier = Modifier.fillMaxWidth(90.percent),
                        durationMs = 300.ms
                    ) {
                        SatelliteResume(
                            it,
                            onCloseRequested = {
                                resumeVisibilityState.hide { satelliteController.clearSelectedSatellite() }
                            },
                        )
                    }
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
