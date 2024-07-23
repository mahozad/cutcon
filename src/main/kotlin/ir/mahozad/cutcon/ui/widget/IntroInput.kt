package ir.mahozad.cutcon.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.defaultIconSize
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.*
import ir.mahozad.cutcon.ui.dialog.showOpenFileDialog
import ir.mahozad.cutcon.ui.icon.Curtain
import ir.mahozad.cutcon.ui.icon.Delete
import ir.mahozad.cutcon.ui.icon.Icons
import ir.mahozad.cutcon.ui.theme.BorderColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.toPath
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

const val INTRO_PREVIEW_SIZE = 66f

private val height = 92.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.IntroInput(
    isEnabled: Boolean,
    image: ImageBitmap?,
    options: IntroOptions,
    source: Source,
    targetFormat: Format,
    lastOpenDirectory: Path?,
    modifier: Modifier,
    onFileChange: (Path?) -> Unit,
    onDurationChange: (Duration) -> Unit,
    onBackgroundColorChange: (Color) -> Unit,
) {
    val language = LocalLanguage.current
    if (
        (targetFormat == Format.MP3) ||
        (targetFormat == Format.RAW) ||
        (targetFormat == Format.MP4 && source.mediaType != Source.MediaType.VIDEO)
    ) {
        PromptBox(
            isEnabled = isEnabled,
            modifier = modifier,
            text = if (targetFormat == Format.MP3) {
                language.messages.txtLblMp3IntroNotSupported
            } else if (targetFormat == Format.RAW) {
                language.messages.txtLblRawIntroNotSupported
            } else if (source.mediaType == Source.MediaType.AUDIO) {
                language.messages.txtLblAudioFileIntroNotSupported
            } else if (source.mediaType == Source.MediaType.IMAGE) {
                language.messages.txtLblImageFileIntroNotSupported
            } else {
                language.messages.txtLblMiscFileIntroNotSupported
            }
        )
    } else {
        var isDragging by remember { mutableStateOf(false) }
        val primaryColor = MaterialTheme.colors.primary
        val primaryVariantColor = MaterialTheme.colors.primaryVariant
        Box(modifier = modifier
            // Uses drawBehind because of the need for dashed stroke
            .drawBehind {
                if (isEnabled && (image == null || isDragging)) {
                    drawBorder(isEnabled, isDragging, 4.dp, primaryColor, primaryVariantColor)
                }
            }
            .clip(RoundedCornerShape(4.dp))
            .onExternalDrag(
                onDragStart = { isDragging = true },
                onDragExit = { isDragging = false },
                onDrop = { dragState ->
                    isDragging = false
                    if (isEnabled) onFileDrop(dragState, onFileChange)
                }
            )
        ) {
            if (image == null) {
                SelectBox(
                    isEnabled = isEnabled,
                    targetFormat = targetFormat,
                    onFileChange = onFileChange,
                    modifier = modifier,
                    lastOpenDirectory = lastOpenDirectory
                )
            } else {
                ConfigBox(
                    isEnabled = isEnabled,
                    image = image,
                    options = options,
                    modifier = modifier,
                    onDurationChange = onDurationChange,
                    onBackgroundColorChange = onBackgroundColorChange,
                    onRemoveRequest = { onFileChange(null) }
                )
            }
            if (isEnabled && isDragging) {
                Box(modifier = Modifier.fillMaxWidth().height(92.dp).background(primaryColor.copy(alpha = 0.2f)))
            }
        }
    }
}

private fun DrawScope.drawBorder(
    isEnabled: Boolean,
    isDragging: Boolean,
    cornerRadius: Dp,
    primaryColor: Color,
    primaryVariantColor: Color
) {
    val stroke = Stroke(
        width = if (isDragging) 2.dp.toPx() else 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
    )
    drawRoundRect(
        color = if (!isEnabled) {
            Color.LightGray
        } else if (isDragging) {
            primaryVariantColor
        } else {
            primaryColor
        },
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun onFileDrop(
    state: ExternalDragValue,
    onFileSelected: (Path?) -> Unit
) {
    (state.dragData as? DragData.FilesList)
        ?.readFiles()
        ?.first()
        ?.let(::URI)
        ?.let(URI::toPath)
        ?.let(onFileSelected)
}

@Composable
private fun PromptBox(isEnabled: Boolean, text: String, modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity))
    ) {
        Text(
            text = text,
            fontSize = (defaultFontSize.value - 1).sp,
            modifier = Modifier.padding(horizontal = 12.dp),
            color = if (isEnabled) {
                LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
            } else {
                LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
            }
        )
    }
}

