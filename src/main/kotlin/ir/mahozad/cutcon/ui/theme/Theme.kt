package ir.mahozad.cutcon.ui.theme

import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.TextUnit
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

val borderColor @Composable get() = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)

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
            subtitle1 = MaterialTheme.typography.subtitle1.copy(fontFamily = language.fontFamily),
            // Most of the app text
            body1 = MaterialTheme.typography.body1.copy(
                fontFamily = language.fontFamily, // For error dialog etc.
                lineHeight = TextUnit.Unspecified // Because of an apparent change in text between CMP 1.5.10 and 1.6.0
            )
        )
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides language.layoutDirection
        ) {
            content()
        }
    }
}
