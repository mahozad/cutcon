package ir.mahozad.cutcon.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.Shortcut
import ir.mahozad.cutcon.model.Speed
import ir.mahozad.cutcon.ui.icon.*

@Preview
@Composable
private fun SpeedInputPreview() {
    SpeedInput(speed = Speed.NORMAL, onReset = {}, onChange = {})
}

/**
 * For alternative designs see the comments of this function in git tag v1.10.0
 */
@Composable
fun SpeedInput(
    speed: Speed,
    onReset: () -> Unit,
    onChange: (Speed) -> Unit
) {
    val language = LocalLanguage.current
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Tooltip(
            if (speed == Speed.NORMAL) {
                language.messages.restoreLastPlaybackSpeed
            } else {
                language.messages.resetPlaybackSpeedToNormal
            },
            Shortcut.SPEED_RESET.symbol
        ) {
            IconButton(onClick = onReset) {
                CustomIcon(
                    icon = if (speed.value < 1f) {
                        Icons.Custom.SpeedSlow
                    } else if (speed.value > 1f) {
                        Icons.Custom.SpeedFast
                    } else {
                        Icons.Custom.SpeedNormal
                    },
                    description = Messages.ICO_DSC_RESET_SPEED
                )
            }
        }
        Tooltip(
            language.messages.decreasePlaybackSpeed,
            Shortcut.SPEED_DECREASE.symbol
        ) {
            IconButton(onClick = { speed.dec().let(onChange) }) {
                CustomIcon(
                    icon = Icons.Custom.Minus,
                    description = Messages.ICO_DSC_DECREASE_SPEED
                )
            }
        }
        Text(
            text = language.localizeNumber(speed.value),
            fontSize = defaultFontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
        Tooltip(
            language.messages.increasePlaybackSpeed,
            Shortcut.SPEED_INCREASE.symbol
        ) {
            IconButton(onClick = { speed.inc().let(onChange) }) {
                CustomIcon(
                    icon = Icons.Custom.Plus,
                    description = Messages.ICO_DSC_INCREASE_SPEED
                )
            }
        }
    }
}
