package com.parodison.orbital.system.components.tracking

import androidx.compose.runtime.Composable
import com.huanshankeji.compose.html.material3.MdIconButton
import com.huanshankeji.compose.html.material3.MdSlider
import com.parodison.orbital.system.components.LiveContainer
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.functions.blur
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backdropFilter
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.mdi.IconStyle
import com.varabyte.kobweb.silk.components.icons.mdi.MdiCalendarToday
import com.varabyte.kobweb.silk.components.icons.mdi.MdiPause
import com.varabyte.kobweb.silk.components.icons.mdi.MdiPlayArrow
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.document
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.w3c.dom.HTMLInputElement
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun BoxScope.TrackingTimeline(
    modifier: Modifier = Modifier,
    min: Number,
    max: Number,
    step: Number,
    value: Number,
    live: Boolean,
    paused: Boolean,
    onPausedChange: () -> Unit,
    onLiveClicked: () -> Unit,
    onTimelineValueChange: (Instant) -> Unit,
    onCustomDateSelected: (Instant) -> Unit,
) {
    val dayOfInstant = Instant.fromEpochMilliseconds(value.toLong()).toLocalDateTime(TimeZone.currentSystemDefault())
    val actualDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val isDifferentDay = dayOfInstant.date != actualDate.date

    Box(
        Modifier
            .fillMaxWidth()
            .padding(topBottom = 10.px, leftRight = 10.px)
            .border(2.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .overflow(Overflow.Hidden)
            .backgroundColor(AppColors.DarkBlue.copyf(alpha = 0.4f))
            .backdropFilter(blur(10.px))
            .styleModifier {
                property("-webkit-backdrop-filter", "blur(10px)")
            }
            .then(modifier)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.px),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MdIconButton(
                    attrs = Modifier
                        .onClick { onPausedChange() }
                        .toAttrs()
                ) {
                    if (paused) {
                        MdiPlayArrow(
                            modifier = Modifier
                                .color(Colors.Gray),
                            style = IconStyle.FILLED
                        )
                    } else {
                        MdiPause(
                            modifier = Modifier
                                .color(Colors.Gray),
                            style = IconStyle.FILLED
                        )
                    }
                }
                LiveContainer(
                    live = live,
                    onLiveClicked = onLiveClicked
                )
            }
            MdSlider(
                attrs = MdSliderStyle.toModifier()
                    .fillMaxWidth()
                    .weight(1)
                    .toAttrs {
                        addEventListener("input") { e ->
                            val target = e.target.asDynamic()
                            val value = target.value as Number
                            onTimelineValueChange(Instant.fromEpochMilliseconds(value.toLong()))
                        }
                },
                min = min,
                max = max,
                step = step,
                value = value,
                ticks = true
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.px)
            ) {
                val hour = dayOfInstant.hour.toString().padStart(2, '0')
                val minute = dayOfInstant.minute.toString().padStart(2, '0')
                SpanText(
                    text = "$hour:$minute",
                    modifier = Modifier
                        .width(60.px)
                        .textAlign(TextAlign.Center)
                        .color(Colors.Gray)
                )
                MdIconButton(
                    attrs = Modifier.toAttrs {
                        onClick {
                            // 1. Crear el input en memoria
                            val input = document.createElement("input") as HTMLInputElement
                            input.type = "datetime-local"
                            input.style.display = "none"

                            // 2. Escuchar cuando el usuario seleccione una fecha
                            input.addEventListener("change", {
                                val dateString = input.value
                                if (dateString.isNotEmpty()) {
                                    val epochMillis = kotlin.js.Date.parse(dateString)
                                    if (!epochMillis.isNaN()) {
                                        val selectedInstant = Instant.fromEpochMilliseconds(epochMillis.toLong())
                                        onCustomDateSelected(selectedInstant)
                                    }
                                }
                                // Limpiar el DOM al terminar
                                document.body?.removeChild(input)
                            })

                            // 3. Adjuntar al DOM temporalmente para que el navegador permita abrirlo
                            document.body?.appendChild(input)

                            // 4. Abrir el picker nativo (showPicker es el estándar moderno)
                            try {
                                input.asDynamic().showPicker()
                            } catch (e: Throwable) {
                                input.click() // Fallback para navegadores antiguos
                            }
                        }
                    }
                ) {
                    MdiCalendarToday(
                        modifier = Modifier.color(Colors.LightGray),
                        style = IconStyle.FILLED
                    )
                }            }
        }
    }
}

val MdSliderStyle = CssStyle {
    base {
        Modifier
            .styleModifier {
            // Estado base
            property("--md-slider-active-track-color", AppColors.PrimaryBlue.toString())
            property("--md-slider-handle-color", AppColors.PrimaryBlue.toString())

            // Forzar el mismo color en Hover, Focus y Press/Dragging
            property("--md-slider-hover-active-track-color", AppColors.PrimaryBlue.toString())
            property("--md-slider-hover-handle-color", AppColors.PrimaryBlue.toString())
            property("--md-slider-focus-handle-color", AppColors.PrimaryBlue.toString())
            property("--md-slider-pressed-handle-color", AppColors.PrimaryBlue.toString())
        }
    }
}