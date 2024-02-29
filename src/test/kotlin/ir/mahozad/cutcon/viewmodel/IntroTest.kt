package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultIntroOptions
import ir.mahozad.cutcon.getResourceAsPath
import ir.mahozad.cutcon.model.IntroOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
abstract class IntroTest {
    @Test
    fun `Intro should initially have proper values`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.introOptions.first()
        assertThat(result).isEqualTo(defaultIntroOptions)
    }

    @Test
    fun `When setting background color to a new value, it should reflect in intro properties`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<IntroOptions>()
        backgroundScope.launch(dispatcher) { viewModel.introOptions.toList(results) }
        viewModel.setIntroBackgroundColor(Color.Red)
        assertThat(results.last()).isEqualTo(
            defaultIntroOptions.copy(backgroundColor = Color.Red)
        )
    }

    @Test
    fun `When setting duration to a new value, it should reflect in intro properties`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<IntroOptions>()
        backgroundScope.launch(dispatcher) { viewModel.introOptions.toList(results) }
        viewModel.setIntroDuration(374.milliseconds)
        assertThat(results.last()).isEqualTo(
            defaultIntroOptions.copy(duration = 374.milliseconds)
        )
    }

    @Test
    fun `When setting intro to a new file, it should reflect in intro properties`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val image = getResourceAsPath("test.png")
        val results = mutableListOf<IntroOptions>()
        backgroundScope.launch(dispatcher) { viewModel.introOptions.toList(results) }
        viewModel.setIntroFile(image)
        assertThat(results.last()).isEqualTo(
            defaultIntroOptions.copy(path = image)
        )
    }

    @Test
    fun `When setting intro to a new file, the intro image bitmap should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val cover = getResourceAsPath("test.png")
        val results = mutableListOf<ImageBitmap?>()
        backgroundScope.launch(dispatcher) { viewModel.introBitmap.toList(results) }
        viewModel.setIntroFile(cover)
        assertThat(results.last()).isNotNull()
    }

    @Test
    fun `When setting intro to an unsupported file, intro should stay (or update to) null`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val cover = getResourceAsPath("test.mp3")
        val viewModel = constructMainViewModel(dispatcher).apply {
            setIntroFile(cover)
        }
        val results = mutableListOf<IntroOptions>()
        backgroundScope.launch(dispatcher) { viewModel.introOptions.toList(results) }
        viewModel.setIntroFile(cover)
        assertThat(results.last().path).isNull()
    }
}
