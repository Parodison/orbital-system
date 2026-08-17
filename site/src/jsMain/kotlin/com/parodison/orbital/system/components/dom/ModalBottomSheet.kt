package com.parodison.orbital.system.components.dom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.thenIf
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.AnimationTimingFunction
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.abs

enum class SheetValue {
    Hidden,
    PartiallyExpanded,
    Expanded,
}

private const val FLING_VELOCITY_THRESHOLD_PX_PER_MS = 0.5

class SheetState internal constructor(
    initialValue: SheetValue,
    internal val skipPartiallyExpanded: Boolean,
    internal val peekHeightFraction: Float,
    internal val confirmValueChange: (SheetValue) -> Boolean,
) {
    var currentValue: SheetValue by mutableStateOf(initialValue)
        internal set

    var targetValue: SheetValue by mutableStateOf(initialValue)
        internal set

    val isVisible: Boolean
        get() = currentValue != SheetValue.Hidden

    internal var sheetHeightPx by mutableStateOf(0.0)
    internal var peekHeightPx by mutableStateOf(0.0)
    internal var dragOffsetPx by mutableStateOf<Double?>(null)
    internal var measured by mutableStateOf(false)

    private var pendingSettle: CompletableDeferred<Unit>? = null

    internal fun anchors(): List<Pair<Double, SheetValue>> {
        val anchors = mutableListOf(0.0 to SheetValue.Expanded)
        if (!skipPartiallyExpanded && peekHeightPx > 0.0 && peekHeightPx < sheetHeightPx) {
            anchors.add(offsetFor(SheetValue.PartiallyExpanded) to SheetValue.PartiallyExpanded)
        }
        anchors.add(sheetHeightPx to SheetValue.Hidden)
        return anchors.sortedBy { it.first }
    }

    internal fun offsetFor(value: SheetValue): Double = when (value) {
        SheetValue.Expanded -> 0.0
        SheetValue.PartiallyExpanded -> (sheetHeightPx - peekHeightPx).coerceAtLeast(0.0)
        SheetValue.Hidden -> sheetHeightPx
    }

    internal fun notifySettled() {
        currentValue = targetValue
        pendingSettle?.complete(Unit)
        pendingSettle = null
    }

    internal suspend fun animateTo(value: SheetValue) {
        if (!confirmValueChange(value)) return
        if (currentValue == value && targetValue == value && dragOffsetPx == null) return
        val deferred = CompletableDeferred<Unit>()
        pendingSettle = deferred
        targetValue = value
        deferred.await()
    }

    suspend fun show() = animateTo(
        if (skipPartiallyExpanded || peekHeightPx <= 0.0 || peekHeightPx >= sheetHeightPx) {
            SheetValue.Expanded
        } else {
            SheetValue.PartiallyExpanded
        }
    )

    suspend fun hide() = animateTo(SheetValue.Hidden)
    suspend fun expand() = animateTo(SheetValue.Expanded)
    suspend fun partialExpand() = animateTo(
        if (skipPartiallyExpanded) SheetValue.Expanded else SheetValue.PartiallyExpanded
    )
}

@Composable
fun rememberModalBottomSheetState(
    skipPartiallyExpanded: Boolean = false,
    peekHeightFraction: Float = 0.5f,
    confirmValueChange: (SheetValue) -> Boolean = { true },
): SheetState = remember {
    SheetState(
        initialValue = SheetValue.Hidden,
        skipPartiallyExpanded = skipPartiallyExpanded,
        peekHeightFraction = peekHeightFraction,
        confirmValueChange = confirmValueChange,
    )
}

object BottomSheetDefaults {
    val ContainerColor: Color = AppColors.DarkBluePrimary
    val ContentColor: Color = Colors.White
    val ScrimColor: Color = Color.rgba(0, 0, 0, 0.32f)
    val ShapeCornerRadius: CSSSizeValue<CSSUnit.px> = 28.px
    val SheetMaxWidth: CSSSizeValue<CSSUnit.px> = 640.px

    @Composable
    fun DragHandle(
        modifier: Modifier = Modifier,
        width: CSSSizeValue<CSSUnit.px> = 32.px,
        height: CSSSizeValue<CSSUnit.px> = 4.px,
        color: Color = Color.rgba(255, 255, 255, 0.32f),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(topBottom = 12.px).then(modifier),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .borderRadius(50.percent)
                    .backgroundColor(color)
            )
        }
    }
}

