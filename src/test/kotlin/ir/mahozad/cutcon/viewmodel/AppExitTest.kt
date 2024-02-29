package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.text.TextRange
import io.mockk.called
import io.mockk.coEvery
import io.mockk.spyk
import io.mockk.verify
import ir.mahozad.cutcon.FakeMediaPlayer
import ir.mahozad.cutcon.component.UrlMaker
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.converter.Converter
import ir.mahozad.cutcon.converter.ConverterFactory
import ir.mahozad.cutcon.converter.FFmpegOption
import ir.mahozad.cutcon.model.ConverterFlags
import ir.mahozad.cutcon.model.CoverOptions
import ir.mahozad.cutcon.model.IntroOptions
import ir.mahozad.cutcon.model.Quality
import ir.mahozad.cutcon.testTimeout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AppExitTest {
    @Test
    fun `Initially, isAppExitConfirmDialogDisplayed should be false`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        assertThat(viewModel.isAppExitConfirmDialogDisplayed.first()).isEqualTo(false)
    }

    @Test
    fun `When isAppExitConfirmDialogDisplayed is false and requesting exit dialog dismiss, isAppExitConfirmDialogDisplayed should stay false`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val results = mutableListOf<Boolean>()
            val viewModel = constructMainViewModel(dispatcher)
            backgroundScope.launch(dispatcher) { viewModel.isAppExitConfirmDialogDisplayed.toList(results) }
            viewModel.onAppExitConfirmDialogDismissRequest()
            assertThat(results).containsExactly(false)
        }

    @Test
    fun `When isAppExitConfirmDialogDisplayed is true and requesting exit dialog dismiss, isAppExitConfirmDialogDisplayed should update to false`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            var progressed = false
            val converter = spyk(object : Converter(dispatcher) {
                override fun ffmpegOptions(
                    quality: Quality,
                    introOptions: IntroOptions,
                    coverOptions: CoverOptions,
                    flags: ConverterFlags
                ) = emptyList<FFmpegOption>()
            })
            coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } coAnswers {
                println("I'm a fake converter and I'm running an infinite loop pretending to convert...")
                while (isActive) {
                    progressed = true
                    delay(15.milliseconds)
                }
            }
            val converterFactory = ConverterFactory { converter }
            val urlMaker = UrlMaker { URL("file://") }
            val results = mutableListOf<Boolean>()
            val saveFile = Path("xyz") / "1.mp4"
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                mediaPlayer = FakeMediaPlayer(10.seconds),
                converterFactory = converterFactory
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("8", TextRange.Zero)
                setSaveFile(saveFile)
            }
            launch(Dispatchers.Default) { viewModel.startProcess() }
            launch(Dispatchers.Default) { while (!progressed) delay(50.milliseconds) }.join()
            backgroundScope.launch(dispatcher) { viewModel.isAppExitConfirmDialogDisplayed.toList(results) }
            viewModel.onAppExitRequest(forceExit = false, exit = {})
            viewModel.onAppExitConfirmDialogDismissRequest()
            assertThat(results).containsExactly(false, true, false)
        }

    @Test
    fun `While no clip is being created and requesting app exit, isAppExitConfirmDialogDisplayed should stay false and passed function be called`() =
        runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val exit: () -> Unit = spyk()
            val results = mutableListOf<Boolean>()
            val viewModel = constructMainViewModel(dispatcher)
            backgroundScope.launch(dispatcher) { viewModel.isAppExitConfirmDialogDisplayed.toList(results) }
            viewModel.onAppExitRequest(forceExit = false, exit = exit)
            verify(exactly = 1) { exit() }
            assertThat(results).containsExactly(false)
        }

    @Test
    fun `While a clip is being created and requesting app exit with forceExit = false, isAppExitConfirmDialogDisplayed should change to true and passed function not called`() =
        runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            var progressed = false
            val converter = spyk(object : Converter(dispatcher) {
                override fun ffmpegOptions(
                    quality: Quality,
                    introOptions: IntroOptions,
                    coverOptions: CoverOptions,
                    flags: ConverterFlags
                ) = emptyList<FFmpegOption>()
            })
            coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } coAnswers {
                println("I'm a fake converter and I'm running an infinite loop pretending to convert...")
                while (isActive) {
                    progressed = true
                    delay(15.milliseconds)
                }
            }
            val exit: () -> Unit = spyk()
            val converterFactory = ConverterFactory { converter }
            val urlMaker = UrlMaker { URL("file://") }
            val results = mutableListOf<Boolean>()
            val saveFile = Path("xyz") / "1.mp4"
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                mediaPlayer = FakeMediaPlayer(10.seconds),
                converterFactory = converterFactory
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("8", TextRange.Zero)
                setSaveFile(saveFile)
            }
            backgroundScope.launch(dispatcher) { viewModel.isAppExitConfirmDialogDisplayed.toList(results) }
            launch(Dispatchers.Default) { viewModel.startProcess() }
            launch(Dispatchers.Default) { while (!progressed) delay(50.milliseconds) }.join()
            viewModel.onAppExitRequest(forceExit = false, exit = exit)
            verify { exit wasNot called }
            assertThat(results).containsExactly(false, true)
        }

    @Test
    fun `While a clip is being created and requesting app exit with forceExit = true, isAppExitConfirmDialogDisplayed should stay false and passed function be called`() =
        runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            var progressed = false
            val converter = spyk(object : Converter(dispatcher) {
                override fun ffmpegOptions(
                    quality: Quality,
                    introOptions: IntroOptions,
                    coverOptions: CoverOptions,
                    flags: ConverterFlags
                ) = emptyList<FFmpegOption>()
            })
            coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } coAnswers {
                println("I'm a fake converter and I'm running an infinite loop pretending to convert...")
                while (isActive) {
                    progressed = true
                    delay(15.milliseconds)
                }
            }
            val exit: () -> Unit = spyk()
            val converterFactory = ConverterFactory { converter }
            val urlMaker = UrlMaker { URL("file://") }
            val results = mutableListOf<Boolean>()
            val saveFile = Path("xyz") / "1.mp4"
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                mediaPlayer = FakeMediaPlayer(10.seconds),
                converterFactory = converterFactory
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("8", TextRange.Zero)
                setSaveFile(saveFile)
            }
            backgroundScope.launch(dispatcher) { viewModel.isAppExitConfirmDialogDisplayed.toList(results) }
            launch(Dispatchers.Default) { viewModel.startProcess() }
            launch(Dispatchers.Default) { while (!progressed) delay(50.milliseconds) }.join()
            viewModel.onAppExitRequest(forceExit = true, exit = exit)
            verify(exactly = 1) { exit() }
            assertThat(results).containsExactly(false)
        }
}
