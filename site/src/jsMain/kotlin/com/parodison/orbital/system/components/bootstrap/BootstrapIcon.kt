package com.parodison.orbital.system.components.bootstrap

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.dom.I

enum class BootstrapIconStyle {
    FILLED,
    OUTLINED;
}

private fun BootstrapIconStyle.toClassNameSuffix(): String {
    return when (this) {
        BootstrapIconStyle.FILLED -> "-fill"
        BootstrapIconStyle.OUTLINED -> ""
    }
}


@Composable
fun BootstrapIcon(
    name: String,
    modifier: Modifier = Modifier,
    style: BootstrapIconStyle = BootstrapIconStyle.OUTLINED,
) {
    val tokens = name.trim().split(" ").filter { it.isNotBlank() }.toMutableList()
    if (tokens.isNotEmpty()) {
        tokens[tokens.lastIndex] = tokens.last() + style.toClassNameSuffix()
    }
    I(
        attrs = modifier.toAttrs {
            classes(*tokens.toTypedArray())
        }
    )
}