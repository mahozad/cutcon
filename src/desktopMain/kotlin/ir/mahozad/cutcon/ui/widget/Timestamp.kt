package ir.mahozad.cutcon.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultDurationConverter
import ir.mahozad.cutcon.defaultFontSize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Preview
@Composable
private fun TimestampPreview() {
    Timestamp(
        time = 17.minutes + 44.seconds,
        textAlign = TextAlign.Start
    )
}

@Composable
fun Timestamp(
    time: Duration,
    textAlign: TextAlign
) = Text(
    text = LocalLanguage.current.localizeDigits(
        defaultDurationConverter
            .format(duration = time, numberOfParts = 2)
            .removePrefix("-") // Because sometimes the 0 duration has negative sign
    ),
    fontSize = defaultFontSize,
    textAlign = textAlign,
    // See the MediaPlayerProgress for why a fixed width is used
    modifier = Modifier.width(43.dp)
)