@Composable
private fun FrameWindowScope.SelectBox(
    isEnabled: Boolean,
    targetFormat: Format,
    lastOpenDirectory: Path?,
    modifier: Modifier,
    onFileChange: (Path?) -> Unit
) {
    val language = LocalLanguage.current
    val scope = rememberCoroutineScope()
    var isDialogDisplayed by remember { mutableStateOf(false) }
    /**
     * See [SaveAsInput] for more information about this.
     */
    var dialogClosedTime by remember { mutableLongStateOf(0) }
    Surface(
        color = if (!isEnabled) {
            Color.LightGray.copy(ContentAlpha.disabled)
        } else {
            MaterialTheme.colors.primary.copy(alpha = 0.1f)
        },
        modifier = modifier
            .height(height)
            .clickable(enabled = isEnabled) {
                if (
                    isDialogDisplayed ||
                    System.currentTimeMillis() - dialogClosedTime < 300
                ) {
                    return@clickable
                }
                dialogClosedTime = System.currentTimeMillis()
                isDialogDisplayed = true
                scope.launch(Dispatchers.IO) {
                    window.showOpenFileDialog(
                        language = language,
                        title = language.messages.dlgTitSelectIntroImage,
                        startingDirectory = lastOpenDirectory,
                        approveButtonLabel = language.messages.btnLblApproveSelectedFile,
                        approveButtonTooltip = language.messages.btnTlpApproveSelectedFile,
                        fileExtensionDescription = language.messages.txtLblIntroSupportedTypesDescription,
                        fileExtensions = supportedImageFileExtensions
                    )
                        .let(onFileChange)
                    dialogClosedTime = System.currentTimeMillis()
                    isDialogDisplayed = false
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(start = 13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDialogDisplayed) {
                    Spinner(modifier = Modifier.size(defaultIconSize).padding(6.dp))
                } else {
                    Icon(
                        imageVector = Icons.Custom.Curtain,
                        contentDescription = Messages.ICO_DSC_ADD_INTRO,
                        modifier = Modifier.size(defaultIconSize),
                        tint = if (isEnabled) {
                            LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                        } else {
                            LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                        }
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = language.messages.txtLblSelectIntroImage,
                    fontSize = (defaultFontSize.value - 1).sp,
                    color = if (isEnabled) {
                        LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                    } else {
                        LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                    }
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = language.messages.txtLblDragFileHere,
                fontSize = (defaultFontSize.value - 3).sp,
                color = if (isEnabled) {
                    LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                } else {
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                }
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (targetFormat == Format.MP4) {
                    language.messages.txtLblHasNoDefault
                } else {
                    language.messages.txtLblHasDefault
                },
                fontSize = (defaultFontSize.value - 3).sp,
                color = if (isEnabled) {
                    LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                } else {
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                }
            )
        }
    }
}

@Composable
private fun ConfigBox(
    isEnabled: Boolean,
    image: ImageBitmap,
    options: IntroOptions,
    modifier: Modifier,
    onDurationChange: (Duration) -> Unit,
    onBackgroundColorChange: (Color) -> Unit,
    onRemoveRequest: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(height)
    ) {
        IntroImage(
            isEnabled = isEnabled,
            image = image,
            backgroundColor = options.backgroundColor,
            onRemoveRequest = onRemoveRequest
        )
        IntroConfig(
            isEnabled = isEnabled,
            options = options,
            onDurationChange = onDurationChange,
            onBackgroundColorChange = onBackgroundColorChange
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun IntroImage(
    isEnabled: Boolean,
    image: ImageBitmap,
    backgroundColor: Color,
    onRemoveRequest: () -> Unit
) {
    var isImageHovered by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(70.dp)
            .onPointerEvent(eventType = PointerEventType.Enter) { isImageHovered = true }
            .onPointerEvent(eventType = PointerEventType.Exit) { isImageHovered = false }
    ) {
        // See https://stackoverflow.com/questions/64742457/how-to-load-image-in-kotlin-compose-desktop
        Image(
            bitmap = image,
            contentDescription = Messages.ICO_DSC_INTRO_PREVIEW,
            colorFilter = if (!isEnabled) {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0F) })
            } else {
                null
            },
            modifier = Modifier
                .size(INTRO_PREVIEW_SIZE.dp)
                .border(Dp.Hairline, BorderColor())
                .blur(if (isImageHovered && isEnabled) 8.dp else 0.dp)
                .drawBehind { drawRect(color = backgroundColor) }
        )
        if (isImageHovered && isEnabled) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.6f))
                    .size(INTRO_PREVIEW_SIZE.dp)
                    .clickable {
                        onRemoveRequest()
                        isImageHovered = false
                    }
            ) {
                CustomIcon(
                    icon = Icons.Custom.Delete,
                    description = Messages.ICO_DSC_REMOVE_INTRO
                )
            }
        }
    }
}

