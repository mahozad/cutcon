package ir.mahozad.cutcon.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.*
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.ui.icon.CameraFill
import ir.mahozad.cutcon.ui.icon.CameraOutline
import ir.mahozad.cutcon.ui.icon.Icons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Desktop
import kotlin.io.path.createDirectories

@Preview
@Composable
private fun ScreenshotButtonPreview() {
    ScreenshotButton(isEnabled = true, isActive = false, onClick = {})
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenshotButton(
    isEnabled: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val language = LocalLanguage.current
    val scope = rememberCoroutineScope()
    TooltipArea(
        tooltip = {
            CompositionLocalProvider(LocalLayoutDirection provides language.layoutDirection) {
                Surface(
                    modifier = Modifier
                        .shadow(4.dp)
                        .width(if (language is LanguageFa) 214.dp else 196.dp)
                        .border(
                            width = Dp.Hairline,
                            color = Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = language.messages.takeScreenshotAndSaveIn,
                            fontSize = (defaultFontSize.value - 2).sp
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = defaultScreenshotSaveDirectory.trim(maxLength = 70).toLtrString(),
                                color = MaterialTheme.colors.primary,
                                fontSize = (defaultFontSize.value - 2).sp
                            )
                        }
                        if (isEnabled) {
                            Text(
                                text = language.messages.txtDscScreenshotHelp,
                                fontSize = (defaultFontSize.value - 2).sp
                            )
                        }
                    }
                }
            }
        },
        content = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        enabled = isEnabled,
                        indication = rememberRipple(bounded = false),
                        interactionSource = remember(::MutableInteractionSource),
                        onClick = onClick,
                        onLongClick = {
                            // Does it asynchronously because creating directories takes a little time
                            scope.launch(Dispatchers.IO) {
                                // Makes sure the directories exist.
                                // Calling this method where the variable is defined is not enough
                                // because, although it creates the directories on app start,
                                // user may have deleted the directories while the app is running
                                defaultScreenshotSaveDirectory.createDirectories()
                                Desktop.getDesktop().open(defaultScreenshotSaveDirectory.toFile())
                            }
                        }
                    )
            ) {
                CustomIcon(
                    icon = if (isActive) Icons.Custom.CameraFill else Icons.Custom.CameraOutline,
                    description = Messages.ICO_DSC_TAKE_SCREENSHOT,
                    tint = if (isEnabled) {
                        null
                    } else {
                        LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                    }
                )
            }
        },
        delayMillis = defaultTooltipDelay.inWholeMilliseconds.toInt(),
        tooltipPlacement = TooltipPlacement.ComponentRect(
            alignment = Alignment.TopCenter,
            offset = DpOffset(0.dp, (-52).dp) /* OR DpOffset.Zero */
        )
    )
}
