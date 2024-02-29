package ir.mahozad.cutcon.ui.dialog

import androidx.compose.ui.awt.ComposeWindow
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.localization.Language
import java.awt.FileDialog
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists

private val logger = logger(name = "SaveFileDialog")

/**
 * To see the difference between [FileDialog] and [JFileChooser], see [showOpenFileDialog].
 *
 * To change the input file name based on file names in the new directory
 * when user changes directory in the dialog, see the below code (and https://stackoverflow.com/a/28407071):
 *
 * ```kotlin
 * fileChooser.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY) {
 *     val oldName = fileChooser.selectedFile?.name
 *     val newDirectory = fileChooser.currentDirectory.toPath()
 *     fileChooser.selectedFile = Path(defaultFileNameProvider(oldName, newDirectory)).toFile()
 * }
 *```
 */
fun ComposeWindow.showSaveFileDialog(
    language: Language,
    title: String,
    formatName: String,
    formatExtension: String,
    startingDirectory: Path?,
    approveButtonLabel: String,
    approveButtonTooltip: String,
    defaultFileNameProvider: (Path) -> String
): Path? {
    // Sets look and feel of file choosers to the native OS look and feel
    // See https://stackoverflow.com/q/10083447
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    UIManager.put("FileChooser.filesOfTypeLabelText", language.messages.txtLblFileFormat)
    UIManager.put("FileChooser.fileNameLabelText", language.messages.txtLblFileName)
    UIManager.put("FileChooser.cancelButtonText", language.messages.btnLblCancel)
    UIManager.put("FileChooser.cancelButtonToolTipText", language.messages.btnTlpCancelFileSave)
    UIManager.put("FileChooser.saveInLabelText", language.messages.txtLblSaveIn)
    UIManager.put("FileChooser.upFolderToolTipText", language.messages.btnTlpUpFolder)
    UIManager.put("FileChooser.newFolderToolTipText", language.messages.btnTlpNewFolder)
    UIManager.put("FileChooser.viewMenuButtonToolTipText", language.messages.btnTlpViewMenu)
    // This is shown when clicking on a directory in the file chooser
    UIManager.put("FileChooser.directoryOpenButtonText", language.messages.btnLblOpen)
    UIManager.put("FileChooser.directoryOpenButtonToolTipText", language.messages.btnTlpOpenFileChooser)
    val fileChooser = SaveFileDialog(formatExtension, language).apply {
        currentDirectory = startingDirectory?.toFile()
        selectedFile = Path(defaultFileNameProvider(currentDirectory.toPath())).toFile()
        fileFilter = FileNameExtensionFilter(formatName, formatExtension)
        isAcceptAllFileFilterUsed = false
        dialogType = JFileChooser.SAVE_DIALOG
        dialogTitle = title
        approveButtonText = approveButtonLabel
        approveButtonToolTipText = approveButtonTooltip
    }
    val result = fileChooser.showSaveDialog(this /* OR null */)
    logger.debug { "File chooser returned with result code $result" }
    return if (result == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile.toPath() else null
}

private class SaveFileDialog(
    private val fileExtension: String,
    private val language: Language
) : JFileChooser() {
    // To approve user file selection in the dialog,
    // see https://stackoverflow.com/q/28637921
    // and https://stackoverflow.com/q/18926629
    // and https://stackoverflow.com/a/3729157
    override fun approveSelection() {
        // User may enter just the name of an existing file without extension
        // so here we normalize name
        // NOTE: This is related to the code in MainViewModel::setSaveFile
        val file = if (selectedFile.name.endsWith(".$fileExtension", ignoreCase = true)) {
            selectedFile.toPath().parent / "${selectedFile.nameWithoutExtension}.$fileExtension"
        } else {
            selectedFile.toPath().parent / "${selectedFile.name}.$fileExtension"
        }
        // On Windows using NTFS, file name and extension are NOT case-sensitive
        // so the following check ignores case (which is our desired logic).
        // See https://stackoverflow.com/a/34717571
        if (file.exists()) {
            // Could also have used showConfirmDialog if localizing the button texts wasn't needed
            val overwritePromptResult = JOptionPane.showOptionDialog(
                this,
                language.messages.txtLblExistingFile,
                language.messages.dlgTitExistingFile,
                JOptionPane.YES_NO_OPTION, // OR YES_NO_CANCEL_OPTION
                JOptionPane.WARNING_MESSAGE,
                null,
                arrayOf(language.messages.btnLblOk, language.messages.btnLblCancel),
                language.messages.btnLblCancel
            )
            if (overwritePromptResult != JOptionPane.YES_OPTION) {
                return
            }
        }
        super.approveSelection()
    }
}
