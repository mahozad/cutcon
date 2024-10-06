package ir.mahozad.cutcon.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.defaultIconSize
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.*
import ir.mahozad.cutcon.ui.dialog.showOpenFileDialog
import ir.mahozad.cutcon.ui.icon.Cover
import ir.mahozad.cutcon.ui.icon.Delete
import ir.mahozad.cutcon.ui.icon.Icons
import ir.mahozad.cutcon.ui.theme.borderColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.toPath
import kotlin.math.roundToInt

const val COVER_PREVIEW_SIZE = 66f

private val height = 92.dp

/**
 * Input for video watermark or audio album art.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.CoverInput(
    isEnabled: Boolean,
    preview: ImageBitmap?,
    options: CoverOptions,
    source: Source,
    targetFormat: Format,
    lastOpenDirectory: Path?,
    modifier: Modifier,
    onFileChange: (Path?) -> Unit,
    onScaleChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onPositionChange: (WatermarkPosition) -> Unit
) {
    val language = LocalLanguage.current
    if (
        (targetFormat == Format.RAW) ||
        (targetFormat == Format.MP4 && source.mediaType != Source.MediaType.VIDEO) ||
        (targetFormat == Format.MP3 && source.mediaType !in setOf(Source.MediaType.AUDIO, Source.MediaType.VIDEO))
    ) {
        PromptBox(
            isEnabled = isEnabled,
            modifier = modifier,
            text = if (targetFormat == Format.RAW) {
                language.messages.txtLblRawCoverNotSupported
            } else if (targetFormat == Format.MP3) {
                language.messages.txtLblInputAlbumArtNotSupported
            } else if (source.mediaType == Source.MediaType.AUDIO) {
                language.messages.txtLblAudioFileWatermarkNotSupported
            } else if (source.mediaType == Source.MediaType.IMAGE) {
                language.messages.txtLblImageFileWatermarkNotSupported
            } else {
                language.messages.txtLblMiscFileWatermarkNotSupported
            }
        )
    } else {
        var isDragging by remember { mutableStateOf(false) }
        val primaryColor = MaterialTheme.colors.primary
        val primaryVariantColor = MaterialTheme.colors.primaryVariant
        Box(modifier = modifier
            // Uses drawBehind because of the need for dashed stroke
            .drawBehind {
                if (isEnabled && (preview == null || isDragging)) {
                    drawBorder(isDragging, 4.dp, primaryColor, primaryVariantColor)
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
            if (preview == null) {
                SelectBox(
                    isEnabled = isEnabled,
                    targetFormat = targetFormat,
                    onFileChange = onFileChange,
                    modifier = Modifier.fillMaxWidth(),
                    lastOpenDirectory = lastOpenDirectory
                )
            } else {
                ConfigBox(
                    isEnabled = isEnabled,
                    preview = preview,
                    options = options,
                    modifier = modifier,
                    targetFormat = targetFormat,
                    onScaleChange = onScaleChange,
                    onOpacityChange = onOpacityChange,
                    onPositionChange = onPositionChange,
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
        color = if (isDragging) {
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
                        title = if (targetFormat == Format.MP4) {
                            language.messages.dlgTitSelectWatermark
                        } else {
                            language.messages.dlgTitSelectAlbumArt
                        },
                        startingDirectory = lastOpenDirectory,
                        approveButtonLabel = language.messages.btnLblApproveSelectedFile,
                        approveButtonTooltip = language.messages.btnTlpApproveSelectedFile,
                        fileExtensionDescription = language.messages.txtLblCoverSupportedTypesDescription,
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
                        imageVector = Icons.Custom.Cover,
                        contentDescription = Messages.ICO_DSC_ADD_COVER,
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
                    text = if (targetFormat == Format.MP4) {
                        language.messages.txtLblSelectWatermark
                    } else {
                        language.messages.txtLblSelectAlbumArt
                    },
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
    preview: ImageBitmap,
    options: CoverOptions,
    targetFormat: Format,
    modifier: Modifier,
    onScaleChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onPositionChange: (WatermarkPosition) -> Unit,
    onRemoveRequest: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(height)
    ) {
        CoverImage(
            image = preview,
            isEnabled = isEnabled,
            onRemoveRequest = onRemoveRequest
        )
        if (targetFormat == Format.MP4) {
            WatermarkConfig(
                isEnabled = isEnabled,
                options = options,
                onPositionChange = onPositionChange,
                onOpacityChange = onOpacityChange,
                onScaleChange = onScaleChange
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CoverImage(
    isEnabled: Boolean,
    image: ImageBitmap,
    onRemoveRequest: () -> Unit
) {
    var isCoverHovered by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(70.dp)
            .onPointerEvent(eventType = PointerEventType.Enter) { isCoverHovered = true }
            .onPointerEvent(eventType = PointerEventType.Exit) { isCoverHovered = false }
    ) {
        // See https://stackoverflow.com/questions/64742457/how-to-load-image-in-kotlin-compose-desktop
        Image(
            bitmap = image,
            contentDescription = Messages.ICO_DSC_COVER_PREVIEW,
            colorFilter = if (!isEnabled) {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0F) })
            } else {
                null
            },
            modifier = Modifier
                .size(COVER_PREVIEW_SIZE.dp)
                .border(Dp.Hairline, borderColor)
                .blur(if (isCoverHovered && isEnabled) 8.dp else 0.dp)
                .drawBehind {
                    // Draws checkerboard in case the image contains transparent parts
                    val tileSize = 4f
                    val tileCount = (size.width / tileSize).toInt()
                    val darkColor = Color.hsl(0f, 0f, 0.8f)
                    val lightColor = Color.hsl(1f, 1f, 1f)
                    for (i in 0..tileCount) {
                        for (j in 0..tileCount) {
                            drawRect(
                                topLeft = Offset(i * tileSize, j * tileSize),
                                color = if ((i + j) % 2 == 0) darkColor else lightColor,
                                size = Size(tileSize, tileSize)
                            )
                        }
                    }
                }
        )
        if (isCoverHovered && isEnabled) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.6f))
                    .size(COVER_PREVIEW_SIZE.dp)
                    .clickable {
                        onRemoveRequest()
                        isCoverHovered = false
                    }
            ) {
                CustomIcon(
                    icon = Icons.Custom.Delete,
                    description = Messages.ICO_DSC_REMOVE_COVER
                )
            }
        }
    }
}

@Composable
private fun WatermarkConfig(
    isEnabled: Boolean,
    options: CoverOptions,
    onPositionChange: (WatermarkPosition) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit
) {
    val language = LocalLanguage.current
    WatermarkPlacement(
        isEnabled = isEnabled,
        position = options.position,
        onChange = onPositionChange
    )
    Column(verticalArrangement = Arrangement.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = language.messages.txtLblAlpha,
                fontSize = (defaultFontSize.value - 1).sp,
                modifier = Modifier.width(40.dp),
                color = if (isEnabled) {
                    LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                } else {
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                }
            )
            CustomTextField(
                isEnabled = isEnabled,
                value = (options.opacity * 100).roundToInt(),
                max = 100
            ) {
                onOpacityChange(it / 100f)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = language.messages.txtLblScale,
                fontSize = (defaultFontSize.value - 1).sp,
                modifier = Modifier.width(40.dp),
                color = if (isEnabled) {
                    LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                } else {
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                }
            )
            CustomTextField(
                isEnabled = isEnabled,
                value = (options.scale * 100).roundToInt(),
                max = 999
            ) {
                onScaleChange(it / 100f)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WatermarkPlacement(
    isEnabled: Boolean,
    position: WatermarkPosition,
    onChange: (WatermarkPosition) -> Unit
) {
    // Ensures the buttons are left-to-right regardless of the language direction
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        FlowRow(maxItemsInEachRow = 3, modifier = Modifier.selectableGroup()) {
            for (i in 0..8) {
                WatermarkPlacementOption(isEnabled = isEnabled, isSelected = (position.ordinal == i)) {
                    onChange(WatermarkPosition.entries[i])
                }
            }
        }
    }
}

@Composable
private fun WatermarkPlacementOption(
    isEnabled: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorPrimary by RadioButtonDefaults.colors().radioColor(isEnabled, isSelected)
    Canvas(
        modifier = Modifier
            .padding(4.dp)
            .size(16.dp)
            .clickable(
                onClick = onClick,
                enabled = isEnabled,
                interactionSource = remember(::MutableInteractionSource),
                indication = rememberRipple(bounded = false, radius = 8.dp)
            )
    ) {
        drawCircle(color = colorPrimary, radius = size.minDimension / 2, style = Stroke(2.dp.toPx()))
        if (isSelected) {
            drawCircle(color = colorPrimary, radius = size.minDimension / 4, style = Fill)
        }
    }
}

// FIXME: Duplicate of code in timestamp input to some extent
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CustomTextField(isEnabled: Boolean, value: Int, max: Int, onValueChange: (Int) -> Unit) {
    val language = LocalLanguage.current
    val colors = TextFieldDefaults.textFieldColors()
    var input by remember { mutableStateOf(value.toString()) }
    val interactionSource = remember(::MutableInteractionSource)
    BasicTextField(
        value = language.localizeDigits(input),
        onValueChange = { newString ->
            val intValue = newString.toIntOrNull() ?: -1
            input = (newString.takeIf { intValue in 0..max || it.isBlank() } ?: input).take(3)
            newString.toIntOrNull()?.takeIf { it in 1..max }?.let(onValueChange)
        },
        cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.high)
        ),
        interactionSource = interactionSource,
        visualTransformation = SuffixTransformer2(language.messages.txtLblPercentSign),
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

private class SuffixTransformer2(private val suffix: String) : VisualTransformation {
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
