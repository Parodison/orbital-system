package com.parodison.orbital.system.pages.groundstations

import androidx.compose.runtime.*
import com.parodison.orbital.system.components.dom.AnimatedVisibility
import com.parodison.orbital.system.components.dom.ModalBottomSheet
import com.parodison.orbital.system.components.dom.rememberAnimatedVisibilityState
import com.parodison.orbital.system.components.dom.rememberModalBottomSheetState
import com.parodison.orbital.system.components.groundstation.GroundStationListScreen
import com.parodison.orbital.system.components.groundstation.GroundStationResume
import com.parodison.orbital.system.components.layouts.LocalWindowSize
import com.parodison.orbital.system.components.layouts.WindowSizeClass
import com.parodison.orbital.system.controllers.GroundStationController
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

@Page
@Layout(".components.layouts.ResponsiveLayout")
@Composable
fun GroundStationsPage() {
    val groundStationController: GroundStationController = koinInject()
    val selectedStation by groundStationController.selectedStation.collectAsState()
    val resumeVisibilityState = rememberAnimatedVisibilityState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val windowSize = LocalWindowSize.current

    LaunchedEffect(selectedStation) {
        if (selectedStation != null) {
            resumeVisibilityState.show()
        }
    }

    Row(
        Modifier
            .fillMaxSize()
            .overflow(Overflow.Hidden)
            .padding(leftRight = 16.px),
        horizontalArrangement = Arrangement.spacedBy(10.px),
    ) {
        GroundStationListScreen(
            modifier = Modifier.fillMaxHeight(),
            onStationSelected = { groundStationController.selectStation(it) },
        )
        selectedStation?.let {
            if (windowSize.sizeClass == WindowSizeClass.Mobile) {
                ModalBottomSheet(
                    onDismissRequest = { groundStationController.clearSelectedStation() },
                    sheetState = bottomSheetState,
                ) {
                    GroundStationResume(
                        it,
                        onCloseRequested = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                                groundStationController.clearSelectedStation()
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
                    GroundStationResume(
                        it,
                        onCloseRequested = {
                            resumeVisibilityState.hide { groundStationController.clearSelectedStation() }
                        },
                    )
                }
            }
        }
    }
}
