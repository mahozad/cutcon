package ir.mahozad.cutcon.ui.panel

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.Shortcut
import ir.mahozad.cutcon.ui.icon.*
import ir.mahozad.cutcon.ui.widget.*
import java.net.URI
import kotlin.io.path.toPath
import kotlin.time.Duration.Companion.seconds

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun MainPanel() {
    val image by viewModel.displayImage.collectAsState(null)
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val isMiniScreen by viewModel.isMiniScreen.collectAsState()
    var isDragging by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = if (isFullscreen) {
            Modifier.fillMaxWidth()
        } else {
            Modifier.width(DISPLAY_WIDTH.dp)
        }
    ) {
        Box(
            modifier = Modifier
                .combinedClickable(
                    indication = null, // This is required to remove the subtle indication when clicking on display image
                    interactionSource = remember(::MutableInteractionSource),
                    onDoubleClick = { if (isFullscreen) viewModel.exitFullscreen() else viewModel.enterFullscreen() },
                    onClick = {},
                ).then(
                    if (isFullscreen) {
                        Modifier.fillMaxSize()
                    } else if (isMiniScreen) {
                        Modifier.width(DISPLAY_WIDTH_MINI.dp).height(DISPLAY_HEIGHT_MINI.dp)
                    } else {
                        Modifier.width(DISPLAY_WIDTH.dp).height(DISPLAY_HEIGHT.dp)
                    }
                )
        ) {
            Display(
                image = image,
                aspectRatio = aspectRatio,
                modifier = Modifier
                    .fillMaxSize()
                    .onExternalDrag(
                        onDragStart = { isDragging = true },
                        onDragExit = { isDragging = false },
                        onDrop = { state ->
                            isDragging = false
                            val dragData = state.dragData
                            if (dragData is DragData.FilesList) {
                                dragData
                                    .readFiles()
                                    .first()
                                    .let(::URI)
                                    .let(URI::toPath)
                                    .let(viewModel::setSourceToLocal)
                            }
                        }
                    )
            )
            if (isFullscreen) {
                ExitFullScreenButton()
            }
            if (isDragging) {
                PlayMediaIndicator()
            }
        }
        if (isMiniScreen && !isFullscreen) {
            ControlsForMiniScreen()
        } else if (!isFullscreen) {
            ControlsForRegularScreen()
        }
    }
}

