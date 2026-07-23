package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.parodison.orbital.system.components.dom.SearchInput
import com.parodison.orbital.system.controllers.SatelliteListState
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.varabyte.kobweb.compose.css.BoxSizing
import com.varabyte.kobweb.compose.css.FontSize
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.boxSizing
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject

@Composable
fun SatelliteListScreen() {
    val satelliteTrackerController: SatelliteTrackerController = koinInject()
    val satelliteListState by satelliteTrackerController.satelliteListState.collectAsState()

    var satelliteSearchText by remember { mutableStateOf("") }




    when(val s = satelliteListState) {
        is SatelliteListState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.px),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(leftRight = 20.px)
                        .boxSizing(BoxSizing.BorderBox),
                    verticalArrangement = Arrangement.spacedBy(15.px),
                ) {
                    SpanText(
                        "Satélites",
                        modifier = Modifier
                            .fontSize(FontSize.XLarge)
                            .fontWeight(600),

                        )
                    SearchInput(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(topBottom = 10.px),
                        value = satelliteSearchText,
                        onValueChange = { satelliteSearchText = it },
                        placeholder = "Buscar Satélites..."
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SatelliteListComponent(
                        modifier = Modifier.fillMaxWidth()
                            .padding(leftRight = 20.px),
                        satelliteList = s.data
                    )
                }
            }
        }
        else -> {
            SpanText("Pendiente de implementación")
        }
    }
}