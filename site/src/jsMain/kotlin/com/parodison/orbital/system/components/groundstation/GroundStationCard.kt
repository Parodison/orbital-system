package com.parodison.orbital.system.components.groundstation

import androidx.compose.runtime.Composable
import com.parodison.orbital.system.components.satellite.SatelliteCardContainerStyle
import com.parodison.orbital.system.core.roundTo
import com.parodison.orbit.core.groundstation.dto.GroundStation
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.silk.components.icons.mdi.MdIcon
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.px

@Composable
fun GroundStationCard(
    modifier: Modifier = Modifier,
    data: GroundStation,
    onClick: () -> Unit,
) {
    Box(
        modifier = SatelliteCardContainerStyle
            .toModifier()
            .then(modifier)
            .onClick { onClick() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.px),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.px),
        ) {
            MdIcon(
                "settings_input_antenna",
                modifier = Modifier.width(40.px).color(Colors.LightGray),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.px),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.px),
                ) {
                    SpanText(
                        data.name,
                        modifier = Modifier.fontWeight(600).fontSize(15.px).color(Colors.White),
                    )
                    GroundStationActiveTag(active = data.status.isActive)
                }
                val coordinatesText = if (data.coordinates != null) {
                    "Latitud: ${data.coordinates!!.latitudeDeg}, Longitude: ${data.coordinates!!.longitudeDeg}"
                } else {
                    "Requiere configurar ubicación"
                }
                SpanText(
                    coordinatesText,
                    modifier = Modifier.fontSize(11.px).color(Colors.Gray),
                )
            }
        }
    }
}
