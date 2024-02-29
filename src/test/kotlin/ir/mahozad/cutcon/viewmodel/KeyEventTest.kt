package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.awt.ComposeWindow
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.model.MediaInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.awt.event.KeyEvent

@OptIn(ExperimentalCoroutinesApi::class)
abstract class KeyEventTest {
    @Test
    fun `When media is resumed and hitting Space bar, the media should pause`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(
            nativeKeyEvent = KeyEvent(
                ComposeWindow(), KeyEvent.KEY_PRESSED, 0, 0, ' '.code, ' ', KeyEvent.KEY_LOCATION_STANDARD
            )
        )
        viewModel.onKeyboardEvent(keyEvent)
        assertThat(results.map(MediaInfo::isResumed)).containsExactly(true, false)
    }

    @Test
    fun `When media is paused and hitting Space bar, the media should resume`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            toggleResume()
        }
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(
            nativeKeyEvent = KeyEvent(
                ComposeWindow(), KeyEvent.KEY_PRESSED, 0, 0, ' '.code, ' ', KeyEvent.KEY_LOCATION_STANDARD
            )
        )
        viewModel.onKeyboardEvent(keyEvent)
        assertThat(results.map(MediaInfo::isResumed)).containsExactly(false, true)
    }

    @Test
    fun `When isFullscreen is false and hitting F, isFullscreen should update to true`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isFullscreen.toList(results) }
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(
            nativeKeyEvent = KeyEvent(
                ComposeWindow(), KeyEvent.KEY_PRESSED, 0, 0, 'F'.code, 'F', KeyEvent.KEY_LOCATION_STANDARD
            )
        )
        viewModel.onKeyboardEvent(keyEvent)
        assertThat(results).containsExactly(false, true)
    }

    @Test
    fun `When isFullscreen is true and hitting F, isFullscreen should update to false`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            enterFullscreen()
        }
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isFullscreen.toList(results) }
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(
            nativeKeyEvent = KeyEvent(
                ComposeWindow(), KeyEvent.KEY_PRESSED, 0, 0, 'F'.code, 'F', KeyEvent.KEY_LOCATION_STANDARD
            )
        )
        viewModel.onKeyboardEvent(keyEvent)
        assertThat(results).containsExactly(true, false)
    }

    @Test
    fun `When isFullscreen is true and hitting Escape, isFullscreen should update to false`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            enterFullscreen()
        }
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isFullscreen.toList(results) }
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(
            nativeKeyEvent = KeyEvent(
                ComposeWindow(), KeyEvent.KEY_PRESSED, 0, 0, 27, 27.toChar(), KeyEvent.KEY_LOCATION_STANDARD
            )
        )
        viewModel.onKeyboardEvent(keyEvent)
        assertThat(results).containsExactly(true, false)
    }

    // etc.
}