@Composable
private fun IntroConfig(
    isEnabled: Boolean,
    options: IntroOptions,
    onDurationChange: (Duration) -> Unit,
    onBackgroundColorChange: (Color) -> Unit
) {
    Column(verticalArrangement = Arrangement.Center) {
        CustomTextField(isEnabled = isEnabled, value = options.duration.inWholeSeconds.toInt()) {
            onDurationChange(it.seconds)
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackgroundColor(
                isEnabled = isEnabled,
                color = Color.Black,
                isSelected = options.backgroundColor == Color.Black
            ) {
                onBackgroundColorChange(Color.Black)
            }
            Spacer(Modifier.width(8.dp))
            BackgroundColor(
                isEnabled = isEnabled,
                color = Color.White,
                isSelected = options.backgroundColor == Color.White,
            ) {
                onBackgroundColorChange(Color.White)
            }
        }
    }
}

@Composable
private fun BackgroundColor(
    isEnabled: Boolean,
    color: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .selectable(enabled = isEnabled, selected = isSelected, onClick = onSelect)
            .background(color)
            .size(16.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) {
                    MaterialTheme.colors.primary
                } else {
                    BorderColor()
                }
            )
    )
}

// FIXME: Duplicate of code in timestamp input to some extent
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CustomTextField(isEnabled: Boolean, value: Int, onValueChange: (Int) -> Unit) {
    val language = LocalLanguage.current
    val colors = TextFieldDefaults.textFieldColors()
    var input by remember { mutableStateOf(value.toString()) }
    val interactionSource = remember(::MutableInteractionSource)
    BasicTextField(
        value = language.localizeDigits(input),
        onValueChange = { newString ->
            input = (newString.takeIf { (it.toIntOrNull() ?: -1) >= 0 || it.isBlank() } ?: input).take(3)
            newString.toIntOrNull()?.takeIf { it > 0 }?.let(onValueChange)
        },
        cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.high)
        ),
        interactionSource = interactionSource,
        visualTransformation = SuffixTransformer1(" ${language.messages.second}"),
        enabled = isEnabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            // To modify the width, also, modify the content padding start and end values in the TextFieldDecoration below
            .width(44.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.backgroundColor(true).value)
            // Should consume (true) (except for TAB) to prevent bugs with app custom shortcuts
            // See https://github.com/JetBrains/compose-multiplatform/issues/1925
            .onKeyEvent { it.key != Key.Tab }
    ) {
        TextFieldDefaults.TextFieldDecorationBox(
            value = language.localizeDigits(input),
            innerTextField = it,
            singleLine = true,
            enabled = isEnabled,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            // Keeps horizontal paddings but changes the vertical
            contentPadding = TextFieldDefaults.textFieldWithoutLabelPadding(
                top = 0.dp,
                bottom = 0.dp,
                start = 0.dp,
                end = 0.dp
            )
        )
    }
}

private class SuffixTransformer1(private val suffix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val result = text + AnnotatedString(suffix)
        val textWithSuffixMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = offset
            override fun transformedToOriginal(offset: Int): Int {
                return if (text.isEmpty()) {
                    0
                } else if (offset < text.length) {
                    offset
                } else {
                    text.length
                }
            }
        }
        return TransformedText(result, textWithSuffixMapping)
    }
}
