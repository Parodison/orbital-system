package com.parodison.orbital.system.components.groundstation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.parodison.orbital.system.components.dom.SearchInput
import com.parodison.orbital.system.core.AppColors
import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
import com.parodison.shared.dto.Page
import com.parodison.shared.resources.SatelliteResource
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.TextOverflow
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.maxHeight
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textOverflow
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.silk.components.icons.mdi.MdiClose
import com.varabyte.kobweb.silk.components.icons.mdi.MdiSatelliteAlt
import com.varabyte.kobweb.silk.components.text.SpanText
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.TagElement
import org.koin.compose.koinInject
import org.w3c.dom.HTMLDialogElement
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TrackSatelliteDialog(
    onDismissRequest: () -> Unit,
    onSatelliteChosen: (OrbitMeanElementsMessage) -> Unit,
) {
    TagElement<HTMLDialogElement>(
        tagName = "dialog",
        applyAttrs = {
            style {
                property("background", "transparent")
                property("border", "none")
                property("padding", "0")
                property("max-width", "none")
            }
            // El diálogo nativo se cierra solo con Escape/backdrop — sin esto, el estado de
            // Compose queda desincronizado y showModal() falla en el siguiente intento de abrirlo.
            addEventListener("close") { onDismissRequest() }
        }
    ) {
        DisposableEffect(Unit) {
            scopeElement.showModal()
            onDispose { }
        }

        Column(
            modifier = Modifier
                .width(94.percent)
                .maxWidth(560.px)
                .height(85.vh)
                .maxHeight(700.px)
                .backgroundColor(AppColors.DarkBluePrimary)
                .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
                .borderRadius(10.px)
                .padding(15.px),
            verticalArrangement = Arrangement.spacedBy(10.px),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpanText(
                    "Rastrear satélite",
                    modifier = Modifier.fontWeight(600).fontSize(16.px),
                )
                MdiClose(
                    modifier = Modifier
                        .cursor(Cursor.Pointer)
                        .color(Colors.White)
                        .onClick { onDismissRequest() },
                )
            }
            SatellitePicker(
                modifier = Modifier.fillMaxWidth().weight(1f).minHeight(0.px),
                onSatelliteChosen = onSatelliteChosen,
            )
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun SatellitePicker(
    modifier: Modifier = Modifier,
    onSatelliteChosen: (OrbitMeanElementsMessage) -> Unit,
) {
    val client: HttpClient = koinInject()
    var searchText by remember { mutableStateOf("") }
    var debouncedSearchText by remember { mutableStateOf("") }
    var satellites by remember { mutableStateOf<List<OrbitMeanElementsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        snapshotFlow { searchText }
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .collect { debouncedSearchText = it }
    }

    LaunchedEffect(debouncedSearchText) {
        isLoading = true
        errorMessage = null
        try {
            val response = client.get(
                SatelliteResource(searchText = debouncedSearchText.ifBlank { null }, pageSize = 30)
            ) {
                accept(ContentType.Application.Cbor)
            }
            if (response.status.isSuccess()) {
                satellites = response.body<Page<OrbitMeanElementsMessage>>().content
            } else {
                errorMessage = "Ha ocurrido un error con status code ${response.status.value}"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Ha ocurrido un error inesperado"
        }
        isLoading = false
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.px)) {
        SearchInput(
            modifier = Modifier.fillMaxWidth(),
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = "Buscar satélites...",
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .minHeight(0.px)
                .overflow(overflowX = Overflow.Hidden, overflowY = Overflow.Auto)
        ) {
            when {
                isLoading && satellites.isEmpty() -> {
                    SpanText(
                        "Cargando...",
                        modifier = Modifier.padding(20.px).fontSize(12.px).color(Colors.Gray),
                    )
                }
                errorMessage != null -> {
                    SpanText(
                        errorMessage.orEmpty(),
                        modifier = Modifier.padding(20.px).fontSize(12.px).color(AppColors.PrimaryRed),
                    )
                }
                satellites.isEmpty() -> {
                    SpanText(
                        "Sin resultados",
                        modifier = Modifier.padding(20.px).fontSize(12.px).color(Colors.Gray),
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.px)) {
                        satellites.forEach { omm ->
                            SatellitePickerRow(omm = omm, onClick = { onSatelliteChosen(omm) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SatellitePickerRow(omm: OrbitMeanElementsMessage, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .padding(10.px)
            .cursor(Cursor.Pointer)
            .onClick { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.px),
    ) {
        MdiSatelliteAlt(modifier = Modifier.fontSize(22.px).color(Colors.LightGray))
        Column {
            SpanText(
                omm.objectName,
                modifier = Modifier
                    .whiteSpace(WhiteSpace.NoWrap)
                    .overflow(Overflow.Hidden)
                    .textOverflow(TextOverflow.Ellipsis)
                    .fontWeight(600)
                    .fontSize(14.px)
                    .color(Colors.White),
            )
            SpanText(
                "NORAD ID: ${omm.noradCatId}",
                modifier = Modifier.fontSize(11.px).color(Colors.Gray),
            )
        }
    }
}
