package ir.mahozad.cutcon.localization

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.LayoutDirection
import java.text.DecimalFormat
import java.util.*

object LanguageEn : Language {
    override val tag = "En"
    override val locale = Locale.ENGLISH!!
    override val messages = MessagesEn
    override val fontFamily = FontFamily(Font("font/roboto.ttf")) // Or, for example, FontFamily.Default
    override val layoutDirection = LayoutDirection.Ltr

    private val decimalFormat = DecimalFormat.getInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 2
    }

    override fun localizeDigits(string: String) =
        string.map { it.digitToIntOrNull()?.let('0'::plus) ?: it }.joinToString(separator = "")

    override fun localizeNumber(number: Float): String {
        return decimalFormat.format(number)
    }
}
