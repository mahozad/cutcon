package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.text.TextRange
import ir.mahozad.cutcon.FakeMediaPlayer
import ir.mahozad.cutcon.constructMainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ClipLoopTest {
    @Test
    fun `Clip looping should initially not be toggleable`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.isLoopToggleable.first()
        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `When setting clip start to a new value and clip duration is not positive, clip looping should become un-toggleable`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(7.seconds)
            ).apply {
                startMediaProgressListener()
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isLoopToggleable.toList(results) }
            viewModel.onClipEndSecondChanged("3", TextRange.Zero)
            viewModel.onClipStartSecondChanged("4", TextRange.Zero)
            assertThat(results).containsExactly(false, true, false)
        }

    @Test
    fun `When setting clip start to a new value and clip duration is positive, clip looping should become toggleable`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(7.seconds)
            ).apply {
                startMediaProgressListener()
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isLoopToggleable.toList(results) }
            viewModel.onClipEndSecondChanged("4", TextRange.Zero)
            viewModel.onClipStartSecondChanged("3", TextRange.Zero)
            assertThat(results).containsExactly(false, true)
        }

    @Test
    fun `When setting clip end to a new value and clip duration is not positive, clip looping should become un-toggleable`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(7.seconds)
            ).apply {
                startMediaProgressListener()
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isLoopToggleable.toList(results) }
            viewModel.onClipEndSecondChanged("3", TextRange.Zero)
            viewModel.onClipEndSecondChanged("0", TextRange.Zero)
            assertThat(results).containsExactly(false, true, false)
        }

    @Test
    fun `When setting clip end to a new value and clip duration is positive, clip looping should become toggleable`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(7.seconds)
            ).apply {
                startMediaProgressListener()
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isLoopToggleable.toList(results) }
            viewModel.onClipEndSecondChanged("3", TextRange.Zero)
            assertThat(results).containsExactly(false, true)
        }
}
