package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.orbital.system.pages.LocalSatelliteScreenActions
import com.parodison.sgp4.Satellite
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject

@Composable
fun SatelliteListComponent(
    modifier: Modifier = Modifier,
    satelliteList: List<Satellite>
) {
    val satelliteScreenActions = LocalSatelliteScreenActions.current
    val satelliteTrackerController: SatelliteTrackerController = koinInject()
    val selectedSatellite by satelliteTrackerController.selectedSatellite.collectAsState()
    val trackingSatellites by satelliteTrackerController.trackingSatellites.collectAsState()

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
                val isSelected = selectedSatellite?.orbitData?.noradCatId == data.orbitData.noradCatId
                val isInTrackingList by remember(trackingSatellites, data) {
                    derivedStateOf { trackingSatellites.contains(data) }
                }

                SatelliteCard(
                    modifier = Modifier.fillMaxWidth(),
                    data = data,
                    selected = isSelected,
                    favorite = isInTrackingList,
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