@Composable
private fun PlayMediaIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface.copy(alpha = 0.8f))
    ) {
        Icon(
            imageVector = Icons.Custom.Play,
            contentDescription = Messages.ICO_DSC_PLAY_FILE,
            modifier = Modifier.size(64.dp).align(Alignment.Center)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
private fun ExitFullScreenButton() {
    // Do not move this up and outside the if block
    // (so the offset resets automatically when fullscreen exits)
    var offsetOfCloseButton by remember { mutableStateOf((-48).dp) }
    val offsetOfCloseButtonAnimated by animateDpAsState(offsetOfCloseButton)
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .onPointerEvent(eventType = PointerEventType.Enter) { offsetOfCloseButton = 64.dp }
            .onPointerEvent(eventType = PointerEventType.Exit) { offsetOfCloseButton = (-48).dp }
    ) {
        Surface(
            color = MaterialTheme.colors.surface.copy(alpha = 0.66f),
            shape = CircleShape,
            onClick = viewModel::exitFullscreen,
            elevation = 0.dp,
            modifier = Modifier
                .offset(y = offsetOfCloseButtonAnimated)
                .border(Dp.Hairline, MaterialTheme.colors.onSurface.copy(alpha = 0.5f), CircleShape)
        ) {
            CustomIcon(
                icon = Icons.Custom.Close,
                tint = MaterialTheme.colors.onSurface,
                description = Messages.ICO_DSC_EXIT_FULLSCREEN
            )
        }
    }
}

@Composable
private fun ControlsForRegularScreen() {
    val clip by viewModel.clip.collectAsState()
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    val isScreenshotInputEnabled by viewModel.isScreenshotInputEnabled.collectAsState()
    val isScreenshotInputActive by viewModel.isScreenshotInputActive.collectAsState()
    MediaPlayerProgress(
        isWavy = mediaInfo.isResumed,
        progress = mediaInfo.progress,
        onSeek = viewModel::setSeek
    )
    Row {
        Box(modifier = Modifier.width(4 * 48.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimestampInputs()
                ClipControls()
                Spacer(Modifier.height(7.dp))
                ClipLength(clip)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Row {
                SeekBackwardShortButton()
                PlayPauseButton(iconPadding = 10.dp)
                SeekForwardShortButton()
            }
            Row {
                SeekBackwardLongButton()
                ScreenshotButton(
                    isEnabled = isScreenshotInputEnabled,
                    isActive = isScreenshotInputActive,
                    onClick = viewModel::takeScreenshot
                )
                SeekForwardLongButton()
            }
        }
        Column(modifier = Modifier.width(192.dp)) {
            AudioInput(
                viewModel = viewModel,
                iconPadding = 12.dp,
                mainModifier = Modifier.padding(end = 14.dp),
                sliderModifier = Modifier
            )
            SpeedInput(
                speed = mediaInfo.speed,
                onReset = viewModel::resetSpeed,
                onChange = viewModel::setSpeed
            )
            Row(
                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                PinButton(iconPadding = 12.dp)
                FullscreenEnterButton()
                MiniScreenEnterButton()
                SidePanelToggleButton()
            }
        }
    }
}

@Composable
private fun SeekBackwardShortButton() {
    val language = LocalLanguage.current
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    Tooltip(
        language.messages.seek5SecondsBackward,
        Shortcut.SEEK_SHORT_BACKWARD.symbol
    ) {
        IconButton(onClick = {
            viewModel.setSeek(((mediaInfo.progress.time - 5.seconds) / mediaInfo.progress.length).toFloat())
        }) {
            CustomIcon(
                icon = if (language is LanguageFa) {
                    Icons.Custom.Rewind5Fa
                } else {
                    Icons.Custom.Rewind5En
                },
                description = Messages.ICO_DSC_REWIND_5_SECONDS
            )
        }
    }
}

@Composable
private fun SeekBackwardLongButton() {
    val language = LocalLanguage.current
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    Tooltip(
        language.messages.seek30SecondsBackward,
        Shortcut.SEEK_LONG_BACKWARD.symbol
    ) {
        IconButton(onClick = {
            viewModel.setSeek(((mediaInfo.progress.time - 30.seconds) / mediaInfo.progress.length).toFloat())
        }) {
            Icon(
                imageVector = if (language is LanguageFa) {
                    Icons.Custom.Rewind30Fa
                } else {
                    Icons.Custom.Rewind30En
                },
                contentDescription = Messages.ICO_DSC_REWIND_30_SECONDS,
                modifier = Modifier.padding(6.dp).size(defaultIconSize)
            )
        }
    }
}

@Composable
private fun SeekForwardShortButton() {
    val language = LocalLanguage.current
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    Tooltip(
        language.messages.seek5SecondsForward,
        Shortcut.SEEK_SHORT_FORWARD.symbol
    ) {
        IconButton(onClick = {
            viewModel.setSeek(((mediaInfo.progress.time + 5.seconds) / mediaInfo.progress.length).toFloat())
        }) {
            CustomIcon(
                icon = if (language is LanguageFa) {
                    Icons.Custom.Forward5Fa
                } else {
                    Icons.Custom.Forward5En
                },
                description = Messages.ICO_DSC_FORWARD_5_SECONDS
            )
        }
    }
}

@Composable
private fun SeekForwardLongButton() {
    val language = LocalLanguage.current
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    Tooltip(
        language.messages.seek30SecondsForward,
        Shortcut.SEEK_LONG_FORWARD.symbol
    ) {
        IconButton(onClick = {
            viewModel.setSeek(((mediaInfo.progress.time + 30.seconds) / mediaInfo.progress.length).toFloat())
        }) {
            Icon(
                imageVector = if (language is LanguageFa) {
                    Icons.Custom.Forward30Fa
                } else {
                    Icons.Custom.Forward30En
                },
                contentDescription = Messages.ICO_DSC_FORWARD_30_SECONDS,
                modifier = Modifier.padding(6.dp).size(defaultIconSize)
            )
        }
    }
}

@Composable
private fun FullscreenEnterButton() {
    Tooltip(
        LocalLanguage.current.messages.switchToFullscreen,
        Shortcut.FULLSCREEN_TOGGLE.symbol,
        Shortcut.FULLSCREEN_EXIT.symbol
    ) {
        IconButton(onClick = viewModel::enterFullscreen) {
            CustomIcon(
                icon = Icons.Custom.FullScreenEnter,
                description = Messages.ICO_DSC_ENTER_FULLSCREEN
            )
        }
    }
}

@Composable
private fun MiniScreenEnterButton() {
    Tooltip(
        LocalLanguage.current.messages.switchToMiniMode,
        Shortcut.MINI_MODE_TOGGLE.symbol
    ) {
        IconButton(onClick = viewModel::toggleMiniScreen) {
            CustomIcon(
                icon = Icons.Custom.MiniScreen,
                description = Messages.ICO_DSC_ENTER_MINI_SCREEN
            )
        }
    }
}

@Composable
private fun SidePanelToggleButton() {
    val language = LocalLanguage.current
    val isSidePanelDisplayed by viewModel.isSidePanelDisplayed.collectAsState()
    Tooltip(
        if (isSidePanelDisplayed) {
            language.messages.hideSidePanel
        } else {
            language.messages.showSidePanel
        },
        Shortcut.SIDE_PANEL_TOGGLE.symbol
    ) {
        IconButton(onClick = viewModel::toggleSidePanel) {
            CustomIcon(
                icon = if (isSidePanelDisplayed) Icons.Custom.SidePanelOn else Icons.Custom.SidePanelOff,
                description = Messages.ICO_DSC_TOGGLE_SIDE_PANEL
            )
        }
    }
}

@Composable
private fun TimestampInputs() {
    val clipStartMinuteInput by viewModel.clipStartMinuteInput.collectAsState()
    val clipStartSecondInput by viewModel.clipStartSecondInput.collectAsState()
    val clipEndMinuteInput by viewModel.clipEndMinuteInput.collectAsState()
    val clipEndSecondInput by viewModel.clipEndSecondInput.collectAsState()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(48.dp)) {
        TimestampInput(
            startRoundness = 24.dp,
            minuteInput = clipStartMinuteInput,
            secondInput = clipStartSecondInput,
            onMinuteChange = viewModel::onClipStartMinuteChanged,
            onSecondChange = viewModel::onClipStartSecondChanged,
        )
        Spacer(Modifier.width(8.dp))
        TimestampInput(
            endRoundness = 24.dp,
            minuteInput = clipEndMinuteInput,
            secondInput = clipEndSecondInput,
            onMinuteChange = viewModel::onClipEndMinuteChanged,
            onSecondChange = viewModel::onClipEndSecondChanged,
        )
    }
}

@Composable
private fun ClipControls() {
    val language = LocalLanguage.current
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    val isLoopToggleable by viewModel.isLoopToggleable.collectAsState()
    Row {
        Tooltip(
            language.messages.setClipStart,
            Shortcut.CLIP_START_BEGINNING.symbol,
            Shortcut.CLIP_START_NOW.symbol
        ) {
            IconButton(onClick = viewModel::onSetClipStartToNow) {
                CustomIcon(
                    icon = Icons.Custom.StartCircle,
                    description = Messages.ICO_DSC_SET_CLIP_START_NOW
                )
            }
        }
        Tooltip(
            if (mediaInfo.clipToLoop == null) {
                language.messages.turnOnClipLoop
            } else {
                language.messages.turnOffClipLoop
            },
            Shortcut.CLIP_LOOP_TOGGLE.symbol
        ) {
            IconButton(
                enabled = isLoopToggleable,
                onClick = viewModel::toggleClipLoop
            ) {
                CustomIcon(
                    icon = if (mediaInfo.clipToLoop == null) Icons.Custom.LoopOff else Icons.Custom.LoopOn,
                    description = Messages.ICO_DSC_TOGGLE_CLIP_LOOP
                )
            }
        }
        Tooltip(
            language.messages.setClipEnd,
            Shortcut.CLIP_END_NOW.symbol,
            Shortcut.CLIP_END_FINISH.symbol
        ) {
            IconButton(onClick = viewModel::onSetClipEndToNow) {
                CustomIcon(
                    icon = Icons.Custom.EndCircle,
                    description = Messages.ICO_DSC_SET_CLIP_END_NOW
                )
            }
        }
    }
}

@Composable
private fun ControlsForMiniScreen() {
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    MediaPlayerProgress(
        isWavy = mediaInfo.isResumed,
        progress = mediaInfo.progress,
        onSeek = viewModel::setSeek
    )
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().offset(y = (-8).dp)
    ) {
        PlayPauseButton(iconPadding = 0.dp)
        AudioInput(
            viewModel = viewModel,
            iconPadding = 0.dp,
            mainModifier = Modifier,
            sliderModifier = Modifier.width(100.dp).offset(x = (-8).dp)
        )
        PinButton(iconPadding = 0.dp)
        Tooltip(
            LocalLanguage.current.messages.switchToNormalMode,
            Shortcut.MINI_MODE_TOGGLE.symbol
        ) {
            IconButton(onClick = viewModel::toggleMiniScreen) {
                Icon(
                    imageVector = Icons.Custom.RegularScreen,
                    contentDescription = Messages.ICO_DSC_ENTER_REGULAR_SCREEN,
                    modifier = Modifier.size(defaultIconSize)
                )
            }
        }
    }
}

