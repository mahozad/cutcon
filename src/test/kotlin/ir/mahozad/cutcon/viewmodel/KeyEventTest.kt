package ir.mahozad.cutcon.viewmodel

import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import ir.mahozad.cutcon.component.MediaPlayer
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.createKeyPressedEvent
import ir.mahozad.cutcon.model.MediaInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class KeyEventTest {

    private val keyEventEscapePressed = createKeyPressedEvent(27.toChar())
    private val keyEventSpacePressed = createKeyPressedEvent(' ')
    private val keyEventFPressed = createKeyPressedEvent('F')

    @Test
    fun `When media is resumed and hitting Space bar, the media player toggleResume or pause should be called`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val mediaPlayer = spyk<MediaPlayer>()
        every { mediaPlayer.isResumed } returns flowOf(true)
        val viewModel = constructMainViewModel(dispatcher, mediaPlayer = mediaPlayer)
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(keyEventSpacePressed)
        viewModel.onKeyboardEvent(keyEvent)
        // TODO: See https://github.com/mockk/mockk/issues/1257
        verify(exactly = 1) { mediaPlayer.toggleResume() }
    }

    @Test
    fun `When media is paused and hitting Space bar, the media player toggleResume or resume should be called`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val mediaPlayer = spyk<MediaPlayer>()
        every { mediaPlayer.isResumed } returns flowOf(false)
        val viewModel = constructMainViewModel(dispatcher, mediaPlayer = mediaPlayer)
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(keyEventSpacePressed)
        viewModel.onKeyboardEvent(keyEvent)
        // TODO: See https://github.com/mockk/mockk/issues/1257
        verify(exactly = 1) { mediaPlayer.toggleResume() }
    }

    @Test
    fun `When isFullscreen is false and hitting F, isFullscreen should update to true`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isFullscreen.toList(results) }
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(keyEventFPressed)
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
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(keyEventFPressed)
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
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(keyEventEscapePressed)
        viewModel.onKeyboardEvent(keyEvent)
        assertThat(results).containsExactly(true, false)
    }

    // etc.
}
