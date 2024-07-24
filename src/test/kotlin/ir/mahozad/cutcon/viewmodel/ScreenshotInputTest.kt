package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.createKeyPressedEvent
import ir.mahozad.cutcon.getResourceAsPath
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ScreenshotInputTest {

    @Nested
    inner class IsScreenshotInputEnabledTest {
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

    @Nested
    inner class IsScreenshotInputActiveTest {
        @Test
        fun `Initially, isScreenshotInputActive should be false`() = runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher)
            assertThat(viewModel.isScreenshotInputActive.first()).isEqualTo(false)
        }

        @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "FIXME: Fails on CI")
        @Test
        fun `When calling takeScreenshot, isScreenshotInputActive should be changed to true`() = runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setSourceToLocal(getResourceAsPath("test.ts"))
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isScreenshotInputActive.toList(results) }
            viewModel.takeScreenshot()
            while (results.size < 3) delay(5.milliseconds) // This is to prevent test from failing on CI
            assertThat(results).containsExactly(false, true, false)
        }

        @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "FIXME: Fails on CI")
        @Test
        fun `When calling takeScreenshot, isScreenshotInputActive should be changed to false after a small period`() =
            runTest {
                val dispatcher = UnconfinedTestDispatcher(testScheduler)
                val viewModel = constructMainViewModel(dispatcher).apply {
                    setSourceToLocal(getResourceAsPath("test.ts"))
                }
                val results = mutableListOf<Boolean>()
                backgroundScope.launch(dispatcher) { viewModel.isScreenshotInputActive.toList(results) }
                viewModel.takeScreenshot()
                while (results.size < 3) delay(5.milliseconds) // This is to prevent test from failing on CI
                assertThat(results).containsExactly(false, true, false)
            }

        @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "FIXME: Fails on CI")
        @Test
        fun `When pressing screenshot shortcut, isScreenshotInputActive should be changed to true and then false after a small period`() =
            runTest {
                val dispatcher = UnconfinedTestDispatcher(testScheduler)
                val viewModel = constructMainViewModel(dispatcher).apply {
                    setSourceToLocal(getResourceAsPath("test.ts"))
                }
                val results = mutableListOf<Boolean>()
                backgroundScope.launch(dispatcher) { viewModel.isScreenshotInputActive.toList(results) }
                val keyEvent = androidx.compose.ui.input.key.KeyEvent(createKeyPressedEvent('S'))
                viewModel.onKeyboardEvent(keyEvent)
                while (results.size < 3) delay(5.milliseconds) // This is to prevent test from failing on CI
                assertThat(results).containsExactly(false, true, false)
            }

        @Test
        fun `When screenshot input is disabled and pressing screenshot shortcut, isScreenshotInputActive should stay false`() =
            runTest {
                val dispatcher = UnconfinedTestDispatcher(testScheduler)
                val viewModel = constructMainViewModel(dispatcher)
                val results = mutableListOf<Boolean>()
                backgroundScope.launch(dispatcher) { viewModel.isScreenshotInputActive.toList(results) }
                val keyEvent = androidx.compose.ui.input.key.KeyEvent(createKeyPressedEvent('S'))
                viewModel.onKeyboardEvent(keyEvent)
                advanceUntilIdle()
                assertThat(results).containsExactly(false)
            }
    }
}
