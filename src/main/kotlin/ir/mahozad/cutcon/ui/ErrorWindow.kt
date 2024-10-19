package ir.mahozad.cutcon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLocalization
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.generated.resources.Res
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.model.Theme
import ir.mahozad.cutcon.openAppLogFolder
import ir.mahozad.cutcon.ui.theme.AppTheme
import ir.mahozad.cutcon.ui.widget.StackTrace
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToSvgPainter
import java.net.URI
import kotlin.system.exitProcess

fun showErrorWindow(error: Throwable?, theme: Theme, language: Language) {
    // Could also use the asynchronous Res.readBytes("drawable/logo-red.svg")
    @OptIn(ExperimentalResourceApi::class)
    val appErrorIcon = Res
        .getUri("drawable/logo-red.svg")
        .let(::URI)
        .toURL()
        .readBytes()
        .decodeToSvgPainter(Density(1f))
    singleWindowApplication(
        alwaysOnTop = true,
        undecorated = true,
        resizable = false,
        icon = appErrorIcon, // Used for app icon in taskbar
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
                icon = appErrorIcon,
                title = { Title(language, it) },
                isMinimizable = false,
                onCloseRequest = { exitProcess(status = 1) }
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides language.layoutDirection,
                    // For context menu when selecting the error message and doing right-click
                    LocalLocalization provides language.contextMenuLocalization
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Prompt(language)
                        Spacer(Modifier.height(8.dp))
                        // Makes the error message selectable for copying
                        SelectionContainer {
                            StackTrace(error)
                        }
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
