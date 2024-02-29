package ir.mahozad.cutcon.viewmodel

import io.mockk.coEvery
import io.mockk.spyk
import ir.mahozad.cutcon.component.UrlMaker
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.converter.Converter
import ir.mahozad.cutcon.converter.ConverterFactory
import ir.mahozad.cutcon.converter.FFmpegOption
import ir.mahozad.cutcon.model.ConverterFlags
import ir.mahozad.cutcon.model.CoverOptions
import ir.mahozad.cutcon.model.IntroOptions
import ir.mahozad.cutcon.model.Quality
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.io.path.Path
import kotlin.io.path.div

@OptIn(ExperimentalCoroutinesApi::class)
abstract class IsFullscreenTest {
    @Test
    fun `IsFullScreen should initially be false`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.isFullscreen.first()
        assertThat(result).isFalse()
    }

    @Test
    fun `When entering fullscreen, isFullscreen should update to true`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isFullscreen.toList(results) }
        viewModel.enterFullscreen()
        assertThat(results.last()).isTrue()
    }

    @Test
    fun `When entering fullscreen, side panel should get hidden`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
        viewModel.enterFullscreen()
        assertThat(results.last()).isFalse()
    }

    @Test
    fun `When exiting fullscreen, isFullscreen should update to false`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isFullscreen.toList(results) }
        viewModel.enterFullscreen()
        viewModel.exitFullscreen()
        assertThat(results.last()).isFalse()
    }

    @Test
    fun `When side panel is off and then entering full screen and then restoring to regular screen, side panel should be off`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                toggleSidePanel()
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
            viewModel.enterFullscreen()
            viewModel.exitFullscreen()
            assertThat(results).containsExactly(false)
        }

    @Test
    fun `When side panel is on and then entering full screen and then restoring to regular screen, side panel should be on`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher)
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
            viewModel.enterFullscreen()
            viewModel.exitFullscreen()
            assertThat(results).containsExactly(true, false, true)
        }

    /**
     * Because when the dialog is displayed, the fullscreen automatically exits
     * and the whole app layout shows the display image.
     */
    @Disabled(
        """
            With the new common Dialog element, this is not required anymore.
            See https://github.com/JetBrains/compose-multiplatform/issues/3438
        """
    )
    @Test
    fun `When isFullscreen is true and isSuccessDialogDisplayed becomes true, isFullscreen should update to false`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val converter = spyk(object : Converter(dispatcher) {
                override fun ffmpegOptions(
                    quality: Quality,
                    introOptions: IntroOptions,
                    coverOptions: CoverOptions,
                    flags: ConverterFlags
                ) = emptyList<FFmpegOption>()
            })
            coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } coAnswers {
                println("I'm a fake converter and I'm pretending to convert...")
            }
            val converterFactory = ConverterFactory { converter }
            val urlMaker = UrlMaker { URL("file://") }
            val saveFile = Path("xyz") / "1.mp4"
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                converterFactory = converterFactory
            ).apply {
                setSaveFile(saveFile)
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isFullscreen.toList(results) }
            viewModel.enterFullscreen()
            viewModel.startProcess()
            assertThat(results).containsExactly(false, true, false)
        }

    @Test
    fun `When hiding side panel then entering fullscreen then exiting fullscreen then showing side panel then the conversion completes, the side panel should not get hidden`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val converter = spyk(object : Converter(dispatcher) {
                override fun ffmpegOptions(
                    quality: Quality,
                    introOptions: IntroOptions,
                    coverOptions: CoverOptions,
                    flags: ConverterFlags
                ) = emptyList<FFmpegOption>()
            })
            coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } coAnswers {
                println("I'm a fake converter and I'm pretending to convert...")
            }
            val converterFactory = ConverterFactory { converter }
            val urlMaker = UrlMaker { URL("file://") }
            val saveFile = Path("xyz") / "1.mp4"
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                converterFactory = converterFactory
            ).apply {
                setSaveFile(saveFile)
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
            viewModel.toggleSidePanel()
            viewModel.enterFullscreen()
            viewModel.exitFullscreen()
            viewModel.toggleSidePanel()
            viewModel.startProcess()
            assertThat(results).containsExactly(true, false, true)
        }

    /**
     * When toggling side panel with keyboard shortcut.
     */
    @Test
    fun `When fullscreen is on, toggling the side panel should have no effect`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            enterFullscreen()
        }
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
        viewModel.toggleSidePanel()
        assertThat(results).containsExactly(false)
    }

    /**
     * When toggling mini screen with keyboard shortcut.
     */
    @Test
    fun `When fullscreen is on, toggling the mini screen should have no effect`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            enterFullscreen()
        }
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isMiniScreen.toList(results) }
        viewModel.toggleMiniScreen()
        assertThat(results).containsExactly(false)
    }

    @Disabled("Not needed anymore. See the disabled unit test above for why.")
    @Test
    fun `When side panel is on then entering fullscreen then the conversion completes, the side panel should update to on`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val converter = spyk(object : Converter(dispatcher) {
                override fun ffmpegOptions(
                    quality: Quality,
                    introOptions: IntroOptions,
                    coverOptions: CoverOptions,
                    flags: ConverterFlags
                ) = emptyList<FFmpegOption>()
            })
            coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } coAnswers {
                println("I'm a fake converter and I'm pretending to convert...")
            }
            val converterFactory = ConverterFactory { converter }
            val urlMaker = UrlMaker { URL("file://") }
            val saveFile = Path("xyz") / "1.mp4"
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                converterFactory = converterFactory
            ).apply {
                setSaveFile(saveFile)
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
            viewModel.enterFullscreen()
            viewModel.startProcess()
            assertThat(results).containsExactly(true, false, true)
        }
}
