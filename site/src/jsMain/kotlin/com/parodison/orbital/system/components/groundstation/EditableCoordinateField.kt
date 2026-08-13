package com.parodison.orbital.system.components.groundstation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.parodison.orbital.system.components.dom.BasicInput
import com.parodison.orbital.system.core.roundTo
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.px

private fun formatCoordinateValue(value: Double, decimals: Int, suffix: String): String {
    val rounded = value.roundTo(decimals)
    val numberText = if (decimals == 0) rounded.toInt().toString() else rounded.toString()
    return "$numberText$suffix"
}

@Composable
fun EditableCoordinateField(
    label: String,
    value: Double,
    decimals: Int,
    suffix: String,
    onCommit: (Double) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    var text by remember(value, isFocused) {
        mutableStateOf(
            if (isFocused) value.toString() else formatCoordinateValue(value, decimals, suffix)
        )
    }

    fun commitIfValid() {
        text.toDoubleOrNull()?.let { parsed ->
            if (parsed != value) onCommit(parsed)
        }
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpanText(label, modifier = Modifier.fontSize(12.px).color(Colors.Gray))
        BasicInput(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.width(90.px),
            onFocus = {
                isFocused = true
                text = value.toString()
            },
            onBlur = {
                isFocused = false
                commitIfValid()
            },
            onEnter = {
                isFocused = false
                commitIfValid()
            },
        )
    }
}