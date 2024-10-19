package ir.mahozad.cutcon.localization

import androidx.compose.ui.platform.PlatformLocalization
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.compose.resources.FontResource
import java.time.DayOfWeek
import java.util.*

/**
 * See https://github.com/JetBrains/compose-multiplatform/issues/425
 * and https://stackoverflow.com/q/2469435
 * and https://www.baeldung.com/java-resourcebundle
 */
interface Language {
    val tag: String
    val locale: Locale
    val messages: Messages
    val fontResource: FontResource
    // See https://m2.material.io/design/usability/bidirectionality.html
    val layoutDirection: LayoutDirection
    val contextMenuLocalization: PlatformLocalization
        get() = object : PlatformLocalization {
            override val cut = messages.cut
            override val copy = messages.copy
            override val paste = messages.paste
            override val selectAll = messages.selectAll
        }

    fun getWeekdayName(day: DayOfWeek) = when (day) {
        DayOfWeek.SATURDAY  -> messages.weekdaySaturday
        DayOfWeek.SUNDAY    -> messages.weekdaySunday
        DayOfWeek.MONDAY    -> messages.weekdayMonday
        DayOfWeek.TUESDAY   -> messages.weekdayTuesday
        DayOfWeek.WEDNESDAY -> messages.weekdayWednesday
        DayOfWeek.THURSDAY  -> messages.weekdayThursday
        DayOfWeek.FRIDAY    -> messages.weekdayFriday
    }

    fun localizeDigits(string: String): String
    fun localizeNumber(number: Float): String

    companion object {
        fun fromTag(tag: String) = if (tag.lowercase() == LanguageFa.tag.lowercase()) {
            LanguageFa
        } else {
            LanguageEn
        }
    }
}
