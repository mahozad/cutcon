package ir.mahozad.cutcon.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.FrameWindowScope
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.LocalSourceSupportedFileType
import ir.mahozad.cutcon.model.Source
import ir.mahozad.cutcon.ui.dialog.showOpenFileDialog
import ir.mahozad.cutcon.ui.icon.Folder
import ir.mahozad.cutcon.ui.icon.Icons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.nio.file.Path

@Preview
@Composable
fun SourceInputPreview() {
    val fakeWindowScope = object : FrameWindowScope {
        override val window get() = TODO("Not implemented") // OR ComposeWindow()
    }
//    fakeWindowScope.SourceInput()
}

private val supportedFileExtensions = LocalSourceSupportedFileType
    .entries
    .flatMap { it.extensions.toList() }
    .toTypedArray()

/**
 * For the previous implementation, see the v1.2.0 Git tag.
 */
@Composable
fun FrameWindowScope.SourceInput(
    source: Source,
    lastOpenDirectory: Path?,
    onSetSourceToLocalRequest: (Path) -> Unit,
) {
    val language = LocalLanguage.current
    var isLocalFileSelectorDisplayed by remember { mutableStateOf(false) }
    /**
     * See [SaveAsInput] for more information about this
     */
    var dialogClosedTime by remember { mutableLongStateOf(0) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(2.dp))
        when (source) {
            is Source.Local -> OpenLocalFolderButton((source as? Source.Local)?.path)
        }
        Spacer(Modifier.width(4.dp))
        when (source) {
            is Source.Local -> {
                ClickableInput(
                    text = source.path.trim(maxLength = 26).toLtrString(),
                    modifier = Modifier.weight(2.4f),
                    startRoundness = 24.dp,
                    endRoundness = 0.dp
                ) {
                    isLocalFileSelectorDisplayed = true
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = defaultInputHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .background(color = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity))
                .clickable(enabled = true, onClick = { isLocalFileSelectorDisplayed = true })
        ) {
            DropDownLabel(
                label = source.label(language),
                isLoading = isLocalFileSelectorDisplayed
            )
        }
        LaunchedEffect(isLocalFileSelectorDisplayed) {
            if (
                !isLocalFileSelectorDisplayed ||
                System.currentTimeMillis() - dialogClosedTime < 300
            ) {
                return@LaunchedEffect
            }
            launch(Dispatchers.IO) {
                showLocalFileSelector(
                    language = language,
                    startingDirectory = lastOpenDirectory,
                    onCancel = {
                        dialogClosedTime = System.currentTimeMillis()
                        isLocalFileSelectorDisplayed = false
                    },
                    onSelected = { path ->
                        onSetSourceToLocalRequest(path)
                        dialogClosedTime = System.currentTimeMillis()
                        isLocalFileSelectorDisplayed = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BoxScope.DropDownLabel(label: String, isLoading: Boolean) {
    val language = LocalLanguage.current
    if (isLoading) {
        Spinner(modifier = Modifier.padding(end = 10.dp).size(12.dp))
    } else {
        Text(
            text = label,
            maxLines = 1,
            fontSize = defaultFontSize,
            modifier = Modifier
                .padding(end = 16.dp, bottom = if (language is LanguageFa) 1.dp else 0.dp)
                .align(Alignment.Center)
        )
    }
}

private fun FrameWindowScope.showLocalFileSelector(
    language: Language,
    startingDirectory: Path?,
    onCancel: () -> Unit,
    onSelected: (Path) -> Unit
) {
    window.showOpenFileDialog(
        language = language,
        title = language.messages.dlgTitSelectLocalFile,
        startingDirectory = startingDirectory,
        approveButtonLabel = language.messages.btnLblApproveSelectedFile,
        approveButtonTooltip = language.messages.btnTlpApproveSelectedFile,
        fileExtensionDescription = language.messages.txtLblLocalFileSupportedTypesDescription,
        fileExtensions = supportedFileExtensions
    )
        ?.let(onSelected)
        ?: onCancel()
}

@Composable
private fun SourceItemUi(text: String) {
    Text(
        text = text,
        fontSize = defaultFontSize,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// NOTE: Copied from code of Dropdown
@Composable
private fun ClickableInput(
    text: String,
    modifier: Modifier = Modifier,
    startRoundness: Dp,
    endRoundness: Dp,
    onClick: () -> Unit
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = defaultInputHeight)
            .clip(
                RoundedCornerShape(
                    topStart = startRoundness,
                    bottomStart = startRoundness,
                    topEnd = endRoundness,
                    bottomEnd = endRoundness
                )
            )
            .background(color = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity))
            .onGloballyPositioned { textFieldSize = it.size.toSize() }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            maxLines = 1,
            fontSize = defaultFontSize,
            // https://stackoverflow.com/a/69896666
            modifier = Modifier
                .alpha(ContentAlpha.high)
                .padding(
                    top = if (LocalLanguage.current is LanguageFa) 3.dp else 0.dp,
                    end = if (endRoundness > 0.dp) 12.dp else 0.dp
                )
        )
    }
}

@Composable
private fun OpenLocalFolderButton(localFile: Path?) {
    val scope = rememberCoroutineScope()
    Tooltip(LocalLanguage.current.messages.openSourceFolder) {
        IconButton(onClick = {
            scope.launch(Dispatchers.IO) {
                localFile?.parent?.toFile()?.let(Desktop.getDesktop()::open)
            }
        }) {
            CustomIcon(
                icon = Icons.Custom.Folder,
                description = Messages.ICO_DSC_OPEN_FOLDER
            )
        }
    }
}
