package ir.mahozad.cutcon.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLocalization
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.model.Status
import ir.mahozad.cutcon.model.Theme
import ir.mahozad.cutcon.ui.dialog.AppExitConfirmDialog
import ir.mahozad.cutcon.ui.dialog.ChangelogDialog
import ir.mahozad.cutcon.ui.dialog.FailureDialog
import ir.mahozad.cutcon.ui.dialog.SuccessDialog
import ir.mahozad.cutcon.ui.panel.MainPanel
import ir.mahozad.cutcon.ui.panel.SidePanel
import ir.mahozad.cutcon.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.Font
import java.awt.Taskbar
import javax.sound.sampled.AudioSystem
import kotlin.io.path.div
import kotlin.math.roundToInt

private val logger = logger(name = "MainWindow")
private val taskbar by lazy {
    runCatching(Taskbar::getTaskbar)
        .onFailure { logger.debug(it) { "Getting OS taskbar failed" } }
        .onSuccess { logger.debug { "Got the OS taskbar instance" } }
        .getOrNull()
}

@Composable
fun MainWindow(viewModel: MainViewModel, onExitRequest: () -> Unit) {
    val theme by viewModel.theme.collectAsState()
    val language by viewModel.language.collectAsState()
    val calendar by viewModel.calendar.collectAsState()
    val isAlwaysOnTop by viewModel.isAlwaysOnTop.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val windowWidth by viewModel.windowWidth.collectAsState()
    // FIXME: The animation smoothness broke after CMP 1.6.2
    // FIXME: When the app theme is dark and toggling side panel,
    //  there is a bit of white flicker on the right side of panel
    val animatedWindowWidth by animateIntAsState(
        targetValue = windowWidth.value,
        animationSpec = if (windowWidth.isAnimated) tween(durationMillis = 240) else snap()
    )
    val windowHeight by viewModel.windowHeight.collectAsState()
    val windowPosition by viewModel.windowPosition.collectAsState()
    val windowState = rememberWindowState(
        placement = WindowPlacement.Floating,
        position = windowPosition,
        size = DpSize(windowWidth.value.dp, windowHeight.dp)
    )
    LaunchedEffect(Unit) {
        snapshotFlow { windowState.position }
            .onEach(viewModel::onWindowPositionChanged)
            .launchIn(this)
    }
    LaunchedEffect(animatedWindowWidth, windowHeight, windowPosition, isFullscreen) {
        windowState.size = DpSize(animatedWindowWidth.dp, windowHeight.dp)
        windowState.position = windowPosition
        windowState.placement = if (isFullscreen) WindowPlacement.Fullscreen else WindowPlacement.Floating
    }
    Window(
        title = language.messages.appName, // Is used for app title in taskbar
        icon = painterResource("logo.svg"), // Is used for app icon in taskbar
        state = windowState,
        resizable = false,
        undecorated = true, // See window decoration component for more information
        transparent = false, // See window decoration component for more information
        alwaysOnTop = isAlwaysOnTop,
        // Called when clicking on close button on app taskbar preview
        onCloseRequest = { viewModel.onAppExitRequest(forceExit = false, onExitRequest) },
        onKeyEvent = {
            viewModel.onKeyboardEvent(it)
            false // Does not matter here
        }
    ) {
        AppTheme(isDark = theme == Theme.DARK) {
            CompositionLocalProvider(
                LocalLanguage provides language,
                LocalCalendar provides calendar,
                LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = FontFamily(Font(language.fontResource))),
                LocalLocalization provides language.contextMenuLocalization,
                LocalLayoutDirection provides LayoutDirection.Ltr
            ) {
                Taskbar(viewModel.status.collectAsState().value)
                Dialogs(viewModel) { viewModel.onAppExitRequest(forceExit = true, exit = onExitRequest) }
                WindowDecoration(
                    isDecorationVisible = !isFullscreen,
                    title = { WindowTitle(it) },
                    icon = painterResource("logo.svg"),
                    isMinimizable = true,
                    onCloseRequest = { viewModel.onAppExitRequest(forceExit = false, exit = onExitRequest) }
                ) {
                    MainContent(viewModel)
                }
            }
        }
    }
}

@Composable
private fun FrameWindowScope.Taskbar(status: Status) {
    LaunchedEffect(status) {
        val state = when (status) {
            is Status.Initializing -> Taskbar.State.INDETERMINATE
            is Status.InProgress -> Taskbar.State.NORMAL
            else -> Taskbar.State.OFF
        }
        val value = (status as? Status.InProgress)?.progress ?: 0f
        // The check is required to prevent exception on some platforms (e.g. macOS 10.13)
        if (taskbar?.isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW) == true) {
            taskbar?.setWindowProgressValue(window, (value * 100).roundToInt())
            taskbar?.setWindowProgressState(window, state)
        }
    }
}

