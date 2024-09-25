package ir.mahozad.cutcon.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.Format
import ir.mahozad.cutcon.model.Source
import ir.mahozad.cutcon.toLtrString
import ir.mahozad.cutcon.trim
import ir.mahozad.cutcon.ui.dialog.showSaveFileDialog
import ir.mahozad.cutcon.ui.icon.Folder
import ir.mahozad.cutcon.ui.icon.Icons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.nio.file.Path
import kotlin.io.path.Path

@Composable
@Preview
fun SaveAsInputPreview() {
    val fakeWindowScope = object : FrameWindowScope {
        // OR override val window get() = ComposeWindow()
        override val window get() = TODO("Not needed")
    }
    with(fakeWindowScope) {
        SaveAsInput(
            isEnabled = true,
            source = Source.Local(Path("1234.mp4")),
            destination = Path("a.mp3"),
            targetFormat = Format.MP3,
            defaultNameProvider = { "a" },
            lastSaveDirectory = null,
            onFileSpecified = {}
        )
    }
}

@Composable
fun FrameWindowScope.SaveAsInput(
    isEnabled: Boolean,
    source: Source,
    destination: Path?,
    targetFormat: Format,
    lastSaveDirectory: Path?,
    defaultNameProvider: (Path) -> String,
    onFileSpecified: (Path) -> Unit
) {
    val language = LocalLanguage.current
    val scope = rememberCoroutineScope()
    var isDialogDisplayed by remember { mutableStateOf(false) }
    // A fix (workaround) to prevent opening multiple dialogs
    // when the button is pressed multiple times in rapid succession and also
    // to fix the bug which caused the dialog to open again when Enter was pressed in an open dialog
    // (this problem was probably because the dialog accept button and the app open file button
    // both received the key event)
    // See https://github.com/JetBrains/compose-multiplatform/issues/3892
    // and https://al-e-shevelev.medium.com/how-to-prevent-multiple-clicks-in-android-jetpack-compose-8e62224c9c5e
    // and https://stackoverflow.com/q/69901608
    var dialogClosedTime by remember { mutableLongStateOf(0) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (lastSaveDirectory != null) {
            Spacer(Modifier.width(2.dp))
            Tooltip(language.messages.openSaveFolder) {
                IconButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        // See https://stackoverflow.com/a/12340147
                        Desktop.getDesktop().open(lastSaveDirectory.toFile())
                    }
                }) {
                    CustomIcon(
                        icon = Icons.Custom.Folder,
                        description = Messages.ICO_DSC_OPEN_FOLDER
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
        }
        OutlinedButton(
            enabled = isEnabled,
            modifier = Modifier.weight(1f),
            onClick = {
                if (
                    isDialogDisplayed ||
                    System.currentTimeMillis() - dialogClosedTime < 300
                ) {
                    return@OutlinedButton
                }
                dialogClosedTime = System.currentTimeMillis()
                isDialogDisplayed = true
                scope.launch(Dispatchers.IO) {
                    val result = window.showSaveFileDialog(
                        language = language,
                        title = language.messages.dlgTitSpecifySaveFile,
                        formatName = targetFormat.actualName(source),
                        formatExtension = targetFormat.extension(source),
                        defaultFileNameProvider = defaultNameProvider,
                        startingDirectory = lastSaveDirectory,
                        approveButtonLabel = language.messages.btnLblApproveSaveFile,
                        approveButtonTooltip = language.messages.btnTlpApproveSaveFolder
                    )
                    dialogClosedTime = System.currentTimeMillis()
                    isDialogDisplayed = false
                    result?.let(onFileSpecified)
                }
            }
        ) {
            AnimatedVisibility(isDialogDisplayed) {
                Row {
                    Spinner(modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                }
            }
            Text(
                text = remember(destination) {
                    destination?.trim(maxLength = 35)?.toLtrString() ?: language.messages.btnLblSelectSaveFolder
                },
                fontSize = if (destination == null) defaultFontSize else (defaultFontSize.value - 1).sp,
                lineHeight = 20.sp
            )
        }
    }
}
