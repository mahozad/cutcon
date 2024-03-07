package ir.mahozad.cutcon.ui.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.defaultTooltipDelay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tooltip(
    tooltip: String,
    vararg shortcuts: String,
    content: @Composable () -> Unit
) {
    TooltipArea(
        tooltip = { TooltipText(tooltip, *shortcuts) },
        content = content,
        delayMillis = defaultTooltipDelay.inWholeMilliseconds.toInt(),
        tooltipPlacement = TooltipPlacement.ComponentRect(
            alignment = Alignment.TopCenter,
            offset = DpOffset(0.dp, (-52).dp) /* OR DpOffset.Zero */
        )
    )
}

@Composable
private fun TooltipText(text: String, vararg shortcuts: String) {
    Surface(
        color = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .shadow(4.dp)
            .border(
                width = Dp.Hairline,
                color = Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LocalLanguage.current.layoutDirection) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = text,
                    fontSize = (defaultFontSize.value - 2).sp
                )
                if (shortcuts.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = LocalLanguage.current.messages.shortcut,
                            fontSize = (defaultFontSize.value - 2).sp
                        )
                        Spacer(Modifier.width(4.dp))
                        for ((i, shortcut) in shortcuts.withIndex()) {
                            Text(
                                text = shortcut,
                                color = MaterialTheme.colors.primary,
                                fontSize = (defaultFontSize.value).sp
                            )
                            if (i < shortcuts.lastIndex) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = LocalLanguage.current.messages.and,
                                    fontSize = (defaultFontSize.value - 2).sp
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
