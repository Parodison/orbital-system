package com.parodison.orbital.system.components.satellite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.parodison.orbital.system.components.dom.PAGE_SIZE_OPTIONS
import com.parodison.orbital.system.components.dom.PageSizeSelect
import com.parodison.orbital.system.components.dom.PaginationControls
import com.parodison.orbital.system.components.dom.SearchInput
import com.parodison.orbital.system.controllers.SatelliteListState
import com.parodison.orbital.system.controllers.SatelliteTrackerController
import com.parodison.orbital.system.core.AppColors
import com.parodison.shared.dto.SatGroup
import com.varabyte.kobweb.browser.uri.encodeURIComponent
import com.varabyte.kobweb.compose.css.BoxSizing
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontSize
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.navigation.UpdateHistoryMode
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxSizing
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.flexShrink
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.onMouseDown
import com.varabyte.kobweb.compose.ui.modifiers.onMouseLeave
import com.varabyte.kobweb.compose.ui.modifiers.onMouseMove
import com.varabyte.kobweb.compose.ui.modifiers.onMouseUp
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun SatelliteListScreen(modifier: Modifier = Modifier) {
    val satelliteTrackerController: SatelliteTrackerController = koinInject()
    val satelliteListState by satelliteTrackerController.satelliteListState.collectAsState()
    val ctx = rememberPageContext()

    var satelliteSearchText by remember { mutableStateOf(ctx.route.queryParams["search"] ?: "") }
    var debouncedSearchText by remember { mutableStateOf(satelliteSearchText) }
    var selectedGroup by remember {
        mutableStateOf(ctx.route.queryParams["group"]?.let { name -> SatGroup.entries.find { it.name == name } })
    }
    var currentPage by remember {
        mutableStateOf(ctx.route.queryParams["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1)
    }
    var pageSize by remember {
        mutableStateOf(ctx.route.queryParams["pageSize"]?.toIntOrNull()?.takeIf { it in PAGE_SIZE_OPTIONS } ?: 100)
    }

    var chipsRowElement by remember { mutableStateOf<HTMLElement?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStartX by remember { mutableStateOf(0.0) }
    var dragStartScrollLeft by remember { mutableStateOf(0.0) }
    var dragDistance by remember { mutableStateOf(0.0) }
    var suppressNextClick by remember { mutableStateOf(false) }

    fun onCategoryClick(action: () -> Unit) {
        if (suppressNextClick) {
            suppressNextClick = false
        } else {
            action()
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { satelliteSearchText }
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .collect {
                debouncedSearchText = it
                currentPage = 1
            }
    }

    LaunchedEffect(debouncedSearchText, selectedGroup, currentPage, pageSize) {
        satelliteTrackerController.findSatellitesBySearch(
            searchText = debouncedSearchText.ifBlank { null },
            page = currentPage,
            pageSize = pageSize,
            group = selectedGroup,
        )

        val params = buildList {
            if (debouncedSearchText.isNotBlank()) add("search=${encodeURIComponent(debouncedSearchText)}")
            selectedGroup?.let { add("group=${it.name}") }
            if (currentPage != 1) add("page=$currentPage")
            if (pageSize != 100) add("pageSize=$pageSize")
        }
        val query = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
        ctx.router.navigateTo("${ctx.route.path}$query", updateHistoryMode = UpdateHistoryMode.REPLACE)
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.px),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
            Row(
                ref = ref { element -> chipsRowElement = element },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(CategoryScrollRowStyle.toModifier())
                    .cursor(if (isDragging) Cursor.Grabbing else Cursor.Grab)
                    .onMouseDown { event ->
                        isDragging = true
                        dragDistance = 0.0
                        dragStartX = event.clientX.toDouble()
                        dragStartScrollLeft = chipsRowElement?.scrollLeft ?: 0.0
                    }
                    .onMouseMove { event ->
                        if (isDragging) {
                            val delta = event.clientX.toDouble() - dragStartX
                            dragDistance = abs(delta)
                            chipsRowElement?.scrollLeft = dragStartScrollLeft - delta
                        }
                    }
                    .onMouseUp {
                        if (isDragging && dragDistance > 4.0) {
                            suppressNextClick = true
                        }
                        isDragging = false
                    }
                    .onMouseLeave { isDragging = false },
                horizontalArrangement = Arrangement.spacedBy(8.px),
            ) {
                CategoryChip(
                    label = "Todos",
                    selected = selectedGroup == null,
                    onClick = {
                        onCategoryClick {
                            selectedGroup = null
                            currentPage = 1
                        }
                    },
                )
                SatGroup.entries.sortedBy { it.displayName }.forEach { group ->
                    CategoryChip(
                        label = group.displayName,
                        selected = selectedGroup == group,
                        onClick = {
                            onCategoryClick {
                                selectedGroup = group
                                currentPage = 1
                            }
                        },
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .minHeight(0.px)
        ) {

            when(val s = satelliteListState) {
                is SatelliteListState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.px),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SpanText(
                                "${s.data.totalItems} satélites encontrados",
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .minHeight(0.px)
                        ) {
                            SatelliteListComponent(
                                modifier = Modifier
                                    .fillMaxSize(),
                                satelliteList = s.data.content
                            )
                        }
                    }
                }
                is SatelliteListState.Loading -> {
                    SpanText("Cargando satélites...")
                }
                is SatelliteListState.Error -> {
                    SpanText("Error: ${s.message}")
                }
                is SatelliteListState.Idle -> {
                    SpanText("Sin datos")
                }
            }
        }
    }
}

val CategoryScrollRowStyle = CssStyle {
    base {
        Modifier.styleModifier {
            property("overflow-x", "auto")
            property("overflow-y", "hidden")
            property("scrollbar-width", "none")
            property("-ms-overflow-style", "none")
            property("user-select", "none")
        }
    }
    cssRule("::-webkit-scrollbar") {
        Modifier.styleModifier {
            property("display", "none")
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .flexShrink(0)
            .padding(topBottom = 6.px, leftRight = 10.px)
            .borderRadius(8.px)
            .cursor(Cursor.Pointer)
            .backgroundColor(
                if (selected) AppColors.PrimaryBlue.copyf(alpha = 0.18f) else AppColors.DarkBluePrimary
            )
            .border(
                1.px,
                LineStyle.Solid,
                if (selected) AppColors.PrimaryBlue.copyf(alpha = 0.5f) else AppColors.OutlineGray
            )
            .onClick { onClick() }
    ) {
        SpanText(
            label,
            modifier = Modifier
                .fontSize(11.px)
                .fontWeight(700)
                .whiteSpace(WhiteSpace.NoWrap)
                .color(if (selected) AppColors.PrimaryBlue.lightened(0.3f) else Colors.LightGray)
        )
    }
}
