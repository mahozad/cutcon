package ir.mahozad.cutcon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.model.Theme
import ir.mahozad.cutcon.openAppLogFolder
import ir.mahozad.cutcon.ui.theme.AppTheme
import ir.mahozad.cutcon.ui.widget.StackTrace
import ir.mahozad.cutcon.viewModel
import kotlin.system.exitProcess

fun errorWindow(throwable: Throwable?) {
    val theme = viewModel.theme.value
    val language = viewModel.language.value
    singleWindowApplication(
        alwaysOnTop = true,
        resizable = false,
        title = language.messages.appName,
        state = WindowState(
            width = 464.dp,
            height = 424.dp,
            position = WindowPosition(Alignment.Center)
        )
    ) {
        AppTheme(isDark = theme == Theme.DARK) {
            WindowDecoration(
                isDecorationVisible = true,
                icon = painterResource("logo.svg"),
                title = { Title(language, it) },
                isMinimizable = false,
                onCloseRequest = { exitProcess(status = 1) }
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides language.layoutDirection) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Prompt(language)
                        Spacer(Modifier.height(8.dp))
                        StackTrace(throwable)
                    }
                }
            }
        }
    }
}

@Composable
private fun Prompt(language: Language) {
    val scope = rememberCoroutineScope()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = scope::openAppLogFolder) {
            Text(
                text = language.messages.openLogFolder,
                fontSize = defaultFontSize
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = language.messages.error,
            fontSize = defaultFontSize
        )
    }
}

@Composable
private fun Title(language: Language, fontSize: TextUnit) {
    Text(
        text = language.messages.appName,
        fontSize = fontSize
    )
}
