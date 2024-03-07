package ir.mahozad.cutcon.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.defaultQuality
import ir.mahozad.cutcon.model.Quality

@Preview
@Composable
private fun QualityInputPreview() {
    QualityInput(
        isApplicable = true,
        isEnabled = true,
        quality = defaultQuality,
        min = 1,
        max = 3,
        onChange = {}
    )
}

@Composable
fun QualityInput(
    isApplicable: Boolean,
    isEnabled: Boolean,
    quality: Quality,
    min: Int,
    max: Int,
    onChange: (Float) -> Unit
) {
    if (isApplicable) {
        // See the MediaPlayerProgress widget
        var isSeeking by remember { mutableStateOf(false) }
        var seek by remember { mutableFloatStateOf(0f) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(8.dp))
            // Makes it always LTR
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Slider(
                    value = if (isSeeking) seek else quality.value.toFloat(),
                    onValueChange = {
                        isSeeking = true
                        seek = it
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        onChange(seek)
                    },
                    valueRange = min.toFloat()..max.toFloat(),
                    steps = max - min - 1,
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = quality.label(LocalLanguage.current),
                    fontSize = defaultFontSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(48.dp),
                    color = if (isEnabled) {
                        LocalContentColor.current
                    } else {
                        LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                    }
                )
            }
        }
    } else {
        Row {
            Spacer(Modifier.width(12.dp))
            Text(
                text = LocalLanguage.current.messages.qualityNotApplicableToRawFormat,
                fontSize = defaultFontSize,
                modifier = Modifier
                    .height(48.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
        }
    }
}
