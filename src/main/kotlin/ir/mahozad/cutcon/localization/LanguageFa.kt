package ir.mahozad.cutcon.localization

import androidx.compose.ui.unit.LayoutDirection
import ir.mahozad.cutcon.generated.resources.Res
import ir.mahozad.cutcon.generated.resources.vazirmatn_ui_v33_003
import java.text.DecimalFormat
import java.util.*

object LanguageFa : Language {
    override val tag = "Fa"
    override val locale = Locale.forLanguageTag(tag)!!
    override val messages = MessagesFa
    override val fontResource = Res.font.vazirmatn_ui_v33_003
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
