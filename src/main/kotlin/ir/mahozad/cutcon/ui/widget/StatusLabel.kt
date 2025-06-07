package ir.mahozad.cutcon.ui.widget

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.model.Source
import ir.mahozad.cutcon.model.Status
import ir.mahozad.cutcon.model.Status.*
import ir.mahozad.cutcon.ui.theme.BorderColor
import kotlin.math.roundToInt

@Composable
fun StatusLabel(status: Status) {
    val language = LocalLanguage.current
    val primaryColor = MaterialTheme.colors.primary
    val primaryVariantColor = MaterialTheme.colors.primaryVariant
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(percent = 50),
                color = when (status) {
                    is Error -> MaterialTheme.colors.error
                    is Finished.Failure -> MaterialTheme.colors.error
                    is Finished.Success -> MaterialTheme.colors.success
                    else -> BorderColor()
                }
            )
            .padding(all = defaultChipHeight / 4)
            .height(defaultChipHeight / 2)
            // See https://stackoverflow.com/q/75941502
            .drawWithContent {
                with(drawContext.canvas.nativeCanvas) {
                    val checkPoint = saveLayer(null, null)
                    // Destination
                    drawContent()
                    // Source
                    if (status is Initializing || status is InProgress) {
                        drawRoundRect(
                            size = Size(size.width * ((status as? InProgress)?.progress ?: 0f), size.height),
                            brush = Brush.horizontalGradient(
                                0f to primaryVariantColor,
                                size.width to primaryColor,
                            ),
                            blendMode = BlendMode.SrcOut,
                            cornerRadius = CornerRadius(100f, 100f)
                        )
                    }
                    restoreToCount(checkPoint)
                }
            }
    ) {
        if (status is InProgress) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(y = if (language is LanguageFa) 1.dp else 0.dp)
            ) {
                Text(
                    text = language.localizeDigits((status.progress * 100).roundToInt().toString()),
                    fontSize = (defaultFontSize.value - 1).sp,
                    // Uses end alignment and fixed width so the progress description
                    // stays at a fixed position for all percentage labels
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(if ((status.progress * 100).roundToInt() < 100) 18.dp else 26.dp)
                )
                Text(
                    text = language.messages.txtLblPercentSign,
                    fontSize = if (language is LanguageFa) defaultFontSize else (defaultFontSize.value - 1).sp,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${language.messages.txtLblProgressCreating} ",
                    fontSize = (defaultFontSize.value - 1).sp
                )
                SourceText(status.source)
            }
        } else {
            @Suppress("KotlinConstantConditions")
            Text(
                text = when (status) {
                    is Ready -> language.messages.txtLblReady
                    is Initializing -> language.messages.txtLblProgressInitializing
                    is Error.ClipFromImageNotSupported -> language.messages.txtLblErrorClipFromImageNotSupported
                    is Error.ClipFromFormatNotSupported -> language.messages.txtLblErrorClipFromFormatNotSupported
                    is Error.FileNotSet -> language.messages.txtLblErrorClipFileNotSet
                    is Error.ClipNotSet -> language.messages.txtLblErrorClipNotSet
                    is Error.ClipLengthZero -> language.messages.txtLblErrorClipLengthZero
                    is Error.ClipLengthNegative -> language.messages.txtLblErrorClipLengthNegative
                    is Error.ClipStartAfterMediaEnd -> language.messages.txtLblErrorClipStartAfterMediaEnd
                    is InProgress -> TODO("NOT REACHABLE; DOES NOT MATTER")
                    is Finished.Failure -> language.messages.txtLblClipCreationFailure
                    is Finished.Success -> language.messages.txtLblClipCreationSuccess
                },
                color = when (status) {
                    is Error -> MaterialTheme.colors.error
                    is Finished.Failure -> MaterialTheme.colors.error
                    is Finished.Success -> MaterialTheme.colors.success
                    else -> Color.Unspecified
                },
                fontSize = (defaultFontSize.value - 1).sp,
                modifier = Modifier.offset(y = if (language is LanguageFa) 1.dp else 0.dp)
            )
        }
    }
}

@Composable
fun SourceText(source: Source) {
    val language = LocalLanguage.current
    when (source) {
        is Source.Local -> {
            Text(
                text = "${language.messages.fromFile} ${source.path.fileName.trim(maxLength = 24).toLtrString()}",
                fontSize = (defaultFontSize.value - 1).sp
            )
        }
    }
}
