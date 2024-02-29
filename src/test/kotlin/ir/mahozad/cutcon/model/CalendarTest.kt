package ir.mahozad.cutcon.model

import ir.mahozad.cutcon.localization.LanguageEn
import ir.mahozad.cutcon.localization.LanguageFa
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * The [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) standard
 * defines the *yyyy-mm-dd* format (with `-` as separator)
 * only for Gregorian (میلادی) calendar system and does not talk about
 * any other calendar systems (such as Persian/Solar/Jalali).
 *
 * Microsoft Windows 11 uses `/` separator when the region/locale
 * is "fa" for both Gregorian and Solar calendars (in taskbar).
 *
 * JavaScript uses `/` separator when the locale is "fa" for
 * both Gregorian and Solar calendars: `new Date().toLocaleString("fa")`.
 *
 * So, the calendars of the app are expected to format like below:
 * - If the language is Persian/Farsi, use *yyyy/mm/dd* format
 * - Otherwise, use *yyyy-mm-dd* format
 */
class CalendarTest {

    @Test
    fun `When calendar is Solar and locale is Persian, formatting should return proper result`() {
        val calendar = Calendar.SOLAR_HIJRI
        val date = LocalDate.of(1789, 5, 6)
        val result = calendar.format(date, LanguageFa)
        assertThat(result).isEqualTo("۱۱۶۸/۰۲/۱۷") // See the comments above test class for more information
    }

    @Test
    fun `When calendar is Solar and locale is English, formatting should return proper result`() {
        val calendar = Calendar.SOLAR_HIJRI
        val date = LocalDate.of(1789, 5, 6)
        val result = calendar.format(date, LanguageEn)
        assertThat(result).isEqualTo("1168-02-17") // See the comments above test class for more information
    }

    @Test
    fun `When calendar is Gregorian and locale is Persian, formatting should return proper result`() {
        val calendar = Calendar.GREGORIAN
        val date = LocalDate.of(1789, 5, 6)
        val result = calendar.format(date, LanguageFa)
        assertThat(result).isEqualTo("۱۷۸۹/۰۵/۰۶") // See the comments above test class for more information
    }

    @Test
    fun `When calendar is Gregorian and locale is English, formatting should return proper result`() {
        val calendar = Calendar.GREGORIAN
        val date = LocalDate.of(1789, 5, 6)
        val result = calendar.format(date, LanguageEn)
        assertThat(result).isEqualTo("1789-05-06") // See the comments above test class for more information
    }
}
