package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.text.TextRange
import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.component.MediaPlayer
import ir.mahozad.cutcon.component.UrlMaker
import ir.mahozad.cutcon.converter.Converter
import ir.mahozad.cutcon.converter.ConverterFactory
import ir.mahozad.cutcon.converter.FFmpegOption
import ir.mahozad.cutcon.converter.ProgressListener
import ir.mahozad.cutcon.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import java.net.URI
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
abstract class StatusTest {
    @Test
    fun `Initially, status should be 'clip not specified' error`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            setSourceToLocal(getResourceAsPath("test.ts"))
        }
        val result = viewModel.status.first()
        assertThat(result).isEqualTo(Status.Error.ClipNotSet)
    }

    @Test
    fun `When source is set to an image, status should be 'image input does not support creating clip' error`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(100.seconds)
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("2", TextRange.Zero)
                setSourceToLocal(getResourceAsPath("test.ts"))
            }
            val results = mutableListOf<Status>()
            backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test.png"))
            assertThat(results).containsExactly(
                Status.Error.FileNotSet,
                Status.Error.ClipFromImageNotSupported
            )
        }

    @Test
    fun `When source is set to an unsupported format, status should be 'input format does not support creating clip' error`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(100.seconds)
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("2", TextRange.Zero)
                setSourceToLocal(getResourceAsPath("test.ts"))
            }
            val results = mutableListOf<Status>()
            backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test-1.md"))
            assertThat(results).containsExactly(
                Status.Error.FileNotSet,
                Status.Error.ClipFromFormatNotSupported
            )
        }

    @Test
    fun `When clip is set to negative duration, status should be 'clip duration negative' error`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            setSourceToLocal(getResourceAsPath("test.ts"))
        }
        val results = mutableListOf<Status>()
        backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
        viewModel.onClipStartSecondChanged("2", TextRange.Zero)
        assertThat(results).containsExactly(
            Status.Error.ClipNotSet,
            Status.Error.ClipLengthNegative
        )
    }

    @Test
    fun `When clip is set to zero duration, status should be 'clip duration zero' error`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(100.seconds)
        ).apply {
            startMediaProgressListener()
            setSourceToLocal(getResourceAsPath("test.ts"))
        }
        val results = mutableListOf<Status>()
        backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
        viewModel.onClipStartSecondChanged("2", TextRange.Zero)
        viewModel.onClipEndSecondChanged("2", TextRange.Zero)
        assertThat(results).containsExactly(
            Status.Error.ClipNotSet,
            Status.Error.ClipLengthNegative,
            Status.Error.ClipLengthZero
        )
    }

    @Test
    fun `When clip start is larger than media length, status should be 'clip start after media end' error`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockMediaPlayer = spyk<MediaPlayer>()
        every { mockMediaPlayer.progress } returns flowOf(Progress(0.5f, 100.seconds))
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = mockMediaPlayer
        ).apply {
            startMediaProgressListener()
            setSourceToLocal(getResourceAsPath("test.ts"))
        }
        val results = mutableListOf<Status>()
        backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
        viewModel.onClipStartMinuteChanged("8", TextRange.Zero)
        viewModel.onClipEndMinuteChanged("9", TextRange.Zero)
        assertThat(results.first()).isEqualTo(Status.Error.ClipNotSet)
        assertThat(results.last()).isEqualTo(Status.Error.ClipStartAfterMediaEnd)
    }

    /**
     * This happens when the media is changed
     */
    @Disabled("Because when setting clip end to after media end it is coerced to media end, cannot get this test to work")
    @Test
    fun `When media length is zero and clip start is larger than media length, status error should be 'save file not set' rather than 'clip start after media end'`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val mockMediaPlayer = spyk<MediaPlayer>()
            every { mockMediaPlayer.progress } returns flowOf(Progress.ZERO)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = mockMediaPlayer
            ).apply {
                startMediaProgressListener()
            }
            val results = mutableListOf<Status>()
            backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
            viewModel.onClipStartMinuteChanged("8", TextRange.Zero)
            viewModel.onClipEndMinuteChanged("9", TextRange.Zero)
            assertThat(results).containsExactly(
                Status.Error.ClipNotSet,
                Status.Error.ClipLengthNegative,
                Status.Error.FileNotSet
            )
        }

    @Test
    fun `When clip is valid and save file is null, status should be 'save file not set' error`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(100.seconds)
        ).apply {
            startMediaProgressListener()
            setSourceToLocal(getResourceAsPath("test.ts"))
        }
        val results = mutableListOf<Status>()
        backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
        viewModel.onClipEndSecondChanged("1", TextRange.Zero)
        assertThat(results).containsExactly(
            Status.Error.ClipNotSet,
            Status.Error.FileNotSet
        )
    }

    @Test
    fun `When clip is valid and save file is set, status should be ready`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(100.seconds)
        ).apply {
            startMediaProgressListener()
            setSourceToLocal(getResourceAsPath("test.ts"))
        }
        val results = mutableListOf<Status>()
        backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
        viewModel.onClipEndSecondChanged("1", TextRange.Zero)
        viewModel.setSaveFile(Path("a/1.mp4"))
        assertThat(results).containsExactly(
            Status.Error.ClipNotSet,
            Status.Error.FileNotSet,
            Status.Ready
        )
    }

    @Test
    fun `When clip is valid and save file is set and conversion process is started, status should be in progress`() =
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
                println("I'm a fake converter and I'm running an infinite loop  pretending to convert...")
                while (isActive) {
                    delay(15.milliseconds)
                    println("Fake conversion in work...")
                    arg<ProgressListener>(7).onProgressUpdate(0f)
                }
            }
            val converterFactory = ConverterFactory { converter }
            val urlMaker = UrlMaker { URI("file://localhost").toURL() }
            val saveFile = Path("xyz") / "1.mp4"
            val sourcePath = getResourceAsPath("test.ts")
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                mediaPlayer = FakeMediaPlayer(100.seconds),
                converterFactory = converterFactory
            ).apply {
                startMediaProgressListener()
                setSourceToLocal(sourcePath)
            }
            val results = mutableListOf<Status>()
            backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
            viewModel.onClipEndSecondChanged("1", TextRange.Zero)
            viewModel.setSaveFile(saveFile)
            viewModel.startProcess()
            delay(50.milliseconds)
            assertThat(results).containsExactly(
                Status.Error.ClipNotSet,
                Status.Error.FileNotSet,
                Status.Ready,
                Status.InProgress(source = Source.Local(sourcePath), progress = 0f)
            )
        }

    @Test
    fun `When the conversion process is canceled, status should update to proper value`() = runTest {
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
            println("I'm a fake converter and I'm running an infinite loop  pretending to convert...")
            println("Fake conversion in work...")
            delay(1.seconds)
            println("Simulating being canceled by throwing CancellationException")
            throw CancellationException()
        }
        val converterFactory = ConverterFactory { converter }
        val urlMaker = UrlMaker { getResourceAsURL("test.ts") }
        val saveFile = Path("xyz") / "1.mp4"
        val sourcePath = getResourceAsPath("test.ts")
        val results = mutableListOf<Status>()
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            urlMaker = urlMaker,
            mediaPlayer = FakeMediaPlayer(100.seconds),
            converterFactory = converterFactory
        ).apply {
            startMediaProgressListener()
            onClipEndSecondChanged("14", TextRange.Zero)
            setFormat(Format.MP4)
            setSaveFile(saveFile)
            setSourceToLocal(sourcePath)
        }
        backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
        viewModel.startProcess()
        delay(5.seconds)
        assertThat(results).containsExactly(
            Status.Ready,
            Status.InProgress(source = Source.Local(sourcePath), progress = 0f),
            Status.Ready,
            Status.Error.FileNotSet
        )
    }

    @Test
    fun `When the conversion process succeeds, status should change to finish with success`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val converter = spyk(object : Converter(dispatcher) {
            override fun ffmpegOptions(
                quality: Quality,
                introOptions: IntroOptions,
                coverOptions: CoverOptions,
                flags: ConverterFlags
            ) = emptyList<FFmpegOption>()
        })
        coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } answers {
            println("I'm a fake converter and I'm pretending to convert...")
        }
        val converterFactory = ConverterFactory { converter }
        val urlMaker = UrlMaker { URI("file://localhost").toURL() }
        val saveFile = Path("xyz") / "1.mp4"
        val results = mutableListOf<Status>()
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            urlMaker = urlMaker,
            mediaPlayer = FakeMediaPlayer(100.seconds),
            converterFactory = converterFactory
        ).apply {
            startMediaProgressListener()
            onClipEndSecondChanged("14", TextRange.Zero)
            setFormat(Format.MP4)
            setSaveFile(saveFile)
            setSourceToLocal(getResourceAsPath("test.ts"))
        }
        backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
        viewModel.startProcess()
        assertThat(results.map { it::class }).containsExactly(
            Status.Ready::class,
            Status.Finished.Success::class
        )
    }

    @Tag("Flaky")
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Fails on CI; FIXME")
    @Test
    fun `When the conversion process is run multiple times with success, the time in success state should be a proper value with its start time reset every time`() =
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
                Thread.sleep(15)
            }
            val converterFactory = ConverterFactory { converter }
            val urlMaker = UrlMaker { URI("file://localhost").toURL() }
            val saveFile = Path("xyz") / "1.mp4"
            val results = mutableListOf<Status>()
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                mediaPlayer = FakeMediaPlayer(100.seconds),
                converterFactory = converterFactory
            ).apply {
                setSourceToLocal(getResourceAsPath("test.ts"))
                startMediaProgressListener()
                onClipEndSecondChanged("14", TextRange.Zero)
                setFormat(Format.MP4)
            }
            backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
            repeat(5) {
                viewModel.setSaveFile(saveFile)
                viewModel.startProcess()
            }
            assertThat((results.reversed()[1] as Status.Finished.Success).totalTime)
                .isBetween(10.milliseconds, 50.milliseconds)
        }

    @Test
    fun `When the conversion process fails, status should change to finish with failure`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val converter = spyk(object : Converter(dispatcher) {
            override fun ffmpegOptions(
                quality: Quality,
                introOptions: IntroOptions,
                coverOptions: CoverOptions,
                flags: ConverterFlags
            ) = emptyList<FFmpegOption>()
        })
        coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } throws Exception()
        val converterFactory = ConverterFactory { converter }
        val urlMaker = UrlMaker { URI("file://localhost").toURL() }
        val saveFile = Path("xyz") / "1.mp4"
        val results = mutableListOf<Status>()
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            urlMaker = urlMaker,
            mediaPlayer = FakeMediaPlayer(100.seconds),
            converterFactory = converterFactory
        ).apply {
            startMediaProgressListener()
            onClipEndSecondChanged("14", TextRange.Zero)
            setFormat(Format.MP4)
            setSaveFile(saveFile)
            setSourceToLocal(getResourceAsPath("test.ts"))
        }
        backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
        viewModel.startProcess()
        assertThat(results.map { it::class }).containsExactly(
            Status.Ready::class,
            Status.Finished.Failure::class
        )
    }

    @Test
    fun `After dismissing finish dialog, status should change to proper value`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val converter = spyk(object : Converter(dispatcher) {
            override fun ffmpegOptions(
                quality: Quality,
                introOptions: IntroOptions,
                coverOptions: CoverOptions,
                flags: ConverterFlags
            ) = emptyList<FFmpegOption>()
        })
        coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } answers {
            println("I'm a fake converter and I'm pretending to convert...")
        }
        val converterFactory = ConverterFactory { converter }
        val urlMaker = UrlMaker { URI("file://localhost").toURL() }
        val saveFile = Path("xyz") / "1.mp4"
        val results = mutableListOf<Status>()
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            urlMaker = urlMaker,
            mediaPlayer = FakeMediaPlayer(100.seconds),
            converterFactory = converterFactory
        ).apply {
            startMediaProgressListener()
            onClipEndSecondChanged("14", TextRange.Zero)
            setSourceToLocal(getResourceAsPath("test.ts"))
            setFormat(Format.MP4)
            setSaveFile(saveFile)
            startProcess()
        }
        backgroundScope.launch(dispatcher) { viewModel.status.toList(results) }
        viewModel.onFinishDialogDismissRequest()
        assertThat(results.map { it::class }).containsExactly(
            Status.Finished.Success::class,
            Status.Error.FileNotSet::class
        )
    }
}
