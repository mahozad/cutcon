package ir.mahozad.cutcon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.component.*
import ir.mahozad.cutcon.component.MediaPlayer
import ir.mahozad.cutcon.converter.ConverterFactory
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.model.*
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_ASPECT_RATIO
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_CALENDAR
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_FINISH_SOUND
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_INTERLACED_FIX
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_LANGUAGE
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_LAST_OPEN_DIRECTORY
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_LAST_SAVE_DIRECTORY
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_LAST_SHOWN_CHANGELOG_VERSION
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_SCREENSHOT_SOUND
import ir.mahozad.cutcon.model.PreferenceKeys.PREF_THEME
import ir.mahozad.cutcon.ui.widget.COVER_PREVIEW_SIZE
import ir.mahozad.cutcon.ui.widget.INTRO_PREVIEW_SIZE
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime
import java.util.prefs.Preferences
import javax.sound.sampled.AudioSystem
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MainViewModel(
    dispatcher: CoroutineContext,
    private val urlMaker: UrlMaker,
    private val mediaPlayer: MediaPlayer,
    private val dateTimeChecker: DateTimeChecker,
    private val converterFactory: ConverterFactory,
    private val saveFileNameGenerator: SaveFileNameGenerator,
    private val settings: Preferences
) {

    private sealed interface ConversionStatus {
        data object None : ConversionStatus
        data object Initializing : ConversionStatus
        data class InProgress(val progress: Float) : ConversionStatus
        data class Success(val totalTime: Duration) : ConversionStatus
        data class Failure(val throwable: Throwable) : ConversionStatus
    }

    data class WindowWidth(val value: Int, val isAnimated: Boolean)

    private val logger = logger(name = MainViewModel::class.simpleName ?: "")
    private val coroutineScope = CoroutineScope(dispatcher)
    private var conversionJob: Job? = null
    private val _isAppExitConfirmDialogDisplayed = MutableStateFlow(false)
    private val _isChangelogDialogDisplayed = MutableStateFlow(
        // settings[PREF_LAST_SHOWN_CHANGELOG_VERSION, null].let {
        //     compareVersionStrings(BuildConfig.APP_VERSION, it) == VersionComparisonResult.NEWER
        // }
        false
    )
    private val _language = MutableStateFlow(
        settings[PREF_LANGUAGE, null]
            ?.let(Language::fromTag)
            ?: defaultLanguage
    )
    private val _calendar = MutableStateFlow(
        settings[PREF_CALENDAR, null]
            ?.let(Calendar::valueOf)
            ?: defaultCalendar
    )
    private val _theme = MutableStateFlow(
        settings[PREF_THEME, null]
            ?.let(Theme::valueOf)
            ?: defaultTheme
    )
    private val _isFinishSoundEnabled = MutableStateFlow(
        settings[PREF_FINISH_SOUND, null]
            ?.let(String::toBoolean)
            ?: defaultIsFinishSoundEnabled
    )
    private val _aspectRatio = MutableStateFlow(
        settings[PREF_ASPECT_RATIO, null]
            ?.let(AspectRatio::valueOf)
            ?: defaultAspectRatio
    )
    private val _isScreenshotSoundEnabled = MutableStateFlow(
        settings[PREF_SCREENSHOT_SOUND, null]
            ?.let(String::toBoolean)
            ?: defaultIsScreenshotSoundEnabled
    )
    private val _isInterlacedFixEnabled = MutableStateFlow(
        settings[PREF_INTERLACED_FIX, null]
            ?.let(String::toBoolean)
            ?: defaultIsInterlacedFixEnabled
    )
    private val _mediaInfo = MutableStateFlow(
        MediaInfo(
            url = defaultMediaUrl,
            speed = defaultSpeed,
            progress = Progress.ZERO,
            isResumed = defaultIsResumed,
            clipToLoop = defaultClipToLoop,
            audioVolume = defaultAudioVolume,
            isAudioMuted = defaultIsAudioMuted
        )
    )
    private val clipSource = MutableStateFlow<Source>(defaultSource)
    private val _isLoopToggleable = MutableStateFlow(false)
    private val _source = MutableStateFlow<Source>(defaultSource)
    private val _quality = MutableStateFlow(defaultQuality)
    private val _clip = MutableStateFlow(defaultClip)
    private val _windowWidth = MutableStateFlow(WindowWidth(WINDOW_WIDTH_WITH_PANEL, isAnimated = true))
    private val _isFullscreen = MutableStateFlow(defaultIsFullscreen)
    private val _isMiniScreen = MutableStateFlow(defaultIsMiniScreen)
    private val _isSidePanelDisplayed = MutableStateFlow(defaultIsSidePanelDisplayed)
    private val _sidePanelSelectedTabIndex = MutableStateFlow(defaultSidePanelDisplayedTab)
    private var _format = MutableStateFlow(defaultFormat)
    private val _isAlwaysOnTop = MutableStateFlow(defaultIsAlwaysOnTop)
    private val _saveFile = MutableStateFlow(defaultSaveFilePath)
    private var _lastOpenDirectory = MutableStateFlow(
        settings[PREF_LAST_OPEN_DIRECTORY, null]
            ?.let(::Path)
            ?.takeIf(Path::exists)
    )
    private var _lastSaveDirectory = MutableStateFlow(
        settings[PREF_LAST_SAVE_DIRECTORY, null]
            ?.let(::Path)
            ?.takeIf(Path::exists)
    )
    private var lastAspectRatio = _aspectRatio.value
    private var lastNonDefaultSpeed = defaultFastSpeed
    private val _coverOptions = MutableStateFlow(defaultCoverOptions)
    private val _introOptions = MutableStateFlow(defaultIntroOptions)
    private val _coverBitmap = MutableStateFlow<ImageBitmap?>(null)
    private val _introBitmap = MutableStateFlow<ImageBitmap?>(null)
    private val _isScreenshotInputActive = MutableStateFlow(false)
    private var wasSidePanelDisplayedBeforeChangingScreen = _isSidePanelDisplayed.value
    private var wasMiniScreen = _isMiniScreen.value
    /**
     * See https://stackoverflow.com/q/10123735
     *
     * Another way which did not take into account the taskbar size:
     * ```kotlin
     * val screenSize = Toolkit.getDefaultToolkit().screenSize
     * ```
     */
    private val screenSize = GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .runCatching { maximumWindowBounds } // Necessary to avoid HeadlessException in @Previews
        .onFailure { logger.error(it) { "Could not get screen size. Maybe the environment is headless..." } }
        .getOrElse {
            logger.error { "Returning 800x600 as screen size" }
            Rectangle(800, 600)
        }
    private val windowUserDraggedPosition: MutableStateFlow<WindowPosition> = MutableStateFlow(
        WindowPosition(
            x = ((screenSize.width - WINDOW_WIDTH_WITH_PANEL) / 2).dp,
            y = ((screenSize.height - WINDOW_HEIGHT_REGULAR) / 2).dp
        )
    )
    private val _conversion = MutableStateFlow<ConversionStatus>(ConversionStatus.None)
    private var _clipStartMinuteInput = MutableStateFlow(TextFieldValue(text = defaultTimeStampString))
    private var _clipStartSecondInput = MutableStateFlow(TextFieldValue(text = defaultTimeStampString))
    private var _clipEndMinuteInput = MutableStateFlow(TextFieldValue(text = defaultTimeStampString))
    private var _clipEndSecondInput = MutableStateFlow(TextFieldValue(text = defaultTimeStampString))
    // NOTE: Do not use .flowOn(dispatcher); It degrades UI performance
    val displayImage = mediaPlayer.output.transform {
        if (it is MediaPlayer.Output.SourceNotStarted) {
            emit(null)
            // To give the transition animation of Display widget **at least** this much time
            delay(500.milliseconds)
        } else if (it is MediaPlayer.Output.Video) {
            emitAll(it.video)
        } else if (it is MediaPlayer.Output.Image) {
            emit(it.image)
        } else if (_source.value.mediaType == Source.MediaType.AUDIO) {
            emit(defaultMusicCoverArt)
        }
    }
    val language = _language.asStateFlow()
    val calendar = _calendar.asStateFlow()
    val theme = _theme.asStateFlow()
    val aspectRatio = _aspectRatio.asStateFlow()
    val clipStartMinuteInput = combine(_clipStartMinuteInput, _language) { input, language ->
        input.copy(text = language.localizeDigits(input.text))
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = TextFieldValue(text = language.value.localizeDigits(defaultTimeStampString))
    )
    val clipStartSecondInput = combine(_clipStartSecondInput, _language) { input, language ->
        input.copy(text = language.localizeDigits(input.text))
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = TextFieldValue(text = language.value.localizeDigits(defaultTimeStampString))
    )
    val clipEndMinuteInput = combine(_clipEndMinuteInput, _language) { input, language ->
        input.copy(text = language.localizeDigits(input.text))
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = TextFieldValue(text = language.value.localizeDigits(defaultTimeStampString))
    )
    val clipEndSecondInput = combine(_clipEndSecondInput, _language) { input, language ->
        input.copy(text = language.localizeDigits(input.text))
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = TextFieldValue(text = language.value.localizeDigits(defaultTimeStampString))
    )
    val source = _source.asStateFlow()
    val isScreenshotInputEnabled = _source
        .map { it.mediaType == Source.MediaType.VIDEO }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = _source.value.mediaType == Source.MediaType.VIDEO
        )
    val isScreenshotInputActive = _isScreenshotInputActive.asStateFlow()
    val isQualityInputApplicable = _format
        .map { it != Format.RAW }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = _format.value != Format.RAW
        )
    // NOTE: The following two properties should be reacted upon only in a single place for displaying the dialogs.
    // See MainWindow file -> Dialogs function for more information.
    val isAppExitConfirmDialogDisplayed = _isAppExitConfirmDialogDisplayed.asStateFlow()
    val isChangelogDialogDisplayed = _isChangelogDialogDisplayed.asStateFlow()
    val isScreenshotSoundEnabled = _isScreenshotSoundEnabled.asStateFlow()
    val isFinishSoundEnabled = _isFinishSoundEnabled.asStateFlow()
    val isInterlacedFixEnabled = _isInterlacedFixEnabled.asStateFlow()
    val quality = _quality.asStateFlow()
    val clip = _clip.asStateFlow()
    val format = _format.asStateFlow()
    val isFullscreen = _isFullscreen.asStateFlow()
    val isMiniScreen = _isMiniScreen.asStateFlow()
    val isSidePanelDisplayed = _isSidePanelDisplayed.asStateFlow()
    val saveFile = _saveFile.asStateFlow()
    val isAlwaysOnTop = _isAlwaysOnTop.asStateFlow()
    val sidePanelSelectedTabIndex = _sidePanelSelectedTabIndex.asStateFlow()
    val lastOpenDirectory = _lastOpenDirectory.asStateFlow()
    val lastSaveDirectory = _lastSaveDirectory.asStateFlow()
    val coverOptions = _coverOptions.asStateFlow()
    val introOptions = _introOptions.asStateFlow()
    val coverBitmap = _coverBitmap.asStateFlow()
    val introBitmap = _introBitmap.asStateFlow()
    val mediaInfo = _mediaInfo.asStateFlow()
    val isLoopToggleable = _isLoopToggleable.asStateFlow()

    /*
     * For adaptive sizing of window (wrap content or fit content)
     * see https://github.com/JetBrains/compose-multiplatform/issues/986
     */
    val windowPosition = combine(_isSidePanelDisplayed, _isMiniScreen) { _, isMiniScreen ->
        if (isMiniScreen && !wasMiniScreen) {
            wasMiniScreen = true
            WindowPosition(
                x = (screenSize.width - WINDOW_WIDTH_MINI).dp,
                y = (screenSize.height - WINDOW_HEIGHT_MINI).dp
            )
        } else if (!isMiniScreen && wasMiniScreen) {
            wasMiniScreen = false
            val appWidth = if (wasSidePanelDisplayedBeforeChangingScreen) {
                WINDOW_WIDTH_WITH_PANEL
            } else {
                WINDOW_WIDTH_NO_PANEL
            }
            WindowPosition(
                x = ((screenSize.width - appWidth) / 2).dp,
                y = ((screenSize.height - WINDOW_HEIGHT_REGULAR) / 2).dp
            ).also { windowUserDraggedPosition.value = it }
        } else {
            windowUserDraggedPosition.value
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = windowUserDraggedPosition.value
    )
    val windowWidth = _windowWidth.asStateFlow()
    val windowHeight = _isMiniScreen
        .map { if (it) WINDOW_HEIGHT_MINI else WINDOW_HEIGHT_REGULAR }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = WINDOW_HEIGHT_REGULAR
        )

    val status = combine(
        _source,
        _mediaInfo,
        _clip,
        _saveFile,
        _conversion
    ) { source, mediaInfo, clip, saveFile, conversion ->
        if (conversion is ConversionStatus.Initializing) {
            Status.Initializing
        } else if (conversion is ConversionStatus.InProgress) {
            Status.InProgress(clipSource.value, conversion.progress)
        } else if (conversion is ConversionStatus.Failure) {
            Status.Finished.Failure(conversion.throwable)
        } else if (conversion is ConversionStatus.Success) {
            Status.Finished.Success(conversion.totalTime)
        } else if (source.mediaType == Source.MediaType.IMAGE) {
            Status.Error.ClipFromImageNotSupported
        } else if (source.mediaType !in setOf(Source.MediaType.VIDEO, Source.MediaType.AUDIO)) {
            Status.Error.ClipFromFormatNotSupported
        } else if (clip.start == ZERO && clip.end == ZERO) {
            Status.Error.ClipNotSet
        } else if (clip.start >= mediaInfo.progress.length && mediaInfo.progress.length > ZERO) {
            Status.Error.ClipStartAfterMediaEnd
        } else if (clip.duration == ZERO) {
            Status.Error.ClipLengthZero
        } else if (clip.duration < ZERO) {
            Status.Error.ClipLengthNegative
        } else if (saveFile == null) {
            Status.Error.FileNotSet
        } else {
            Status.Ready
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = Status.Error.ClipNotSet
    )

    init {
        mediaPlayer
            .isResumed
            .onEach { isResumed -> _mediaInfo.update { it.copy(isResumed = isResumed) } }
            .launchIn(coroutineScope)
        _lastSaveDirectory
            .filterNotNull()
            .conflate()
            .onEach { settings.put(PREF_LAST_SAVE_DIRECTORY, it.absolutePathString()) }
            .launchIn(coroutineScope)
        _lastOpenDirectory
            .filterNotNull()
            .conflate()
            .onEach { settings.put(PREF_LAST_OPEN_DIRECTORY, it.absolutePathString()) }
            .launchIn(coroutineScope)
    }

    fun startUrlMaker() {
        _source
            .map(urlMaker::makeUrl)
            .distinctUntilChanged()
            .onEach(mediaPlayer::play)
            .onEach { mediaPlayer.resume() }
            .onEach { url -> _mediaInfo.update { it.copy(url = url) } }
            .launchIn(coroutineScope)
    }

    fun startMediaProgressListener() {
        mediaPlayer
            .progress
            .onEach { p -> _mediaInfo.update { it.copy(progress = p) } }
            .onEach {
                _clip.update {
                    val input = generateNewTimestamp(_clipEndMinuteInput.value.text, _clipEndSecondInput.value.text)
                    val end = input?.coerceAtMost(_mediaInfo.value.progress.length) ?: it.end
                    it.copy(end = end)
                }
            }
            .launchIn(coroutineScope)
    }

    fun startDateTimeChecker() {
        dateTimeChecker
            .dateTimeFlow()
            // .onEach { (date, _) -> currentDate.value = date }
            // .onEach { (_, time) -> currentTime.value = time }
            .launchIn(coroutineScope)
    }

    private fun updateWindowWidth(shouldAnimate: Boolean) {
        _windowWidth.value = if (_isMiniScreen.value) {
            WindowWidth(WINDOW_WIDTH_MINI, isAnimated = false)
        } else if (_isSidePanelDisplayed.value) {
            WindowWidth(WINDOW_WIDTH_WITH_PANEL, shouldAnimate)
        } else {
            WindowWidth(WINDOW_WIDTH_NO_PANEL, shouldAnimate)
        }
    }

    fun setLanguage(language: Language) {
        _language.value = language
        settings.put(PREF_LANGUAGE, language.tag)
    }

    fun onClipStartMinuteChanged(string: String, selection: TextRange) {
        _clipStartMinuteInput.value = handleInputForTimeMinute(_clipStartMinuteInput.value.text, string, selection)
        val newValue = generateNewTimestamp(_clipStartMinuteInput.value.text, _clipStartSecondInput.value.text)
        setClipStart(newValue)
    }

    fun onClipStartSecondChanged(string: String, selection: TextRange) {
        _clipStartSecondInput.value = handleInputForTimeSecond(_clipStartSecondInput.value.text, string, selection)
        val newValue = generateNewTimestamp(_clipStartMinuteInput.value.text, _clipStartSecondInput.value.text)
        setClipStart(newValue)
    }

    fun onClipEndMinuteChanged(string: String, selection: TextRange) {
        _clipEndMinuteInput.value = handleInputForTimeMinute(_clipEndMinuteInput.value.text, string, selection)
        val newValue = generateNewTimestamp(_clipEndMinuteInput.value.text, _clipEndSecondInput.value.text)
        setClipEnd(newValue)
    }

    fun onClipEndSecondChanged(string: String, selection: TextRange) {
        _clipEndSecondInput.value = handleInputForTimeSecond(_clipEndSecondInput.value.text, string, selection)
        val newValue = generateNewTimestamp(_clipEndMinuteInput.value.text, _clipEndSecondInput.value.text)
        setClipEnd(newValue)
    }

    fun onSetClipStartToNow() = setClipStartTo(_mediaInfo.value.progress.time)

    fun onSetClipEndToNow() = setClipEndTo(_mediaInfo.value.progress.time)

    private fun setClipStartTo(time: Duration) {
        val (minute, second) = defaultDurationConverter
            .format(duration = time, numberOfParts = 2)
            .split(':')
        _clipStartMinuteInput.value = TextFieldValue(text = minute)
        _clipStartSecondInput.value = TextFieldValue(text = second)
        setClipStart(time)
    }

    private fun setClipEndTo(time: Duration) {
        val (minute, second) = defaultDurationConverter
            .format(duration = time, numberOfParts = 2)
            .split(':')
        _clipEndMinuteInput.value = TextFieldValue(text = minute)
        _clipEndSecondInput.value = TextFieldValue(text = second)
        setClipEnd(time)
    }

    private fun generateNewTimestamp(minute: String, second: String): Duration? {
        val minuteString = minute.takeIf(String::isNotEmpty) ?: defaultTimeStampString
        val secondString = second.takeIf(String::isNotEmpty) ?: defaultTimeStampString
        return defaultDurationConverter.parse("$minuteString:$secondString")
    }

    fun toggleIsAlwaysOnTop() {
        _isAlwaysOnTop.value = !_isAlwaysOnTop.value
    }

    fun onKeyboardEvent(event: KeyEvent) {
        if (event.type == KeyEventType.KeyDown) {
            when (event.key) {
                in Shortcut.SEEK_SHORT_BACKWARD.keys  ->
                    setSeek(((_mediaInfo.value.progress.time - 5.seconds) / _mediaInfo.value.progress.length).toFloat())
                in Shortcut.SEEK_SHORT_FORWARD.keys   ->
                    setSeek(((_mediaInfo.value.progress.time + 5.seconds) / _mediaInfo.value.progress.length).toFloat())
                in Shortcut.SEEK_LONG_BACKWARD.keys   ->
                    setSeek(((_mediaInfo.value.progress.time - 30.seconds) / _mediaInfo.value.progress.length).toFloat())
                in Shortcut.SEEK_LONG_FORWARD.keys    ->
                    setSeek(((_mediaInfo.value.progress.time + 30.seconds) / _mediaInfo.value.progress.length).toFloat())
                in Shortcut.AUDIO_DECREASE.keys       -> setVolume((_mediaInfo.value.audioVolume - 0.1f).coerceAtLeast(0f))
                in Shortcut.AUDIO_INCREASE.keys       -> setVolume((_mediaInfo.value.audioVolume + 0.1f).coerceAtMost(1f))
                in Shortcut.PLAY_PAUSE.keys           -> toggleResume()
                in Shortcut.FULLSCREEN_EXIT.keys      -> exitFullscreen()
                in Shortcut.SPEED_DECREASE.keys       -> setSpeed(_mediaInfo.value.speed.dec())
                in Shortcut.SPEED_INCREASE.keys       -> setSpeed(_mediaInfo.value.speed.inc())
                in Shortcut.SPEED_RESET.keys          -> resetSpeed()
                in Shortcut.CLIP_START_NOW.keys       -> onSetClipStartToNow()
                in Shortcut.CLIP_END_NOW.keys         -> onSetClipEndToNow()
                in Shortcut.CLIP_START_BEGINNING.keys -> setClipStartTo(ZERO)
                in Shortcut.CLIP_END_FINISH.keys      -> setClipEndTo(_mediaInfo.value.progress.length)
                in Shortcut.SIDE_PANEL_TOGGLE.keys    -> toggleSidePanel()
                in Shortcut.MINI_MODE_TOGGLE.keys     -> toggleMiniScreen()
                in Shortcut.FULLSCREEN_TOGGLE.keys    -> if (_isFullscreen.value) exitFullscreen() else enterFullscreen()
                in Shortcut.CLIP_LOOP_TOGGLE.keys     -> if (_isLoopToggleable.value) toggleClipLoop()
                in Shortcut.PIN_TOGGLE.keys           -> toggleIsAlwaysOnTop()
                in Shortcut.AUDIO_MUTE_TOGGLE.keys    -> toggleAudioMute()
                in Shortcut.SCREENSHOT_TAKE.keys      -> takeScreenshot()
            }
        }
    }

    fun setSaveFile(file: Path) {
        val extension = _format.value.extension(_source.value)
        if (file.name.endsWith(".$extension", ignoreCase = true)) {
            _saveFile.value = file.parent / "${file.nameWithoutExtension}.$extension"
        } else {
            _saveFile.value = file.parent / "${file.name}.$extension"
        }
        _lastSaveDirectory.value = file.parent
    }

    fun setQuality(newQuality: Float) {
        _quality.value = newQuality.toQuality()
    }

    fun setFormat(newFormat: Format) {
        _format.value = newFormat
        updateSaveFileName()
    }

    fun setIntroFile(path: Path?) {
        coroutineScope.launch {
            _lastOpenDirectory.update { path?.parent ?: it }
            val newPath = convertImageToSupportedFormatIfNeeded(path)
            _introOptions.update { it.copy(path = newPath) }
            _introBitmap.value = getImageBitmap(path, INTRO_PREVIEW_SIZE) {
                logger.warn { "Could not create intro preview for $path" }
            }
        }
    }

    fun setCoverFile(path: Path?) {
        coroutineScope.launch {
            _lastOpenDirectory.update { path?.parent ?: it }
            val newPath = convertImageToSupportedFormatIfNeeded(path)
            _coverOptions.update { it.copy(path = newPath) }
            _coverBitmap.value = getImageBitmap(path, COVER_PREVIEW_SIZE) {
                logger.warn { "Could not create watermark/album art preview for $path" }
            }
        }
    }

    private fun convertImageToSupportedFormatIfNeeded(path: Path?): Path? {
        val mimeType = path?.detectMimeType()
        return if (mimeType == "image/svg+xml") {
            convertSvgToPng(path)
        } else if (mimeType?.startsWith("image/") == true) {
            path
        } else {
            null
        }
    }

    private fun getImageBitmap(
        path: Path?,
        desiredSizeIfVector: Float,
        onFailure: () -> Unit
    ): ImageBitmap? {
        if (path == null) return null
        val result = decodeImage(path, desiredSizeIfVector)
        if (result == null) onFailure()
        return result
    }

    fun setIntroBackgroundColor(color: Color) {
        _introOptions.update { it.copy(backgroundColor = color) }
    }

    fun setIntroDuration(duration: Duration) {
        _introOptions.update { it.copy(duration = duration) }
    }

    fun setWaterMarkScale(scale: Float) {
        _coverOptions.update { it.copy(scale = scale) }
    }

    fun setWaterMarkOpacity(opacity: Float) {
        _coverOptions.update { it.copy(opacity = opacity) }
    }

    fun setWatermarkPosition(position: WatermarkPosition) {
        _coverOptions.update { it.copy(position = position) }
    }

    private fun setClipStart(value: Duration?) {
        _clip.update { it.copy(start = value ?: defaultTimeStamp) }
        _isLoopToggleable.value = _clip.value.duration > ZERO
        _mediaInfo.update { it.copy(clipToLoop = null) }
        mediaPlayer.setClipToLoop(null)
        logger.info { "Clip start was set to ${_clip.value.start}" }
    }

    private fun setClipEnd(value: Duration?) {
        _clip.update { it.copy(end = value?.coerceAtMost(_mediaInfo.value.progress.length) ?: defaultTimeStamp) }
        _isLoopToggleable.value = _clip.value.duration > ZERO
        _mediaInfo.update { it.copy(clipToLoop = null) }
        mediaPlayer.setClipToLoop(null)
        logger.info { "Clip end was set to ${_clip.value.end}" }
    }

    fun startProcess() {
        conversionJob = coroutineScope.launch {
            val conversionStartTime = SystemDateTime.nowMillis()
            clipSource.value = _source.value
            logger.info { "Started creating ${clip.value} from ${_source.value}..." }
            _conversion.value = ConversionStatus.Initializing
            val converter = converterFactory.createFor(_format.value)
            _conversion.value = ConversionStatus.InProgress(progress = 0f)
            converter.runCatching {
                convert(
                    input = urlMaker.makeUrl(_source.value),
                    clip = _clip.value,
                    intro = _introOptions.value,
                    cover = _coverOptions.value,
                    quality = _quality.value,
                    fixInterlaced = _isInterlacedFixEnabled.value,
                    output = _saveFile.value!!,
                    listener = { _conversion.value = ConversionStatus.InProgress(progress = it) }
                )
                (SystemDateTime.nowMillis() - conversionStartTime).milliseconds
            }
                .onSuccess(::onConversionSuccess)
                .onFailure(::onConversionException)
        }
    }

    private fun onConversionSuccess(conversionDuration: Duration) {
        logger.info { "Conversion finished successfully in $conversionDuration" }
        _conversion.value = ConversionStatus.Success(totalTime = conversionDuration)
        resetState()
    }

    /**
     * Including CancellationException (conversion job cancel)
     */
    private fun onConversionException(throwable: Throwable) {
        if (throwable is CancellationException) {
            logger.info { "Conversion canceled successfully" }
            _conversion.value = ConversionStatus.None
        } else {
            logger.error(throwable) { "Conversion failed" }
            _conversion.value = ConversionStatus.Failure(throwable = throwable)
        }
        resetState()
    }

    fun cancelProcess() {
        logger.info { "Cancelling the conversion..." }
        conversionJob?.cancel()
    }

    private fun resetState() {
        _saveFile.value = null
    }

    fun cancelEverything() {
        // Cancels everything launched in this scope
        // (including the conversion job)
        coroutineScope.cancel()
        mediaPlayer.terminate()
    }

    fun toggleSidePanel() {
        if (!_isFullscreen.value && !_isMiniScreen.value) {
            _isSidePanelDisplayed.value = !_isSidePanelDisplayed.value
            updateWindowWidth(shouldAnimate = true)
        }
    }

    fun enterFullscreen() {
        wasSidePanelDisplayedBeforeChangingScreen = _isSidePanelDisplayed.value
        _isSidePanelDisplayed.value = false
        _isFullscreen.value = true
    }

    fun exitFullscreen() {
        _isFullscreen.value = false
        _isSidePanelDisplayed.value = wasSidePanelDisplayedBeforeChangingScreen
    }

    fun toggleMiniScreen() {
        if (_isFullscreen.value) return
        if (!_isMiniScreen.value) {
            wasSidePanelDisplayedBeforeChangingScreen = _isSidePanelDisplayed.value
            _isSidePanelDisplayed.value = false
        }
        _isMiniScreen.update { !it }
        if (!_isMiniScreen.value) {
            _isSidePanelDisplayed.value = wasSidePanelDisplayedBeforeChangingScreen
        }
        updateWindowWidth(shouldAnimate = false)
    }

    fun takeScreenshot() {
        if (!isScreenshotInputEnabled.value) {
            return
        }
        coroutineScope.launch {
            _isScreenshotInputActive.value = true
            delay(400.milliseconds)
            _isScreenshotInputActive.value = false
        }
        // Does it asynchronously because creating directories takes a little time
        coroutineScope.launch {
            // Makes sure the directories exist.
            // Calling this method where the variable is defined is not enough
            // because, although it creates the directories on app start,
            // user may have deleted the directories while the app is running
            defaultScreenshotSaveDirectory.createDirectories()
            val wasSuccessful = mediaPlayer.takeScreenshot(defaultScreenshotSaveDirectory.toFile())
            logger.info { "Saving screenshot ${if (wasSuccessful) "succeeded" else "failed"}" }
        }
        coroutineScope.launch {
            if (_isScreenshotSoundEnabled.value) {
                val soundPath = assetsPath / "shutter.wav"
                val audioStream = AudioSystem.getAudioInputStream(soundPath.toFile())
                val audioClip = AudioSystem.getClip()
                audioClip.open(audioStream)
                audioClip.start()
            }
        }
    }

    fun toggleResume() {
        mediaPlayer.toggleResume()
    }

    fun setSeek(seek: Float) {
        val fraction = if (_mediaInfo.value.clipToLoop != null) {
            val startFraction = (_clip.value.start / _mediaInfo.value.progress.length).toFloat()
            val endFraction = (_clip.value.end / _mediaInfo.value.progress.length).toFloat()
            seek.coerceIn(startFraction, endFraction)
        } else {
            seek
        }
        mediaPlayer.seek(fraction)
    }

    fun setVolume(volume: Float) {
        mediaPlayer.setAudioVolume(volume.coerceIn(0f..1f))
        _mediaInfo.update { it.copy(audioVolume = volume.coerceIn(0f..1f)) }
    }

    fun setSpeed(speed: Speed) {
        mediaPlayer.setSpeed(speed.value)
        _mediaInfo.update { it.copy(speed = speed) }
        lastNonDefaultSpeed = speed.takeIf { it != defaultSpeed } ?: lastNonDefaultSpeed
    }

    fun resetSpeed() {
        if (_mediaInfo.value.speed != defaultSpeed) {
            _mediaInfo.update { it.copy(speed = defaultSpeed) }
        } else {
            _mediaInfo.update { it.copy(speed = lastNonDefaultSpeed) }
        }
        mediaPlayer.setSpeed(_mediaInfo.value.speed.value)
    }

    fun setSidePanelSelectedTabIndex(tabIndex: Int) {
        _sidePanelSelectedTabIndex.value = tabIndex
    }

    fun generateSaveFileDefaultName(path: Path): String {
        return saveFileNameGenerator.generate(path, LocalDate.now(), LocalTime.now())
    }

    fun toggleAudioMute() {
        _mediaInfo.update { it.copy(isAudioMuted = !it.isAudioMuted) }
        if (_mediaInfo.value.isAudioMuted) mediaPlayer.mute() else mediaPlayer.unMute()
    }

    fun toggleClipLoop() {
        val fraction = (_clip.value.start / _mediaInfo.value.progress.length).toFloat()
        if (_mediaInfo.value.clipToLoop == null) mediaPlayer.seek(fraction)
        _mediaInfo.update { it.copy(clipToLoop = if (it.clipToLoop == null) _clip.value else null) }
        mediaPlayer.setClipToLoop(_mediaInfo.value.clipToLoop)
    }

    fun onWindowPositionChanged(position: WindowPosition) {
        windowUserDraggedPosition.value = position
    }

    fun showChangelogDialog() {
        _isChangelogDialogDisplayed.value = true
    }

    fun onFinishDialogDismissRequest() {
        _conversion.value = ConversionStatus.None
    }

    fun onChangelogDialogDismissRequest() {
        settings.put(PREF_LAST_SHOWN_CHANGELOG_VERSION, BuildConfig.APP_VERSION)
        _isChangelogDialogDisplayed.value = false
    }

    fun onAppExitConfirmDialogDismissRequest() {
        _isAppExitConfirmDialogDisplayed.value = false
    }

    fun setAspectRatio(aspectRatio: AspectRatio) {
        lastAspectRatio = aspectRatio
        _aspectRatio.value = aspectRatio
        settings.put(PREF_ASPECT_RATIO, aspectRatio.name)
    }

    fun setCalendar(calendar: Calendar) {
        _calendar.value = calendar
        settings.put(PREF_CALENDAR, calendar.name)
    }

    fun setTheme(theme: Theme) {
        _theme.value = theme
        settings.put(PREF_THEME, theme.name)
    }

    fun setIsFinishSoundEnabled(isEnabled: Boolean) {
        _isFinishSoundEnabled.value = isEnabled
        settings.put(PREF_FINISH_SOUND, isEnabled.toString())
    }

    fun setIsScreenshotSoundEnabled(isEnabled: Boolean) {
        _isScreenshotSoundEnabled.value = isEnabled
        settings.put(PREF_SCREENSHOT_SOUND, isEnabled.toString())
    }

    fun setIsInterlacedFixEnabled(isEnabled: Boolean) {
        _isInterlacedFixEnabled.value = isEnabled
        settings.put(PREF_INTERLACED_FIX, isEnabled.toString())
    }

    fun setSourceToLocal(path: Path) {
        _source.value = Source.Local(path)
        _lastOpenDirectory.value = path.parent
        onSourceChanged()
    }

    private fun onSourceChanged() {
        coroutineScope.launch {
            // The aspect ratio is changed with a little delay
            // so the existing display image is not distorted
            delay(300.milliseconds)
            if (_source.value.mediaType == Source.MediaType.VIDEO) {
                _aspectRatio.value = lastAspectRatio
            } else {
                _aspectRatio.value = AspectRatio.SOURCE
            }
        }
        updateSaveFileName()
    }

    private fun updateSaveFileName() {
        _saveFile.update {
            it
                ?.toString()
                ?.substringBeforeLast('.')
                ?.plus(".${_format.value.extension(_source.value)}")
                ?.let(::Path)
        }
    }

    fun onAppExitRequest(forceExit: Boolean, exit: () -> Unit) {
        val isIdle =
            _conversion.value !is ConversionStatus.Initializing &&
            _conversion.value !is ConversionStatus.InProgress
        if (isIdle || forceExit) {
            exit()
        } else {
            _isAppExitConfirmDialogDisplayed.value = true
        }
    }
}
