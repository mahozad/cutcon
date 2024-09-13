package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultCoverOptions
import ir.mahozad.cutcon.getResourceAsPath
import ir.mahozad.cutcon.model.CoverOptions
import ir.mahozad.cutcon.model.WatermarkPosition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.extension

@OptIn(ExperimentalCoroutinesApi::class)
abstract class CoverTest {
    @Test
    fun `Cover should initially have proper values`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.coverOptions.first()
        assertThat(result).isEqualTo(defaultCoverOptions)
    }

    @Test
    fun `When setting opacity to a new value, it should reflect in cover properties`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<CoverOptions>()
        backgroundScope.launch(dispatcher) { viewModel.coverOptions.toList(results) }
        viewModel.setWaterMarkOpacity(0.63f)
        assertThat(results.last()).isEqualTo(
            defaultCoverOptions.copy(opacity = 0.63f)
        )
    }

    @Test
    fun `When setting scale to a new value, it should reflect in cover properties`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<CoverOptions>()
        backgroundScope.launch(dispatcher) { viewModel.coverOptions.toList(results) }
        viewModel.setWaterMarkScale(0.91f)
        assertThat(results.last()).isEqualTo(
            defaultCoverOptions.copy(scale = 0.91f)
        )
    }

    @Test
    fun `When setting position to a new value, it should reflect in cover properties`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<CoverOptions>()
        backgroundScope.launch(dispatcher) { viewModel.coverOptions.toList(results) }
        viewModel.setWatermarkPosition(WatermarkPosition.BOTTOM_LEFT)
        assertThat(results.last()).isEqualTo(
            defaultCoverOptions.copy(position = WatermarkPosition.BOTTOM_LEFT)
        )
    }

    @Test
    fun `When setting cover to a new file, it should reflect in cover properties`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val cover = getResourceAsPath("test.png")
        val results = mutableListOf<CoverOptions>()
        backgroundScope.launch(dispatcher) { viewModel.coverOptions.toList(results) }
        viewModel.setCoverFile(cover)
        assertThat(results.last()).isEqualTo(
            defaultCoverOptions.copy(path = cover)
        )
    }

    @Test
    fun `When setting cover to a new file, the cover image bitmap should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val cover = getResourceAsPath("test.png")
        val results = mutableListOf<ImageBitmap?>()
        backgroundScope.launch(dispatcher) { viewModel.coverPreview.toList(results) }
        viewModel.setCoverFile(cover)
        assertThat(results.last()).isNotNull()
    }

    @Test
    fun `When setting cover to an SVG file, the image should be converted to PNG because FFmpeg does not support SVG`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher)
            val cover = getResourceAsPath("test.svg")
            val results = mutableListOf<CoverOptions>()
            backgroundScope.launch(dispatcher) { viewModel.coverOptions.toList(results) }
            viewModel.setCoverFile(cover)
            assertThat(results.last().path?.extension).isEqualToIgnoringCase("png")
        }

    @Test
    fun `When setting cover to an unsupported file, cover should stay (or update to) null`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val cover = getResourceAsPath("test.mp3")
        val viewModel = constructMainViewModel(dispatcher).apply {
            setCoverFile(cover)
        }
        val results = mutableListOf<CoverOptions>()
        backgroundScope.launch(dispatcher) { viewModel.coverOptions.toList(results) }
        viewModel.setCoverFile(cover)
        assertThat(results.last().path).isNull()
    }
}
