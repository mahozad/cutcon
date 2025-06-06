package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import ir.mahozad.cutcon.component.MediaPlayer
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.component.*
import ir.mahozad.cutcon.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jaudiotagger.audio.AudioFileIO
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
abstract class MediaInfoTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initialize() {
            mockkObject(SystemDateTime)
        }

        @JvmStatic
        @AfterAll
        fun terminate() {
            unmockkAll()
        }
    }

    @Test
    fun `Media info should initially have proper value`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.mediaInfo.first()
        assertThat(result).isEqualTo(constructMediaInfo())
    }

    @Test
    fun `When media is paused and media url changes, isResumed should update to true because VLC automatically starts playing the new media when a new URL is set`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = DefaultUrlMaker
            ).apply {
                startUrlMaker()
                toggleResume()
            }
            val results = mutableListOf<MediaInfo>()
            backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
            viewModel.setSourceToLocal(Path("abc.xyz"))
            assertThat(results)
                .last()
                .extracting(MediaInfo::isResumed)
                .isEqualTo(true)
        }

    @Test
    fun `When local file source is set, media url should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            urlMaker = DefaultUrlMaker
        ).apply {
            startUrlMaker()
        }
        val results = mutableListOf<MediaInfo>()
        val path = Path("a/1.mp4")
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.setSourceToLocal(path)
        assertThat(results.last().url.toString())
            .isEqualTo("file://localhost/${path.absolute().invariantSeparatorsPathString}")
    }

    @Test
    fun `When toggling resume, media info should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val isResumed = MutableStateFlow(false)
        val mediaPlayer = spyk<MediaPlayer>()
        every { mediaPlayer.isResumed } returns isResumed
        every { mediaPlayer.toggleResume() } answers { isResumed.update { !it } }
        val viewModel = constructMainViewModel(dispatcher, mediaPlayer = mediaPlayer)
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.toggleResume()
        assertThat(results.map(MediaInfo::isResumed)).containsExactly(false, true)
    }

    @Test
    fun `When toggling audio mute, media info should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.toggleAudioMute()
        assertThat(results[1]).isEqualTo(
            constructMediaInfo(isAudioMuted = !defaultIsAudioMuted)
        )
    }

    @Test
    fun `When setting seek to new value, media info progress should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(100.seconds)
        ).apply {
            startMediaProgressListener()
        }
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.setSeek(0.42f)
        assertThat(results[1].progress.fraction).isEqualTo(0.42f)
    }

    @Test
    fun `When clip loop was on and now is off, setting seek to a value after clip end, media info progress should update to the seek`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(100.seconds)
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("56", TextRange.Zero)
                toggleClipLoop()
                toggleClipLoop()
            }
            val results = mutableListOf<MediaInfo>()
            backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
            viewModel.setSeek(0.88f)
            assertThat(results.last().progress.fraction).isEqualTo(0.88f)
        }

    @Test
    fun `When setting audio volume to new value, media info should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.setVolume(0.83f)
        assertThat(results[1]).isEqualTo(
            constructMediaInfo(audioVolume = 0.83f)
        )
    }

    @Test
    fun `When setting speed to new value, media info speed should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.setSpeed(Speed.FAST2_5)
        assertThat(results[1]).isEqualTo(
            constructMediaInfo(speed = Speed.FAST2_5)
        )
    }

    @Test
    fun `When no previous speed was set and resetting speed, media info speed should reset to 'default fast'`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher)
            val results = mutableListOf<MediaInfo>()
            backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
            viewModel.resetSpeed()
            assertThat(results[1]).isEqualTo(
                constructMediaInfo(speed = defaultFastSpeed)
            )
        }

    @Test
    fun `When speed is not the default and resetting speed, media info speed should reset to default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            setSpeed(Speed.FAST2_5)
        }
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.resetSpeed()
        assertThat(results[1]).isEqualTo(
            constructMediaInfo()
        )
    }

    @Test
    fun `When speed is set to non-default and then set to default and then resetting speed, media info speed should become the last non-default speed`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setSpeed(Speed.FAST3_0)
                setSpeed(defaultSpeed)
            }
            val results = mutableListOf<MediaInfo>()
            backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
            viewModel.resetSpeed()
            assertThat(results[1]).isEqualTo(
                constructMediaInfo(speed = Speed.FAST3_0)
            )
        }

    @Test
    fun `When no previous speed was set and resetting speed for two times, media info speed should become the default speed`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher)
            val results = mutableListOf<MediaInfo>()
            backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
            viewModel.resetSpeed()
            viewModel.resetSpeed()
            assertThat(results[2]).isEqualTo(
                constructMediaInfo()
            )
        }

    @Test
    fun `When clip looping is on and setting clip start to a new value, clip loop should update to null`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(100.seconds)
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("2", TextRange.Zero)
                toggleClipLoop()
            }
            val results = mutableListOf<MediaInfo>()
            backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
            viewModel.onClipStartSecondChanged("1", TextRange.Zero)
            assertThat(results.last()).isEqualTo(
                constructMediaInfo(
                    progress = Progress(fraction = 0f, 100.seconds),
                    clipToLoop = null
                )
            )
        }

    @Test
    fun `When clip looping is on and setting clip end to a new value, clip loop should update to null`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(100.seconds)
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("2", TextRange.Zero)
                toggleClipLoop()
            }
            val results = mutableListOf<MediaInfo>()
            backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
            viewModel.onClipEndSecondChanged("6", TextRange.Zero)
            assertThat(results.last()).isEqualTo(
                constructMediaInfo(
                    progress = Progress(fraction = 0f, 100.seconds),
                    clipToLoop = null
                )
            )
        }

    @Test
    fun `After toggling on clip loop, progress should update to clip start time`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(100.seconds)
        ).apply {
            startMediaProgressListener()
            onClipStartSecondChanged("7", TextRange.Zero)
            onClipEndSecondChanged("13", TextRange.Zero)
            setSeek(0.5f) // Also triggers media progress update
        }
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.toggleClipLoop()
        assertThat(results.last()).isEqualTo(
            constructMediaInfo(
                progress = Progress(0.07f, 100.seconds),
                clipToLoop = Clip(7.seconds, 13.seconds)
            )
        )
    }

    @Test
    fun `After toggling off clip loop, progress should not change`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(100.seconds)
        ).apply {
            startMediaProgressListener()
            onClipStartSecondChanged("7", TextRange.Zero)
            onClipEndSecondChanged("13", TextRange.Zero)
            toggleClipLoop()
        }
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.setSeek(0.1f)
        viewModel.toggleClipLoop()
        assertThat(results.last()).isEqualTo(
            constructMediaInfo(
                progress = Progress(0.1f, 100.seconds),
                clipToLoop = null
            )
        )
    }

    @Test
    fun `When clip loop is on and seeking to a time before clip start, progress should update to clip start`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = FakeMediaPlayer(100.seconds)
            ).apply {
                startMediaProgressListener()
                onClipStartSecondChanged("24", TextRange.Zero)
                onClipEndSecondChanged("56", TextRange.Zero)
                setSeek(0.5f)
                toggleClipLoop()
            }
            val results = mutableListOf<MediaInfo>()
            backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
            viewModel.setSeek(0.1f)
            assertThat(results.last()).isEqualTo(
                constructMediaInfo(
                    progress = Progress(0.24f, 100.seconds),
                    clipToLoop = Clip(24.seconds, 56.seconds)
                )
            )
        }

    @Test
    fun `When clip loop is on and seeking to a time in clip bounds, seek should update to that`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(100.seconds)
        ).apply {
            startMediaProgressListener()
            onClipStartSecondChanged("24", TextRange.Zero)
            onClipEndSecondChanged("56", TextRange.Zero)
            setSeek(0.5f)
            toggleClipLoop()
        }
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.setSeek(0.39f)
        assertThat(results.last()).isEqualTo(
            constructMediaInfo(
                progress = Progress(0.39f, 100.seconds),
                clipToLoop = Clip(24.seconds, 56.seconds)
            )
        )
    }

    @Test
    fun `When clip loop is on and seeking to a time after clip end, seek should update to clip end`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = FakeMediaPlayer(100.seconds)
        ).apply {
            startMediaProgressListener()
            onClipStartSecondChanged("24", TextRange.Zero)
            onClipEndSecondChanged("56", TextRange.Zero)
            setSeek(0.5f)
            toggleClipLoop()
        }
        val results = mutableListOf<MediaInfo>()
        backgroundScope.launch(dispatcher) { viewModel.mediaInfo.toList(results) }
        viewModel.setSeek(0.9f)
        assertThat(results.last()).isEqualTo(
            constructMediaInfo(
                progress = Progress(0.56f, 100.seconds),
                clipToLoop = Clip(24.seconds, 56.seconds)
            )
        )
    }

    @Test
    fun `When source is set to a local file containing album art, display image should update to the local file album art`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val mockMediaPlayer = spyk<MediaPlayer>()
            val fakeMediaImage = decodeImage(getResourceAsPath("test.png"))!!
            val fakeMediaPlayerOutput = MutableStateFlow<MediaPlayer.Output>(MediaPlayer.Output.SourceNotStarted)
            val localFile = getResourceAsPath("test.mp3")
            // FIXME: Duplicate of code in MediaPlayer
            val localFileAlbumArt = localFile
                .toFile()
                .runCatching(AudioFileIO::read)
                .getOrNull()
                ?.tag
                ?.firstArtwork
                ?.binaryData
                ?.inputStream()
                ?.use(::loadImageBitmap)!!
            every { mockMediaPlayer.output } returns fakeMediaPlayerOutput
            every { mockMediaPlayer.play(any()) } coAnswers  {
                fakeMediaPlayerOutput.value = MediaPlayer.Output.SourceNotStarted
                delay(10.milliseconds)
                if ("test.png" in args.single().toString()) {
                    fakeMediaPlayerOutput.value = MediaPlayer.Output.Video(flowOf(fakeMediaImage))
                } else {
                    fakeMediaPlayerOutput.value = MediaPlayer.Output.Image(localFileAlbumArt)
                }
            }
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = mockMediaPlayer,
                urlMaker = DefaultUrlMaker
            ).apply {
                setSourceToLocal(Path("test.png"))
                startUrlMaker()
            }
            val results = mutableListOf<ImageBitmap?>()
            backgroundScope.launch(dispatcher) { viewModel.displayImage.toList(results) }
            viewModel.setSourceToLocal(localFile)
            advanceTimeBy(1.minutes)
            assertThat(results.map(ImageBitmap?::getPixels)).containsExactly(
                fakeMediaImage.getPixels(),
                null,
                localFileAlbumArt.getPixels()
            )
        }

    @Test
    fun `When source is local containing album art and it is set to a local audio file with no album art, display image should update to default display image`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val mockMediaPlayer = spyk<MediaPlayer>()
            val fakeMediaPlayerOutput = MutableStateFlow<MediaPlayer.Output>(MediaPlayer.Output.SourceNotStarted)
            val localFile = getResourceAsPath("test.mp3")
            val localFileAlbumArt = localFile
                .toFile()
                .runCatching(AudioFileIO::read)
                .getOrNull()
                ?.tag
                ?.firstArtwork
                ?.binaryData
                ?.inputStream()
                ?.use(::loadImageBitmap)!!
            every { mockMediaPlayer.output } returns fakeMediaPlayerOutput
            every { mockMediaPlayer.play(any()) } coAnswers  {
                fakeMediaPlayerOutput.value = MediaPlayer.Output.SourceNotStarted
                delay(10.milliseconds)
                if ("test.mp3" in args.single().toString()) {
                    fakeMediaPlayerOutput.value = MediaPlayer.Output.Image(localFileAlbumArt)
                } else {
                    fakeMediaPlayerOutput.value = MediaPlayer.Output.SourceHasNoImage
                }
            }
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = mockMediaPlayer,
                urlMaker = DefaultUrlMaker
            ).apply {
                setSourceToLocal(localFile)
                startUrlMaker()
            }
            val results = mutableListOf<ImageBitmap?>()
            backgroundScope.launch(dispatcher) { viewModel.displayImage.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test-no-cover.mp3"))
            advanceTimeBy(1.minutes)
            assertThat(results.map(ImageBitmap?::getPixels)).containsExactly(
                localFileAlbumArt.getPixels(),
                null,
                defaultMusicCoverArt.getPixels()
            )
        }

    @Test
    fun `When source is local containing album art and it is set to a local video file, display image should update to video frame`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val mockMediaPlayer = spyk<MediaPlayer>()
            val fakeMediaPlayerOutput = MutableStateFlow<MediaPlayer.Output>(MediaPlayer.Output.SourceNotStarted)
            val fakeMediaImage = decodeImage(getResourceAsPath("test.png"))
            val localFile = getResourceAsPath("test.mp3")
            val localFileAlbumArt = localFile
                .toFile()
                .runCatching(AudioFileIO::read)
                .getOrNull()
                ?.tag
                ?.firstArtwork
                ?.binaryData
                ?.inputStream()
                ?.use(::loadImageBitmap)!!
            every { mockMediaPlayer.output } returns fakeMediaPlayerOutput
            every { mockMediaPlayer.play(any()) } coAnswers  {
                fakeMediaPlayerOutput.value = MediaPlayer.Output.SourceNotStarted
                delay(10.milliseconds)
                if ("test.mp3" in args.single().toString()) {
                    fakeMediaPlayerOutput.value = MediaPlayer.Output.Image(localFileAlbumArt)
                } else {
                    fakeMediaPlayerOutput.value = MediaPlayer.Output.Video(flowOf(fakeMediaImage))
                }
            }
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = mockMediaPlayer,
                urlMaker = DefaultUrlMaker
            ).apply {
                setSourceToLocal(localFile)
                startUrlMaker()
            }
            val results = mutableListOf<ImageBitmap?>()
            backgroundScope.launch(dispatcher) { viewModel.displayImage.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test.ts"))
            advanceTimeBy(1.minutes)
            assertThat(results.map(ImageBitmap?::getPixels)).containsExactly(
                localFileAlbumArt.getPixels(),
                null,
                fakeMediaImage.getPixels()
            )
        }

    @Test
    fun `When source is set to a local still image, display image should update to the image`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockMediaPlayer = spyk<MediaPlayer>()
        val fakeMediaPlayerOutput = MutableStateFlow<MediaPlayer.Output>(MediaPlayer.Output.SourceNotStarted)
        val fakeMediaImage = decodeImage(getResourceAsPath("test-wide.png"))
        val localFile = getResourceAsPath("test.png")
        every { mockMediaPlayer.output } returns fakeMediaPlayerOutput
        every { mockMediaPlayer.play(any()) } coAnswers  {
            fakeMediaPlayerOutput.value = MediaPlayer.Output.SourceNotStarted
            delay(10.milliseconds)
            if ("test-wide.png" in args.single().toString()) {
                fakeMediaPlayerOutput.value = MediaPlayer.Output.Video(flowOf(fakeMediaImage))
            } else {
                fakeMediaPlayerOutput.value = MediaPlayer.Output.Image(decodeImage(localFile)!!)
            }
        }
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = mockMediaPlayer,
            urlMaker = DefaultUrlMaker
        ).apply {
            setSourceToLocal(Path("test-wide.png"))
            startUrlMaker()
        }
        val results = mutableListOf<ImageBitmap?>()
        backgroundScope.launch(dispatcher) { viewModel.displayImage.toList(results) }
        viewModel.setSourceToLocal(localFile)
        advanceTimeBy(1.minutes)
        assertThat(results.map(ImageBitmap?::getPixels)).containsExactly(
            fakeMediaImage.getPixels(),
            null,
            decodeImage(localFile).getPixels()
        )
    }

    @Test
    fun `When source is set to a local GIF image, display image should update to the GIF frames`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockMediaPlayer = spyk<MediaPlayer>()
        val fakeMediaPlayerOutput = MutableStateFlow<MediaPlayer.Output>(MediaPlayer.Output.SourceNotStarted)
        val fakeMediaImage = decodeImage(getResourceAsPath("test.png"))
        val localFile = getResourceAsPath("test.gif")
        every { mockMediaPlayer.output } returns fakeMediaPlayerOutput
        every { mockMediaPlayer.play(any()) } coAnswers {
            fakeMediaPlayerOutput.value = MediaPlayer.Output.SourceNotStarted
            delay(10.milliseconds)
            if ("test.png" in args.single().toString()) {
                fakeMediaPlayerOutput.value = MediaPlayer.Output.Video(flowOf(fakeMediaImage))
            } else {
                fakeMediaPlayerOutput.value = MediaPlayer.Output.Video(flowOf(decodeImage(localFile)))
            }
        }
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            mediaPlayer = mockMediaPlayer,
            urlMaker = DefaultUrlMaker
        ).apply {
            setSourceToLocal(Path("test.png"))
            startUrlMaker()
        }
        val results = mutableListOf<ImageBitmap?>()
        backgroundScope.launch(dispatcher) { viewModel.displayImage.toList(results) }
        viewModel.setSourceToLocal(localFile)
        advanceTimeBy(1.minutes)
        assertThat(results.map(ImageBitmap?::getPixels)).containsExactly(
            fakeMediaImage.getPixels(),
            null,
            decodeImage(localFile).getPixels()
        )
    }

    @Test
    fun `When the current media progress is greater than 60 minutes, setting clip start to now should update clip start minute input to proper value`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val mockMediaPlayer = spyk<MediaPlayer>()
            every { mockMediaPlayer.progress } returns flowOf(Progress(0.97f, 65.minutes))
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = mockMediaPlayer
            ).apply {
                startMediaProgressListener()
            }
            val results = mutableListOf<TextFieldValue>()
            backgroundScope.launch(dispatcher) { viewModel.clipStartMinuteInput.toList(results) }
            viewModel.onSetClipStartToNow()
            assertThat(results.last()).isEqualTo(TextFieldValue(defaultLanguage.localizeDigits("63")))
        }

    @Test
    fun `When the current media progress is greater than 60 minutes, setting clip end to now should update clip end minute input to proper value`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val mockMediaPlayer = spyk<MediaPlayer>()
            every { mockMediaPlayer.progress } returns flowOf(Progress(0.97f, 65.minutes))
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                mediaPlayer = mockMediaPlayer
            ).apply {
                startMediaProgressListener()
            }
            val results = mutableListOf<TextFieldValue>()
            backgroundScope.launch(dispatcher) { viewModel.clipEndMinuteInput.toList(results) }
            viewModel.onSetClipEndToNow()
            assertThat(results.last()).isEqualTo(TextFieldValue(defaultLanguage.localizeDigits("63")))
        }
}
