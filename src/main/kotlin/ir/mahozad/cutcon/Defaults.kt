@file:Suppress("MayBeConstant")

package ir.mahozad.cutcon

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahozad.cutcon.component.DefaultDurationConverter
import ir.mahozad.cutcon.component.SystemDateTime
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.localization.LanguageEn
import ir.mahozad.cutcon.model.*
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

const val DISPLAY_WIDTH = 600  // 16:9 aspect ratio
const val DISPLAY_HEIGHT = 338 // 16:9 aspect ratio
const val DISPLAY_WIDTH_MINI = 356  // 16:9 aspect ratio
const val DISPLAY_HEIGHT_MINI = 200 // 16:9 aspect ratio
const val WINDOW_WIDTH_WITH_PANEL = DISPLAY_WIDTH + 384
const val WINDOW_WIDTH_NO_PANEL = DISPLAY_WIDTH + 16
const val WINDOW_HEIGHT_REGULAR = DISPLAY_HEIGHT + (4 /* Rows of components */ * 48) + 34
const val WINDOW_WIDTH_MINI = 372
const val WINDOW_HEIGHT_MINI = 314

val defaultAudioImage by lazy {
    decodeImage(path = assetsPath / "cover.svg", mimeType = "image/svg+xml")
}

/**
 * Because VLC has a problem that finishes the media when seeking to 1.0f,
 * the seek fraction is a few seconds (aka [liveSeekSafeMargin]) before the media end.
 */
val liveSeekFraction: Float get() {
    val now = SystemDateTime.nowTime()
    val nowDuration = now.minute.minutes + now.second.seconds
    val safeMarginFraction = liveSeekSafeMargin / nowDuration
    return 1 - safeMarginFraction.toFloat().coerceIn(0f..1f)
}
val liveSeekSafeMargin = 5.seconds
val defaultSource by lazy { Source.Local(assetsPath / "cover.svg") }
val defaultTimeStamp = Duration.ZERO
val defaultClip = Clip(defaultTimeStamp, defaultTimeStamp)
val defaultFormat = Format.MP4
val defaultQuality = Quality.MEDIUM
val defaultLanguage: Language = LanguageEn
val defaultCalendar = Calendar.GREGORIAN
val defaultTheme = Theme.LIGHT
val defaultTooltipDelay = 1.seconds
val defaultDateTimeCheckingPeriod = 3.seconds
val defaultFontSize = 13.sp
val defaultIconSize = 24.dp
val defaultChipHeight = 34.dp
val defaultInputHeight = 36.dp
val defaultIconColor = Color.Black
val defaultIsFullscreen = false
val defaultIsMiniScreen = false
val defaultIsAlwaysOnTop = false
val defaultIsSidePanelDisplayed = true
val defaultIsFinishSoundEnabled = true
val defaultIsScreenshotSoundEnabled = true
val defaultIsInterlacedFixEnabled = true
val defaultSidePanelDisplayedTab = 0
val defaultSaveFilePath: Path? = null
val defaultOutputFrameRate = 25
val defaultDurationConverter = DefaultDurationConverter
val defaultTimeStampString get() = defaultDurationConverter.format(defaultTimeStamp, numberOfParts = 1)
val defaultScreenshotSaveDirectory = (System.getProperty("user.home") ?: error("Could not get user home directory"))
    .let(::Path) / "Pictures" / "Screenshots" / BuildConfig.APP_NAME
val defaultAspectRatio = AspectRatio.W16H9
val defaultCoverOptions = CoverOptions(
    path = null,
    scale = 1f,
    opacity = 1f,
    position = WatermarkPosition.CENTER
)
val defaultIntroOptions = IntroOptions(
    path = null,
    duration = 1.seconds,
    backgroundColor = Color.Black
)
val defaultIsResumed = true
val defaultAudioVolume = 1f
val defaultIsAudioMuted = false
val defaultMediaUrl = URL("file://")
val defaultSeek = 0f
val defaultSpeed = Speed.NORMAL
val defaultFastSpeed = Speed.FAST2_0
val defaultClipToLoop: Clip? = null

val LocalLanguage = compositionLocalOf { defaultLanguage }
val LocalCalendar = compositionLocalOf { defaultCalendar }
