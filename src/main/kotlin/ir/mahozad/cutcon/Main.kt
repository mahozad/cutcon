package ir.mahozad.cutcon

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.*
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.component.*
import ir.mahozad.cutcon.component.DefaultMediaPlayer
import ir.mahozad.cutcon.converter.DefaultConverterFactory
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.ui.MainWindow
import ir.mahozad.cutcon.ui.showErrorWindow
import kotlinx.coroutines.Dispatchers
import java.util.prefs.Preferences
import kotlin.io.path.Path

// See logback.xml file in classpath for logback configs
private val logger = logger(name = "Main")
private var error: Throwable? = null

/**
 * Note that it does not catch exceptions thrown outside a [Window].
 * See https://github.com/JetBrains/compose-multiplatform/issues/663
 * and https://github.com/JetBrains/compose-multiplatform/issues/1764
 * and https://github.com/JetBrains/compose-multiplatform/issues/4233
 */
@OptIn(ExperimentalComposeUiApi::class)
private val ApplicationScope.exceptionHandler
    get() = WindowExceptionHandlerFactory {
        WindowExceptionHandler {
            logger.error(it) { "An error occurred in the application" }
            // The app should be terminated so the code will continue after
            // application {} because of its exitProcessOnExit = false
            terminate()
            error = it
        }
    }

val assetsPath = System
    .getProperty("compose.application.resources.dir")
    ?.let(::Path)
    ?.also { logger.debug { "Custom assets path: $it" } }
    ?: error(Messages.ERR_COMPOSE_RES_DIR_NOT_SET)

val viewModel = MainViewModel(
    urlMaker = DefaultUrlMaker,
    settings = Preferences.userNodeForPackage({}::class.java),
    dispatcher = Dispatchers.IO,
    mediaPlayer = DefaultMediaPlayer(),
    dateTimeChecker = DefaultDateTimeChecker(Dispatchers.IO),
    converterFactory = DefaultConverterFactory(Dispatchers.IO),
    saveFileNameGenerator = DefaultSaveFileNameGenerator
)

@OptIn(ExperimentalComposeUiApi::class)
fun main(vararg args: String) {
    logger.info { "Application started" }
    initialize(args.getOrNull(0))
    application(exitProcessOnExit = false /* See exceptionHandler */) {
        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides exceptionHandler
        ) {
            MainWindow(onExitRequest = ::terminate)
        }
    }
    error?.let(::showErrorWindow)
    logger.info { "Application finished" }
}

private fun initialize(commandLinePath: String?) {
    viewModel.startUrlMaker()
    viewModel.startDateTimeChecker()
    viewModel.startMediaProgressListener()
    // For when launching the app via "Open with" or command line
    commandLinePath?.let(::Path)?.run(viewModel::setSourceToLocal)
}

// This is also called when force closing/halting/killing the app process
private fun ApplicationScope.terminate() {
    logger.info { "Cancelling everything..." }
    viewModel.cancelEverything()
    logger.info { "Exiting the application..." }
    exitApplication()
}
