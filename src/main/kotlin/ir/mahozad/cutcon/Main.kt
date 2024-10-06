package ir.mahozad.cutcon

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import androidx.compose.ui.window.application
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.component.*
import ir.mahozad.cutcon.component.DefaultMediaPlayer
import ir.mahozad.cutcon.converter.DefaultConverterFactory
import ir.mahozad.cutcon.ui.MainWindow
import ir.mahozad.cutcon.ui.showErrorWindow
import kotlinx.coroutines.Dispatchers
import java.util.prefs.Preferences
import kotlin.io.path.Path
import kotlin.io.path.exists

private val logger = logger(name = "Main")
private var appError: Throwable? = null
private val viewModel = MainViewModel(
    urlMaker = DefaultUrlMaker,
    settings = Preferences.userNodeForPackage({}::class.java),
    dispatcher = Dispatchers.Default,
    mediaPlayer = DefaultMediaPlayer(),
    dateTimeChecker = DefaultDateTimeChecker(Dispatchers.IO),
    converterFactory = DefaultConverterFactory(Dispatchers.Default),
    saveFileNameGenerator = DefaultSaveFileNameGenerator
)

/**
 * Note that it does not catch exceptions thrown outside a [Window].
 * See https://github.com/JetBrains/compose-multiplatform/issues/663
 * and https://github.com/JetBrains/compose-multiplatform/issues/1764
 * and https://github.com/JetBrains/compose-multiplatform/issues/4233
 */
@OptIn(ExperimentalComposeUiApi::class)
private val ApplicationScope.exceptionHandlerProvider
    get() = LocalWindowExceptionHandlerFactory.provides(
        WindowExceptionHandlerFactory {
            WindowExceptionHandler {
                logger.error(it) { "An error occurred in the application" }
                // The app should be terminated so the code will continue after
                // application{} block because of its exitProcessOnExit = false
                terminate()
                appError = it
            }
        }
    )

@OptIn(ExperimentalComposeUiApi::class)
fun main(vararg args: String) {
    logger.info { "Application started" }
    initialize(programArgs = args)
    application(exitProcessOnExit = false /* See exception handler */) {
        CompositionLocalProvider(exceptionHandlerProvider) {
            MainWindow(viewModel, ::terminate)
        }
    }
    showErrorWindowIfNeeded(appError)
    logger.info { "Application finished" }
}

private fun initialize(vararg programArgs: String) {
    viewModel.startUrlMaker()
    viewModel.startDateTimeChecker()
    viewModel.startMediaProgressListener()
    // For when launching the app via "Open with" or command line
    setInitialSource(programArgs.firstOrNull())
}

// For when launching the app via "Open with" or command line
private fun setInitialSource(pathString: String?) = pathString
    ?.runCatching { Path(this) }
    ?.getOrNull()
    ?.takeIf { it.exists() }
    ?.run(viewModel::setSourceToLocal)

// This is also called when force closing/halting/killing the app process
private fun ApplicationScope.terminate() {
    logger.info { "Cancelling everything..." }
    viewModel.cancelEverything()
    logger.info { "Exiting the application..." }
    exitApplication()
}

private fun showErrorWindowIfNeeded(error: Throwable?) {
    val theme = viewModel.theme.value
    val language = viewModel.language.value
    if (error != null) showErrorWindow(error, theme, language)
}
