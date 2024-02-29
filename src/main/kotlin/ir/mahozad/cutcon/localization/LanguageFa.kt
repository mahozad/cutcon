package ir.mahozad.cutcon.localization

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.LayoutDirection
import java.text.DecimalFormat
import java.util.*

object LanguageFa : Language {
    override val tag = "Fa"
    override val locale = Locale.forLanguageTag(tag)!!
    override val messages = MessagesFa
    override val fontFamily = FontFamily(Font("font/vazirmatn-ui-v33.003.ttf"))
    override val layoutDirection = LayoutDirection.Rtl

    private val decimalFormat = DecimalFormat.getInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 2
    }

    override fun localizeDigits(string: String) =
        string.map { it.digitToIntOrNull()?.let('۰'::plus) ?: it }.joinToString(separator = "")

    override fun localizeNumber(number: Float): String {
        val string = decimalFormat.format(number)
        // Fixes the bug with number not being localized when running the app distributable
        return localizeDigits(string).replace(',', '٬').replace('.', '٫')
    }
}
