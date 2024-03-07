package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.text.TextRange
import io.mockk.every
import io.mockk.spyk
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.component.MediaPlayer
import ir.mahozad.cutcon.model.Clip
import ir.mahozad.cutcon.model.Progress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ClipInfoTest {
    @Test
    fun `Clip info should initially be the default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.clip.first()
        assertThat(result).isEqualTo(defaultClip)
    }

    @Test
    fun `When setting clip start to a new value, it should reflect in clip info`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Clip>()
        backgroundScope.launch(dispatcher) { viewModel.clip.toList(results) }
        viewModel.onClipStartSecondChanged("3", TextRange.Zero)
        assertThat(results.last()).isEqualTo(Clip(3.seconds, Duration.ZERO))
    }

    @Test
    fun `When setting clip start to null, it should update to default timestamp in clip info`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Clip>()
        backgroundScope.launch(dispatcher) { viewModel.clip.toList(results) }
        viewModel.onClipStartSecondChanged("", TextRange.Zero)
        assertThat(results.last()).isEqualTo(Clip(defaultTimeStamp, Duration.ZERO))
    }

    @Test
    fun `When setting clip end to a new value, it should reflect in clip info`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(7.seconds)
        ).apply {
            startMediaProgressListener()
        }
        val results = mutableListOf<Clip>()
        backgroundScope.launch(dispatcher) { viewModel.clip.toList(results) }
        viewModel.onClipEndSecondChanged("3", TextRange.Zero)
        assertThat(results.last()).isEqualTo(Clip(Duration.ZERO, 3.seconds))
    }

    @Test
    fun `When setting clip end to null, it should update to default timestamp in clip info`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(7.seconds)
        ).apply {
            startMediaProgressListener()
        }
        val results = mutableListOf<Clip>()
        backgroundScope.launch(dispatcher) { viewModel.clip.toList(results) }
        viewModel.onClipEndSecondChanged("", TextRange.Zero)
        assertThat(results.last()).isEqualTo(Clip(Duration.ZERO, defaultTimeStamp))
    }

    @Test
    fun `When setting clip end to a value after the source end, it should update to the end of source`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(7.seconds)
        ).apply {
            startMediaProgressListener()
        }
        val results = mutableListOf<Clip>()
        backgroundScope.launch(dispatcher) { viewModel.clip.toList(results) }
        viewModel.onClipEndSecondChanged("20", TextRange.Zero)
        assertThat(results.last()).isEqualTo(Clip(Duration.ZERO, 7.seconds))
    }

    @Test
    fun `When clip end input is after media end and the source is changed with its end greater than or equal to end input, clip end should update to end input`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val mockMediaPlayer = spyk<MediaPlayer>()
            val progressFlow = MutableStateFlow(Progress(0f, 5.seconds))
            every { mockMediaPlayer.progress } returns progressFlow
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = mockMediaPlayer
            ).apply {
                startMediaProgressListener()
                setSourceToLocal(Path("abc"))
                onClipEndSecondChanged(string = "30", selection = TextRange.Zero)
            }
            val results = mutableListOf<Clip>()
            backgroundScope.launch(dispatcher) { viewModel.clip.toList(results) }
            progressFlow.value = Progress(0f, 100.seconds)
            viewModel.setSourceToLocal(Path("xyz"))
            assertThat(results).containsExactly(
                Clip(Duration.ZERO, 5.seconds),
                Clip(Duration.ZERO, 30.seconds)
            )
        }

    @Test
    fun `When clip end input is before media end and the source is changed with its end less than end input, clip end should update to media length`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val mockMediaPlayer = spyk<MediaPlayer>()
            val progressFlow = MutableStateFlow(Progress(0f, 30.seconds))
            every { mockMediaPlayer.progress } returns progressFlow
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = mockMediaPlayer
            ).apply {
                startMediaProgressListener()
                setSourceToLocal(Path("abc"))
                onClipEndSecondChanged(string = "20", selection = TextRange.Zero)
            }
            val results = mutableListOf<Clip>()
            backgroundScope.launch(dispatcher) { viewModel.clip.toList(results) }
            progressFlow.value = Progress(0f, 5.seconds)
            viewModel.setSourceToLocal(Path("xyz"))
            assertThat(results).containsExactly(
                Clip(Duration.ZERO, 20.seconds),
                Clip(Duration.ZERO, 5.seconds)
            )
        }

    @Test
    fun `When clip end input is before media end and the source is changed to an image, clip end should update to zero`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val mockMediaPlayer = spyk<MediaPlayer>()
            val progressFlow = MutableStateFlow(Progress(0f, 30.seconds))
            every { mockMediaPlayer.progress } returns progressFlow
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = mockMediaPlayer
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged(string = "20", selection = TextRange.Zero)
            }
            val results = mutableListOf<Clip>()
            backgroundScope.launch(dispatcher) { viewModel.clip.toList(results) }
            progressFlow.value = Progress(0f, Duration.ZERO)
            viewModel.setSourceToLocal(getResourceAsPath("test.png"))
            assertThat(results).containsExactly(
                Clip(Duration.ZERO, 20.seconds),
                Clip(Duration.ZERO, Duration.ZERO)
            )
        }
}
