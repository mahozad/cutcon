package ir.mahozad.cutcon.ui.widget

import androidx.compose.animation.AnimatedContent
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultChipHeight
import ir.mahozad.cutcon.defaultDurationConverter
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.model.Clip
import ir.mahozad.cutcon.ui.theme.borderColor
import kotlin.time.Duration.Companion.seconds

@Preview
@Composable
private fun ClipLengthPreview() {
    ClipLength(Clip(78.seconds, 139.seconds))
}

@Composable
fun ClipLength(clip: Clip) {
    val language = LocalLanguage.current
    CompositionLocalProvider(LocalLayoutDirection provides language.layoutDirection) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(152.dp)
                .height(defaultChipHeight)
                .border(1.dp, borderColor, RoundedCornerShape(percent = 50))
        ) {
            Text(text = language.messages.txtLblClipLength, fontSize = defaultFontSize)
            Spacer(Modifier.width(4.dp))
            // See https://semicolonspace.com/jetpack-compose-animate-content-changes/
            AnimatedContent(targetState = clip) {
                Text(
                    text = "${/* For minus sign in a negative duration in RTL layouts to stay on the left side */"\u202D"}${
                        language.localizeDigits(defaultDurationConverter.format(it.duration, numberOfParts = 2))
                    }",
                    fontSize = defaultFontSize
                )
            }
        }
    }
}