@Composable
private fun WindowTitle(defaultFontSize: TextUnit) {
    val language = LocalLanguage.current
    Text(
        text = language.messages.appName,
        fontSize = defaultFontSize,
        modifier = Modifier.offset(y = if (language is LanguageFa) (-1).dp else 0.dp)
    )
}

/**
 * See https://github.com/JetBrains/compose-multiplatform/issues/2481
 * and https://github.com/JetBrains/compose-multiplatform/issues/865
 * and https://plugins.jetbrains.com/plugin/16541-compose-multiplatform-ide-support/versions/snapshots
 *
 * FIXME: Preview does not work
 */
@Preview
@Composable
private fun MainContentPreview() {
    val fakeWindowScope = object : FrameWindowScope {
        override val window get() = TODO("Not implemented") // OR ComposeWindow()
    }
    val viewModel = Class
        .forName("ir.mahozad.cutcon.MainKt")
        .getDeclaredField("viewModel")
        .apply { isAccessible = true }
        .get(null /* because a top-level property is static */) as MainViewModel
    with(fakeWindowScope) { MainContent(viewModel) }
}

/**
 * To access the current window, the function extends on [FrameWindowScope].
 * We can also pass the window as an argument to the function.
 *
 * In previous versions of Compose JB/Desktop/Multiplatform accessing
 * the window was done using `LocalAppWindow.current.window`.
 *
 * See https://github.com/JetBrains/compose-multiplatform/issues/176#issuecomment-812514936
 * and its subsequent comments.
 */
@Composable
private fun FrameWindowScope.MainContent(viewModel: MainViewModel) {
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val isSidePanelDisplayed by viewModel.isSidePanelDisplayed.collectAsState()
    Row(
        modifier = Modifier.padding(horizontal = if (isFullscreen) 0.dp else 8.dp),
        horizontalArrangement = Arrangement.Absolute.spacedBy(8.dp)
    ) {
        MainPanel(viewModel)
        if (isSidePanelDisplayed) {
            SidePanel(viewModel)
        }
    }
}

/**
 * Note: The code to react upon change of a dialog display property and show the dialog should only be in a single place
 * (for example, here) to avoid multiple pieces of code from showing multiple instances of a dialog simultaneously when
 * the property becomes true.
 *
 * For example, in version 1.11.0 of the app, when the ShowChangelog button in about screen was clicked, the
 * [MainViewModel.isChangelogDialogDisplayed] became true and the if statement here showed the dialog. But, there was
 * also an if statement in about panel code itself that also showed the dialog.
 * So, all in all, two dialogs were displayed simultaneously on top of each other.
 * This was evident in the darker dialog scrim because of the two overlapping each other.
 */
@Composable
private fun Dialogs(viewModel: MainViewModel, onCloseRequest: () -> Unit) {
    val status by viewModel.status.collectAsState()
    val language = LocalLanguage.current
    val isFinishSoundEnabled by viewModel.isFinishSoundEnabled.collectAsState()
    val isChangelogDialogDisplayed by viewModel.isChangelogDialogDisplayed.collectAsState()
    val isAppExitConfirmDialogDisplayed by viewModel.isAppExitConfirmDialogDisplayed.collectAsState()
    LaunchedEffect(status, isFinishSoundEnabled) {
        if (status is Status.Finished.Success && isFinishSoundEnabled) {
            withContext(Dispatchers.IO) {
                val soundPath = assetsPath / "notification.wav"
                val audioStream = AudioSystem.getAudioInputStream(soundPath.toFile())
                val audioClip = AudioSystem.getClip()
                audioClip.open(audioStream)
                audioClip.start()
            }
        }
    }
    // This is needed because we have set the default parent layout direction to LTR
    CompositionLocalProvider(LocalLayoutDirection provides language.layoutDirection) {
        if (isChangelogDialogDisplayed) {
            ChangelogDialog(
                changelog = language.messages.changelog,
                onCloseRequest = viewModel::onChangelogDialogDismissRequest
            )
        } else if (isAppExitConfirmDialogDisplayed) {
            AppExitConfirmDialog(
                onDenied = viewModel::onAppExitConfirmDialogDismissRequest,
                onConfirmed = onCloseRequest
            )
        } else if (status is Status.Finished.Success) {
            SuccessDialog(
                totalTime = (status as? Status.Finished.Success)?.totalTime,
                onCloseRequest = viewModel::onFinishDialogDismissRequest
            )
        } else if (status is Status.Finished.Failure) {
            FailureDialog(
                throwable = (status as? Status.Finished.Failure)?.throwable,
                onCloseRequest = viewModel::onFinishDialogDismissRequest
            )
        }
    }
}
