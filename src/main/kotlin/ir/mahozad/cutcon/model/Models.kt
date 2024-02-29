package ir.mahozad.cutcon.model

import androidx.compose.ui.graphics.Color
import com.github.mfathi91.time.PersianDate
import ir.mahozad.cutcon.detectMimeType
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.localization.LanguageFa
import java.net.URL
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.extension
import kotlin.time.Duration

interface Labeled {
    val label: (Language) -> String
}

data class Progress(val fraction: Float, val length: Duration) {
    val time get() = length * fraction.toDouble()
    companion object { val ZERO = Progress(0f, Duration.ZERO) }
}

enum class VersionComparisonResult { SAME, OLDER, NEWER }

sealed interface Source : Labeled {
    val mimeType: String?
    val mediaType: MediaType
    val formatName: String
    val fileExtension: String

    enum class MediaType { VIDEO, AUDIO, IMAGE, UNKNOWN }

    data class Local(val path: Path) : Source {
        override val label: (Language) -> String = { it.messages.txtLblSourceLocal }
        override val fileExtension = path.extension.lowercase()
        override val formatName = path.extension.uppercase()
        override val mimeType = path.detectMimeType()
        override val mediaType = when {
            mimeType?.startsWith("video/") == true -> MediaType.VIDEO
            mimeType?.startsWith("audio/") == true -> MediaType.AUDIO
            mimeType?.startsWith("image/") == true -> MediaType.IMAGE
            else -> MediaType.UNKNOWN
        }
    }
}

sealed interface Status {
    sealed interface Error : Status {
        data object ClipFromImageNotSupported : Error
        data object ClipFromFormatNotSupported : Error
        data object FileNotSet : Error
        data object ClipNotSet : Error
        data object ClipLengthZero : Error
        data object ClipLengthNegative : Error
        data object ClipStartAfterMediaEnd : Error
    }
    sealed interface Finished : Status {
        data class Success(val totalTime: Duration) : Finished
        data class Failure(val throwable: Throwable) : Finished
    }
    data object Ready : Status
    data object Initializing : Status
    data class InProgress(val source: Source, val progress: Float) : Status
}

enum class AspectRatio(
    val ratio: Float?,
    override val label: (Language) -> String
) : Labeled {
    W16H9(ratio = 16f / 9f, label = { it.messages.txtLblAspectRatio16To9 }),
    SOURCE(ratio = null, label = { it.messages.txtLblAspectRatioSource })
}

data class MediaInfo(
    val url: URL,
    val speed: Speed,
    val progress: Progress,
    val isResumed: Boolean,
    val audioVolume: Float,
    val isAudioMuted: Boolean,
    val clipToLoop: Clip?
)

enum class Quality(
    override val label: (Language) -> String,
    val value: Int
) : Labeled {
    LOWEST(label = { it.messages.txtLblQuality1 }, value = 1),
    LOW(label = { it.messages.txtLblQuality2 }, value = 2),
    MEDIUM(label = { it.messages.txtLblQuality3 }, value = 3),
    HIGH(label = { it.messages.txtLblQuality4 }, value = 4),
    HIGHEST(label = { it.messages.txtLblQuality5 }, value = 5)
}

enum class WatermarkPosition {
    TOP_LEFT, TOP_MIDDLE, TOP_RIGHT,
    CENTER_LEFT, CENTER, CENTER_RIGHT,
    BOTTOM_LEFT, BOTTOM_MIDDLE, BOTTOM_RIGHT
}

data class CoverOptions(
    val path: Path?,
    val scale: Float,
    val opacity: Float,
    val position: WatermarkPosition,
)

data class IntroOptions(
    val path: Path?,
    val backgroundColor: Color,
    val duration: Duration,
)

data class ConverterFlags(
    val isInterlacingFixEnabled: Boolean,
    val isVideoAvailableInInput: Boolean = true
)

data class Clip(
    val start: Duration,
    val end: Duration
) {
    val duration = end - start
}

enum class Speed(val value: Float) {
    SLOW0_5(0.5f),
    SLOW0_75(0.75f),
    NORMAL(1.0f),
    FAST1_5(1.5f),
    FAST2_0(2.0f),
    FAST2_5(2.5f),
    FAST3_0(3.0f);

    operator fun dec(): Speed {
        val i = (ordinal - 1).coerceAtLeast(0)
        return entries[i]
    }

    operator fun inc(): Speed {
        val i = (ordinal + 1).coerceAtMost(Speed.entries.lastIndex)
        return entries[i]
    }
}

data class DateItem(val date: String, val weekDay: String, val suffix: String?)
data class TimeItem(val time: String)

enum class Calendar(override val label: (Language) -> String) : Labeled {
    /**
     * A calendar that is based on the movements of the sun.
     * Because it was compiled during the reign of Jalaluddin Malik-Shah I, it is called Jalali calendar.
     * Because it is based on the sun, it is also called the (En: Solar)/(Ar: Shamsi)/(Fa: Khorshidi) calendar.
     * Its starting date (مبدا) was the year Malik-Shah sat on the throne.
     *
     * So, Jalali == Solar == Shamsi == Khorshidi.
     *
     * The calendar in use today in Iran (Solar Hijri/هجری خورشیدی) is Jalali/Solar but with two modifications:
     *   1. Its starting date is the Hijrah (the journey of the prophet Muhammad and his followers from Mecca to Medina)
     *   2. It has leap years (the original Jalali/Solar had variable months so, it did not need adjusting)
     *
     * All the above calendars are a type of broader category called Iranian (Persian) calendars.
     */
    SOLAR_HIJRI({ it.messages.txtLblCalendarSolarHijri }) {
        override fun format(date: LocalDate, language: Language): String {
            return PersianDate
                .fromGregorian(date)
                .toString()
                .let(language::localizeDigits)
                .replace("-", if (language is LanguageFa) "/" else "-")
        }
    },

    /**
     * The most widely used calendar around the world today.
     *
     * In Iran, it is called "میلادی".
     */
    GREGORIAN({ it.messages.txtLblCalendarGregorian }) {
        override fun format(date: LocalDate, language: Language): String {
            return date
                .toString()
                .let(language::localizeDigits)
                .replace("-", if (language is LanguageFa) "/" else "-")
        }
    };

    abstract fun format(date: LocalDate, language: Language): String
}

enum class Theme(override val label: (Language) -> String) : Labeled {
    LIGHT({ it.messages.txtLblThemeLight }),
    DARK({ it.messages.txtLblThemeDark })
}

enum class Toggle(override val label: (Language) -> String) : Labeled {
    ENABLED({ it.messages.txtLblEnabled }),
    DISABLED({ it.messages.txtLblDisabled })
}
