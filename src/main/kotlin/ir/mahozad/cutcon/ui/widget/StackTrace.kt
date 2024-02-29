package ir.mahozad.cutcon.ui.widget

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import ir.mahozad.cutcon.defaultFontSize

@Composable
fun StackTrace(throwable: Throwable?) {
    // Shows the error always in LTR direction
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        val horizontalScrollState = rememberScrollState()
        val verticalScrollState = rememberScrollState()
        Box {
            Text(
                // FIXME: Tab characters in stacktrace are rendered as unknown (square) glyph
                //  See https://github.com/JetBrains/compose-multiplatform/issues/2626
                text = throwable?.stackTraceToString()?.replace("\t", "    ") ?: "",
                softWrap = false,
                overflow = TextOverflow.Visible,
                fontSize = defaultFontSize,
                color = MaterialTheme.colors.error,
                modifier = Modifier
                    .horizontalScroll(horizontalScrollState)
                    .verticalScroll(verticalScrollState)
            )
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState = horizontalScrollState),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState = verticalScrollState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
            )
        }
    }
}
