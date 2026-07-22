package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.orbital.system.models.OrbitData
import com.parodison.orbital.system.pages.LocalSatelliteScreenActions
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject

@Composable
fun SatelliteListComponent(
    modifier: Modifier = Modifier,
    satelliteList: List<OrbitData>
) {
    val satelliteScreenActions = LocalSatelliteScreenActions.current
    val satelliteTrackerController: SatelliteTrackerController = koinInject()
    val selectedSatellite by satelliteTrackerController.selectedSatellite.collectAsState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.px),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpanText("${satelliteList.size} satélites encontrados")
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.px),
        ) {
            satelliteList.forEach { data ->
                val isSelected = selectedSatellite?.noradCatId == data.noradCatId

                SatelliteCard(
                    modifier = Modifier.fillMaxWidth(),
                    data = data,
                    selected = isSelected,
                    onSatelliteSelected = {
                        satelliteScreenActions.onSatelliteSelected(it)
                    },
                    onSatelliteResumeRequested = {
                        satelliteScreenActions.onSatelliteResumeRequested(it)
                    },
                )
            }
        }
    }
}