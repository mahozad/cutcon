package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.getResourceAsPath
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class IsScreenshotInputEnabledTest {
    @Nested
    inner class LocalSourceTest {
        @Test
        fun `When setting file to a video, IsScreenshotInputEnabled should update to true`() = runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setSourceToLocal(getResourceAsPath("test.png"))
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isScreenshotInputEnabled.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test.ts"))
            assertThat(results).containsExactly(false, true)
        }

        @Test
        fun `When setting file to an image, IsScreenshotInputEnabled should update to false`() = runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setSourceToLocal(getResourceAsPath("test.ts"))
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isScreenshotInputEnabled.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test.png"))
            assertThat(results).containsExactly(true, false)
        }

        @Test
        fun `When setting file to an audio, IsScreenshotInputEnabled should update to false`() = runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setSourceToLocal(getResourceAsPath("test.ts"))
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isScreenshotInputEnabled.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test.mp3"))
            assertThat(results).containsExactly(true, false)
        }
    }
}