@Composable
private fun PlayPauseButton(iconPadding: Dp) {
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    Tooltip(
        if (mediaInfo.isResumed) {
            LocalLanguage.current.messages.pauseMediaPlayback
        } else {
            LocalLanguage.current.messages.resumeMediaPlayback
        },
        Shortcut.PLAY_PAUSE.symbol
    ) {
        IconButton(onClick = viewModel::toggleResume) {
            Icon(
                imageVector = if (mediaInfo.isResumed) Icons.Custom.Pause else Icons.Custom.Play,
                contentDescription = Messages.ICO_DSC_PLAY_PAUSE,
                modifier = Modifier.padding(iconPadding).size(defaultIconSize + 2.dp)
            )
        }
    }
}

@Composable
private fun PinButton(iconPadding: Dp) {
    val isAlwaysOnTop by viewModel.isAlwaysOnTop.collectAsState()
    Tooltip(
        if (isAlwaysOnTop) {
            LocalLanguage.current.messages.unPinAppWindow
        } else {
            LocalLanguage.current.messages.pinAppWindow
        },
        Shortcut.PIN_TOGGLE.symbol
    ) {
        IconButton(onClick = viewModel::toggleIsAlwaysOnTop) {
            Icon(
                imageVector = if (isAlwaysOnTop) Icons.Custom.PinOn else Icons.Custom.PinOff,
                contentDescription = Messages.ICO_DSC_TOGGLE_ALWAYS_ON_TOP,
                modifier = Modifier.padding(iconPadding).size(defaultIconSize)
            )
        }
    }
}
