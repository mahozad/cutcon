package ir.mahozad.cutcon.ui.theme

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import ir.mahozad.cutcon.LocalLanguage

private val LightColors = lightColors(
    primary = Color(0xff208bb2),
    secondary = Color(0xff208bb2),
    primaryVariant = Color(0xff1faedc)
)

private val DarkColors = darkColors(
    primary = Color(0xff1faedc),
    secondary = Color(0xff1faedc),
    primaryVariant = Color(0xff208bb2)
)

@Composable
fun AppTheme(
    isDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val language = LocalLanguage.current
    MaterialTheme(
        colors = if (isDark) DarkColors else LightColors,
        typography = MaterialTheme.typography.copy(
            // For buttons because they have hard-coded text style
            button = MaterialTheme.typography.button.copy(fontFamily = language.fontFamily),
            // For dropdown items because they have hard-coded text style
            subtitle1 = MaterialTheme.typography.subtitle1.copy(fontFamily = language.fontFamily)
        )
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = language.fontFamily),
            LocalLayoutDirection provides language.layoutDirection
        ) {
            content()
        }
    }
}