@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetMaxWidth: CSSSizeValue<CSSUnit.px>? = BottomSheetDefaults.SheetMaxWidth,
    shapeCornerRadius: CSSSizeValue<CSSUnit.px> = BottomSheetDefaults.ShapeCornerRadius,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = BottomSheetDefaults.ContentColor,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: (@Composable () -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    fun requestDismiss() {
        coroutineScope.launch {
            sheetState.hide()
            onDismissRequest()
        }
    }

    DisposableEffect(Unit) {
        val previousBodyOverflow = document.body?.style?.getPropertyValue("overflow") ?: ""
        document.body?.style?.setProperty("overflow", "hidden")
        val keyListener: (Event) -> Unit = { e ->
            if (e.asDynamic().key == "Escape") requestDismiss()
        }
        window.addEventListener("keydown", keyListener)
        onDispose {
            document.body?.style?.setProperty("overflow", previousBodyOverflow)
            window.removeEventListener("keydown", keyListener)
        }
    }

    // Raw drag-session bookkeeping, deliberately kept out of Compose state: it only
    // needs to survive between pointer callbacks on the same gesture, never triggers
    // recomposition on its own, and is reset on every pointerdown.
    var dragStartClientY = 0.0
    var dragStartOffsetPx = 0.0
    var lastMoveClientY = 0.0
    var lastMoveTimestamp = 0.0
    var velocityPxPerMs = 0.0

    fun endDrag() {
        val offset = sheetState.dragOffsetPx ?: return
        sheetState.dragOffsetPx = null
        val anchors = sheetState.anchors()
        val target = when {
            velocityPxPerMs > FLING_VELOCITY_THRESHOLD_PX_PER_MS ->
                anchors.firstOrNull { it.first > offset + 1.0 }?.second ?: anchors.last().second

            velocityPxPerMs < -FLING_VELOCITY_THRESHOLD_PX_PER_MS ->
                anchors.lastOrNull { it.first < offset - 1.0 }?.second ?: anchors.first().second

            else -> anchors.minByOrNull { abs(it.first - offset) }!!.second
        }
        coroutineScope.launch {
            sheetState.animateTo(target)
            if (target == SheetValue.Hidden) onDismissRequest()
        }
    }

    val currentOffsetPx = sheetState.dragOffsetPx ?: sheetState.offsetFor(sheetState.targetValue)
    val isDragging = sheetState.dragOffsetPx != null
    val scrimProgress = if (sheetState.sheetHeightPx > 0.0) {
        (1.0 - (currentOffsetPx / sheetState.sheetHeightPx)).coerceIn(0.0, 1.0)
    } else {
        0.0
    }

    Box(
        modifier = Modifier
            .position(Position.Fixed)
            .top(0.px).left(0.px).right(0.px).bottom(0.px)
            .styleModifier { property("z-index", "1000") },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .backgroundColor(scrimColor)
                .opacity(scrimProgress)
                .thenIf(!isDragging) {
                    Modifier.transition {
                        property("opacity")
                        duration(300.ms)
                        timingFunction(AnimationTimingFunction("cubic-bezier(0.4, 0, 0.2, 1)"))
                    }
                }
                .onClick { requestDismiss() }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .thenIf(sheetMaxWidth != null) { Modifier.maxWidth(sheetMaxWidth!!) }
                .maxHeight(90.percent)
                .backgroundColor(containerColor)
                .color(contentColor)
                .borderRadius(topLeft = shapeCornerRadius, topRight = shapeCornerRadius)
                .overflow(Overflow.Hidden)
                .position(Position.Relative)
                .opacity(if (sheetState.measured) 1 else 0)
                .thenIf(!isDragging) {
                    Modifier.transition {
                        property("translate")
                        duration(300.ms)
                        timingFunction(AnimationTimingFunction("cubic-bezier(0.4, 0, 0.2, 1)"))
                    }
                }
                .translateY(currentOffsetPx.px)
                .onTransitionEnd { e ->
                    if (e.propertyName == "translate" && sheetState.dragOffsetPx == null) {
                        sheetState.notifySettled()
                    }
                }
                .attrsModifier {
                    ref { el ->
                        fun measure() {
                            sheetState.sheetHeightPx = el.getBoundingClientRect().height
                            sheetState.peekHeightPx = window.innerHeight * sheetState.peekHeightFraction.toDouble()
                            sheetState.measured = true
                        }
                        window.requestAnimationFrame {
                            measure()
                            coroutineScope.launch { sheetState.show() }
                        }
                        val resizeListener: (Event) -> Unit = { measure() }
                        window.addEventListener("resize", resizeListener)
                        onDispose {
                            sheetState.measured = false
                            window.removeEventListener("resize", resizeListener)
                        }
                    }
                }
                .then(modifier),
        ) {
            dragHandle?.let { handle ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cursor(Cursor.Grab)
                        .attrsModifier {
                            addEventListener("pointerdown") { evt ->
                                val native = evt.nativeEvent.asDynamic()
                                evt.target.unsafeCast<HTMLElement>().asDynamic().setPointerCapture(native.pointerId)
                                dragStartClientY = native.clientY as Double
                                dragStartOffsetPx = currentOffsetPx
                                sheetState.dragOffsetPx = dragStartOffsetPx
                                lastMoveClientY = dragStartClientY
                                lastMoveTimestamp = native.timeStamp as Double
                                velocityPxPerMs = 0.0
                            }
                            addEventListener("pointermove") { evt ->
                                if (sheetState.dragOffsetPx == null) return@addEventListener
                                val native = evt.nativeEvent.asDynamic()
                                val clientY = native.clientY as Double
                                val newOffset = (dragStartOffsetPx + (clientY - dragStartClientY))
                                    .coerceIn(0.0, sheetState.sheetHeightPx)
                                sheetState.dragOffsetPx = newOffset
                                val now = native.timeStamp as Double
                                val dt = now - lastMoveTimestamp
                                if (dt > 0) velocityPxPerMs = (clientY - lastMoveClientY) / dt
                                lastMoveClientY = clientY
                                lastMoveTimestamp = now
                            }
                            addEventListener("pointerup") { endDrag() }
                            addEventListener("pointercancel") { endDrag() }
                        },
                ) { handle() }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .minHeight(0.px)
                    .fillMaxWidth()
                    .overflow(overflowX = Overflow.Hidden, overflowY = Overflow.Auto)
            ) {
                Column(Modifier.fillMaxWidth()) { content() }
            }
        }
    }
}
