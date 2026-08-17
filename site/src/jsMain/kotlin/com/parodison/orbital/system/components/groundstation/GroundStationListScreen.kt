package com.parodison.orbital.system.components.groundstation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.parodison.orbital.system.components.dom.PageSizeSelect
import com.parodison.orbital.system.components.dom.PaginationControls
import com.parodison.orbital.system.components.dom.SearchInput
import com.parodison.orbital.system.controllers.GroundStationController
import com.parodison.orbital.system.controllers.GroundStationListState
import com.parodison.orbit.core.groundstation.dto.GroundStation
import com.varabyte.kobweb.compose.css.BoxSizing
import com.varabyte.kobweb.compose.css.FontSize
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.boxSizing
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun GroundStationListScreen(
    modifier: Modifier = Modifier,
    onStationSelected: (GroundStation) -> Unit,
) {
    val groundStationController: GroundStationController = koinInject()
    val groundStationListState by groundStationController.groundStationListState.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var debouncedSearchText by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(100) }

    LaunchedEffect(Unit) {
        snapshotFlow { searchText }
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .collect {
                debouncedSearchText = it
                currentPage = 1
            }
    }

    LaunchedEffect(debouncedSearchText, currentPage, pageSize) {
        groundStationController.loadGroundStationList(
            searchText = debouncedSearchText.ifBlank { null },
            page = currentPage,
            pageSize = pageSize,
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.px),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .boxSizing(BoxSizing.BorderBox),
            verticalArrangement = Arrangement.spacedBy(15.px),
        ) {
            SpanText(
                "Estaciones terrenas",
                modifier = Modifier
                    .fontSize(FontSize.XLarge)
                    .fontWeight(600),
            )
            SearchInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(topBottom = 10.px),
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = "Buscar estaciones...",
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .minHeight(0.px)
        ) {
            when (val s = groundStationListState) {
                is GroundStationListState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.px),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SpanText(
                                "${s.data.totalItems} estaciones encontradas",
                                modifier = Modifier
                                    .fontSize(12.px)
                                    .color(Colors.LightGray)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.px),
                            ) {
                                PageSizeSelect(
                                    pageSize = pageSize,
                                    onPageSizeChange = {
                                        pageSize = it
                                        currentPage = 1
                                    },
                                )
                                PaginationControls(
                                    currentPage = s.data.currentPage,
                                    totalPages = s.data.totalPages,
                                    hasPrevious = s.data.hasPrevious,
                                    hasNext = s.data.hasNext,
                                    onPreviousPage = { currentPage = (currentPage - 1).coerceAtLeast(1) },
                                    onNextPage = { currentPage += 1 },
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .minHeight(0.px)
                                .overflow(overflowX = Overflow.Hidden, overflowY = Overflow.Scroll),
                            verticalArrangement = Arrangement.spacedBy(10.px),
                        ) {
                            s.data.content.forEach { station ->
                                GroundStationCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    data = station,
                                    onClick = { onStationSelected(station) },
                                )
                            }
                        }
                    }
                }
                is GroundStationListState.Loading -> {
                    SpanText("Cargando estaciones...")
                }
                is GroundStationListState.Error -> {
                    SpanText("Error: ${s.message}")
                }
                is GroundStationListState.Idle -> {
                    SpanText("Sin datos")
                }
            }
        }
    }
}
