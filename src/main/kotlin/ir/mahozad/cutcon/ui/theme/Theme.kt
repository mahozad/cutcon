package ir.mahozad.cutcon.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.tween
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import ir.mahozad.cutcon.LocalLanguage
import org.jetbrains.compose.resources.Font


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

private val animationSpec = tween<Color>(durationMillis = 300, easing = Ease)

@Composable
private fun animatedColors(colors: Colors) = Colors(
    isLight = colors.isLight,
    primary = animateColorAsState(colors.primary, animationSpec).value,
    primaryVariant = animateColorAsState(colors.primaryVariant, animationSpec).value,
    onPrimary = animateColorAsState(colors.onPrimary, animationSpec).value,
    secondary = animateColorAsState(colors.secondary, animationSpec).value,
    secondaryVariant = animateColorAsState(colors.secondaryVariant, animationSpec).value,
    onSecondary = animateColorAsState(colors.onSecondary, animationSpec).value,
    background = animateColorAsState(colors.background, animationSpec).value,
    onBackground = animateColorAsState(colors.onBackground, animationSpec).value,
    surface = animateColorAsState(colors.surface, animationSpec).value,
    onSurface = animateColorAsState(colors.onSurface, animationSpec).value,
    error = animateColorAsState(colors.error, animationSpec).value,
    onError = animateColorAsState(colors.onError, animationSpec).value
)

val Colors.mediaDisplay: Color
    @Composable get() = animateColorAsState(
        targetValue = if (isLight) Color(0xff_e1e1e1) else Color(0xff_2f2f2f),
        animationSpec = animationSpec
    ).value

val Colors.border: Color
    @Composable get() = animateColorAsState(
        targetValue = onSurface.copy(ContentAlpha.medium),
        animationSpec = animationSpec
    ).value

@Suppress("UnusedReceiverParameter")
val Colors.success: Color get() = Color(red = 88, green = 150, blue = 0)

@Composable
fun AppTheme(
    isDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val language = LocalLanguage.current
    MaterialTheme(
        colors = animatedColors(if (isDark) DarkColors else LightColors),
        typography = MaterialTheme.typography.copy(
            // For buttons because they have hard-coded text style
            button = MaterialTheme.typography.button.copy(fontFamily = FontFamily(Font(language.fontResource))),
            // For dropdown items because they have hard-coded text style
            subtitle1 = MaterialTheme.typography.subtitle1.copy(fontFamily = FontFamily(Font(language.fontResource))),
            // Most of the app text
            body1 = MaterialTheme.typography.body1.copy(
                fontFamily = FontFamily(Font(language.fontResource)), // For error dialog etc.
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
