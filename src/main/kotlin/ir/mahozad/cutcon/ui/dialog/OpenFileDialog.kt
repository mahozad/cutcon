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

private val logger = logger(name = "OpenFileDialog")

/**
 * Note that [FileDialog] shows the native operating system chooser while the [JFileChooser]
 * shows the Java custom dialog (which can be made to *look like* the OS dialog by setting its look and feel),
 * but even then, its layout, labels, and options are not exactly the same as the native one.
 * FileDialog and JFileChooser have these differences:
 * - FileDialog layout, labels, and options is the same as all applications that show the OS native file chooser
 * - FileDialog buttons and labels (except dialog title) cannot be localized
 * - FileDialog file filtering does not work on Windows as of Java 17
 * - FileDialog automatically remembers last opened directory even after app restart
 * - FileDialog shows an old look and feel for buttons (when running the installed app exe)
 * - FileDialog is displayed a little faster
 * - FileDialog prompts for replace confirmation if choosing an existing file for save,
 *   but this has to be implemented manually in JFileChooser
 * - The files and directories in FileDialog can be renamed and deleted
 *   (especially useful for manipulating a directory created in the dialog itself),
 *   but it does not seem to be possible in JFileChooser (unless, possibly, through manual implementation).
 *
 * ```kotlin
 * val dialog = FileDialog(this /* OR null */).apply {
 *     setTitle(title)
 *     mode = FileDialog.SAVE
 *     file = startingDirectory?.let(defaultFileNameProvider)
 *     directory = startingDirectory?.toString()
 *     filenameFilter = FilenameFilter { _, name -> name.lowercase().endsWith(formatExtension) }
 *     isVisible = true
 * }
 * val result = dialog.directory?.let(::Path)?.resolve(dialog.file)
 * logger.debug { "File chooser returned ${result?.toLtrString()}" }
 * return result
 * ```
 *
 * See https://github.com/JetBrains/compose-multiplatform/issues/176
 */
fun ComposeWindow.showOpenFileDialog(
    language: Language,
    title: String,
    startingDirectory: Path?,
    approveButtonLabel: String,
    approveButtonTooltip: String,
    fileExtensionDescription: String,
    vararg fileExtensions: String
): Path? {
    // NOTE: Make sure UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    //  is called at the start of the program to get OS native look and feel
    //
    // See https://stackoverflow.com/q/4850315
    // and https://stackoverflow.com/q/16941623
    // and https://stackoverflow.com/a/29086587
    //
    // "FileChooser.acceptAllFileFilterText"
    // "FileChooser.ancestorInputMap"
    // "FileChooser.byDateText"
    // "FileChooser.byNameText"
    // "FileChooser.cancelButtonMnemonic"
    // "FileChooser.cancelButtonText"
    // "FileChooser.chooseButtonText"
    // "FileChooser.createButtonText"
    // "FileChooser.desktopName"
    // "FileChooser.detailsViewIcon" // For example: ImageIcon(FileSystem.class.getResource("folder.png"))
    // "FileChooser.directoryDescriptionText"
    // "FileChooser.directoryOpenButtonMnemonic"
    // "FileChooser.directoryOpenButtonText"
    // "FileChooser.fileDescriptionText"
    // "FileChooser.fileNameLabelMnemonic"
    // "FileChooser.fileNameLabelText"
    // "FileChooser.openButtonToolTipText"
    // "FileChooser.cancelButtonToolTipText"
    // "FileChooser.upFolderToolTipText"
    // "FileChooser.homeFolderToolTipText"
    // "FileChooser.listViewButtonToolTipText"
    // "FileChooser.fileNameHeaderText"
    // "FileChooser.fileSizeGigaBytes"
    // "FileChooser.fileSizeKiloBytes"
    // "FileChooser.fileSizeMegaBytes"
    // "FileChooser.filesOfTypeLabelMnemonic"
    // "FileChooser.filesOfTypeLabelText"
    // "FileChooser.helpButtonMnemonic"
    // "FileChooser.helpButtonText"
    // "FileChooser.homeFolderIcon"
    // "FileChooser.listViewIcon"
    // "FileChooser.saveInLabelText"
    // "FileChooser.lookInLabelText"
    // "FileChooser.lookInLabelMnemonic"
    // "FileChooser.mac.newFolder"
    // "FileChooser.mac.newFolder.subsequent"
    // "FileChooser.newFolderAccessibleName"
    // "FileChooser.newFolderButtonText"
    // "FileChooser.newFolderErrorSeparator"
    // "FileChooser.newFolderErrorText"
    // "FileChooser.newFolderExistsErrorText"
    // "FileChooser.newFolderIcon"
    // "FileChooser.newFolderPromptText"
    // "FileChooser.newFolderTitleText"
    // "FileChooser.newFolderToolTipText"
    // "FileChooser.acceptAllFileFilterText"
    // "FileChooser.renameFileButtonText"
    // "FileChooser.deleteFileButtonText"
    // "FileChooser.filterLabelText"
    // "FileChooser.detailsViewButtonToolTipText"
    // "FileChooser.detailsViewButtonAccessibleName"
    // "FileChooser.detailsViewActionLabel.textAndMnemonic"
    // "FileChooser.listViewButtonToolTipText"
    // "FileChooser.listViewButtonAccessibleName"
    // "FileChooser.viewMenuButtonToolTipText"
    // "FileChooser.fileSizeHeaderText"
    // "FileChooser.fileDateHeaderText"
    // "FileChooser.openButtonMnemonic
    // "FileChooser.openButtonText"
    // "FileChooser.openDialogTitleText"
    // "FileChooser.openTitleText"
    // "FileChooser.readOnly"
    // "FileChooser.saveButtonMnemonic"
    // "FileChooser.saveButtonText"
    // "FileChooser.saveDialogFileNameLabelText"
    // "FileChooser.saveDialogTitleText"
    // "FileChooser.saveTitleText"
    // "FileChooser.untitledFileName"
    // "FileChooser.untitledFolderName"
    // "FileChooser.upFolderIcon"
    // "FileChooser.updateButtonMnemonic"
    // "FileChooser.updateButtonText"
    // "FileChooser.useSystemExtensionHiding"
    // "FileChooser.usesSingleFilePane"
    //
    // Sets look and feel of file choosers to the native OS look and feel
    // See https://stackoverflow.com/q/10083447
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    UIManager.put("FileChooser.filesOfTypeLabelText", language.messages.txtLblFileType)
    UIManager.put("FileChooser.fileNameLabelText", language.messages.txtLblFileName)
    UIManager.put("FileChooser.lookInLabelText", language.messages.txtLblLookIn)
    UIManager.put("FileChooser.cancelButtonText", language.messages.btnLblCancel)
    UIManager.put("FileChooser.cancelButtonToolTipText", language.messages.btnTlpCancelFileChooser)
    UIManager.put("FileChooser.acceptAllFileFilterText", language.messages.txtLblFileTypeAll)
    UIManager.put("FileChooser.upFolderToolTipText", language.messages.btnTlpUpFolder)
    UIManager.put("FileChooser.newFolderToolTipText", language.messages.btnTlpNewFolder)
    UIManager.put("FileChooser.viewMenuButtonToolTipText", language.messages.btnTlpViewMenu)
    // This is shown when clicking on a directory in the file chooser
    UIManager.put("FileChooser.directoryOpenButtonText", language.messages.btnLblOpen)
    UIManager.put("FileChooser.directoryOpenButtonToolTipText", language.messages.btnTlpOpenFileChooser)
    // UIManager.put("FileChooser.saveButtonText", "Select")
    val fileChooser = OpenFileDialog(language).apply {
        // See https://stackoverflow.com/q/6724784
        // applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT)
        currentDirectory = startingDirectory?.toFile()
        if (fileExtensions.isNotEmpty()) {
            fileFilter = FileNameExtensionFilter(fileExtensionDescription, *fileExtensions)
        }
        isMultiSelectionEnabled = false
        fileSelectionMode = JFileChooser.FILES_ONLY
        dialogTitle = title
        approveButtonText = approveButtonLabel
        approveButtonToolTipText = approveButtonTooltip
    }
    val result = fileChooser.showOpenDialog(this /* OR null */)
    logger.debug { "File chooser returned with result code $result" }
    return if (result == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile.toPath() else null
}

private class OpenFileDialog(
    private val language: Language
) : JFileChooser() {
    override fun approveSelection() {
        if (selectedFile.exists()) {
            super.approveSelection()
        } else {
            // Could also have used showMessageDialog if localizing the ok button text wasn't needed
            JOptionPane.showOptionDialog(
                this,
                language.messages.txtLblMissingFile,
                language.messages.dlgTitMissingFile,
                JOptionPane.OK_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                arrayOf(language.messages.btnLblOk),
                language.messages.btnLblOk
            )
        }
    }
}
