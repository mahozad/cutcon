@file:Suppress("MayBeConstant")

package ir.mahozad.cutcon

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.component.DefaultDurationConverter
import ir.mahozad.cutcon.component.SystemDateTime
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.localization.LanguageEn
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.*
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

const val DISPLAY_WIDTH = 600  // 16:9 aspect ratio
const val DISPLAY_HEIGHT = 338 // 16:9 aspect ratio
const val DISPLAY_WIDTH_MINI = 356  // 16:9 aspect ratio
const val DISPLAY_HEIGHT_MINI = 200 // 16:9 aspect ratio
const val SIDE_PANEL_WIDTH = 360
const val WINDOW_WIDTH_WITH_PANEL = DISPLAY_WIDTH + SIDE_PANEL_WIDTH + 3 * /* Paddings */ 8
const val WINDOW_WIDTH_NO_PANEL = DISPLAY_WIDTH + 2 * /* Paddings */ 8
const val WINDOW_HEIGHT_REGULAR = DISPLAY_HEIGHT + (4 /* Rows of components */ * 48) + 34
const val WINDOW_WIDTH_MINI = 372
const val WINDOW_HEIGHT_MINI = 314

private val logger = logger(name = "Defaults")

val assetsPath = System
    .getProperty("compose.application.resources.dir")
    ?.let(::Path)
    ?.also { logger.debug { "Custom assets path: $it" } }
    ?: error(Messages.ERR_COMPOSE_RES_DIR_NOT_SET)

val defaultMusicCoverArt by lazy { decodeImage(path = assetsPath / "music.svg") }

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
// A fake path is used because the source is non-null, and we don't want
// to show any file on app startup. Also, the path name below will be
// shown as the source input text when the app starts.
val defaultSource by lazy { Source.Local(Path("Example file")) }
val defaultTimeStamp = Duration.ZERO
val defaultClip = Clip(defaultTimeStamp, defaultTimeStamp)
val defaultFormat = Format.MP4
val defaultQuality = Quality.MEDIUM
val defaultLanguage: Language = LanguageEn
val defaultCalendar = Calendar.GREGORIAN
val defaultTheme = Theme.LIGHT
val defaultTooltipDelay = 600.milliseconds
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
val defaultAspectRatio = AspectRatio.SOURCE
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
val defaultMediaUrl = URI("file://localhost").toURL()
val defaultSpeed = Speed.NORMAL
val defaultFastSpeed = Speed.FAST2_0
val defaultClipToLoop: Clip? = null

val LocalLanguage = compositionLocalOf { defaultLanguage }
val LocalCalendar = compositionLocalOf { defaultCalendar }
