package ir.mahozad.cutcon.component

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import ir.mahozad.cutcon.decodeImage
import ir.mahozad.cutcon.getPixels
import ir.mahozad.cutcon.getResourceAsPath
import ir.mahozad.cutcon.model.Source
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.jupiter.api.*
import kotlin.io.path.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class MediaPlayerTest {

    private lateinit var mediaPlayer: MediaPlayer

    // TODO: Add tests for progress emissions as well

    @BeforeEach
    fun setUp() {
        mediaPlayer = DefaultMediaPlayer()
    }

    @AfterEach
    fun tearDown() {
        // This is required to prevent problems when running all tests
        mediaPlayer.terminate()
    }

    @Nested
    inner class ImageInputTest {
        @Test
        fun `When playing a PNG image, the output should update to image`() = runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val source = getResourceAsPath("test.png")
            val results = mutableListOf<MediaPlayer.Output>()
            backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
            mediaPlayer.play(source.let(Source::Local).let(DefaultUrlMaker::makeUrl))
            assertThat(results).hasSize(2)
            assertThat(results.first()).isEqualTo(MediaPlayer.Output.SourceNotStarted)
            assertThat((results[1] as MediaPlayer.Output.Image).image.getPixels())
                .isEqualTo(decodeImage(source).getPixels())
        }

        @Test
        fun `When a video is playing and playing a PNG image, the output should update to image`() = runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val source1 = getResourceAsPath("test.ts")
            val source2 = getResourceAsPath("test.png")
            mediaPlayer.play(source1.let(Source::Local).let(DefaultUrlMaker::makeUrl))
            val results = mutableListOf<MediaPlayer.Output>()
            backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
            mediaPlayer.play(source2.let(Source::Local).let(DefaultUrlMaker::makeUrl))
            assertThat(results).hasSize(4)
            assertThat(results[0]).isEqualTo(MediaPlayer.Output.SourceNotStarted)
            assertThat(results[1]).isInstanceOf(MediaPlayer.Output.Video::class.java)
            assertThat(results[2]).isEqualTo(MediaPlayer.Output.SourceNotStarted)
            assertThat((results[3] as MediaPlayer.Output.Image).image.getPixels())
                .isEqualTo(decodeImage(source2).getPixels())
        }

        @Disabled("Because most of the time the test gets stuck")
        @Test
        fun `When playing an SVG image, the output should update to the image and its size be equal to the SVG intrinsic size`() =
            runTest {
                val dispatcher = UnconfinedTestDispatcher(testScheduler)
                val source = getResourceAsPath("test.svg")
                val results = mutableListOf<MediaPlayer.Output>()
                backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
                mediaPlayer.play(source.let(Source::Local).let(DefaultUrlMaker::makeUrl))
                assertThat(results).hasSize(2)
                assertThat(results.first()).isEqualTo(MediaPlayer.Output.SourceNotStarted)
                assertThat((results.last() as MediaPlayer.Output.Image).image.getPixels())
                    .isEqualTo(decodeImage(source).getPixels())
            }
    }

    @Test
    fun `When playing a video, the output should update to video frames`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val source = getResourceAsPath("test.ts")
        val results = mutableListOf<MediaPlayer.Output>()
        backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
        mediaPlayer.play(source.let(Source::Local).let(DefaultUrlMaker::makeUrl))
        assertThat(results).hasSize(2)
        assertThat(results.first()).isEqualTo(MediaPlayer.Output.SourceNotStarted)
        assertThat(results.last()).isInstanceOf(MediaPlayer.Output.Video::class.java)
        assertThat((results.last() as MediaPlayer.Output.Video).video.take(3).toList()).hasSize(3)
    }

    @Disabled
    @Test
    fun `Test for actual video frames`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        // create video from still images: ffmpeg.exe -r 1 -i ./%d.png  -vf "fps=2,format=yuv420p" out.mp4
        val source = getResourceAsPath("test.mp4")
        val results = mutableListOf<MediaPlayer.Output>()
        backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
        mediaPlayer.play(source.let(Source::Local).let(DefaultUrlMaker::makeUrl))
        val actualFrames = (results[1] as MediaPlayer.Output.Video)
            .video
            .filterNotNull()
            .take(2)
            .toList()
            .map(ImageBitmap::asSkiaBitmap)
            .map(Image::makeFromBitmap)
            .mapNotNull { it.encodeToData(EncodedImageFormat.PNG) }
            .map(Data::bytes)

        val referenceFrame1 = getResourceAsPath("reference/14.png").readBytes()
        val referenceFrame2 = getResourceAsPath("reference/15.png").readBytes()

        val actualPath1 = Path("screenshot1.png")
        actualPath1.writeBytes(referenceFrame1)
        val actualPath2 = Path("screenshot2.png")
        actualPath2.writeBytes(referenceFrame2)

        assertThat(actualFrames).contains(referenceFrame1, referenceFrame2)
    }

    @Test
    fun `When a media is playing and playing a new media, the output should update to 'no source' and then to the new media`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val source1 = getResourceAsPath("test.png")
            val source2 = getResourceAsPath("test.ts")
            val results = mutableListOf<MediaPlayer.Output>()
            backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
            mediaPlayer.play(source1.let(Source::Local).let(DefaultUrlMaker::makeUrl))
            mediaPlayer.play(source2.let(Source::Local).let(DefaultUrlMaker::makeUrl))
            assertThat(results).hasSize(4)
            assertThat(results.first()).isEqualTo(MediaPlayer.Output.SourceNotStarted)
            assertThat(results[1]).isInstanceOf(MediaPlayer.Output.Image::class.java)
            assertThat(results[2]).isInstanceOf(MediaPlayer.Output.SourceNotStarted::class.java)
            assertThat(results[3]).isInstanceOf(MediaPlayer.Output.Video::class.java)
        }

    @Test
    fun `When playing a GIF, the output should update to GIF frames`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val source = getResourceAsPath("test.gif")
        val results = mutableListOf<MediaPlayer.Output>()
        backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
        mediaPlayer.play(source.let(Source::Local).let(DefaultUrlMaker::makeUrl))
        assertThat(results).hasSize(2)
        assertThat(results.first()).isEqualTo(MediaPlayer.Output.SourceNotStarted)
        assertThat(results[1]).isInstanceOf(MediaPlayer.Output.Video::class.java)
        assertThat((results[1] as MediaPlayer.Output.Video).video.take(3).toList()).hasSize(3)
    }

    @Test
    fun `When playing an audio, it should play properly`() =
        runTest(timeout = 5.seconds /* To fail faster because this test fails by getting stuck instead of throwing an exception */) {
            val source = getResourceAsPath("test.mp3")
            mediaPlayer.play(source.let(Source::Local).let(DefaultUrlMaker::makeUrl))
            val progress = mediaPlayer
                .progress
                .filter { it.length > Duration.ZERO && it.fraction > 0f }
                .first()
            assertThat(progress.fraction).isGreaterThan(0f)
        }

    @Test
    fun `When playing an audio containing cover art, the output should update to cover art`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val source = getResourceAsPath("test.mp3")
        val results = mutableListOf<MediaPlayer.Output>()
        backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
        mediaPlayer.play(source.let(Source::Local).let(DefaultUrlMaker::makeUrl))
        assertThat(results).hasSize(2)
        assertThat(results.first()).isEqualTo(MediaPlayer.Output.SourceNotStarted)
        assertThat(results[1]).isInstanceOf(MediaPlayer.Output.Image::class.java)
    }

    @Test
    fun `When playing an audio with no cover art, the output should update to 'source has no image'`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val source = getResourceAsPath("test-no-cover.mp3")
        val results = mutableListOf<MediaPlayer.Output>()
        backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
        mediaPlayer.play(source.let(Source::Local).let(DefaultUrlMaker::makeUrl))
        assertThat(results).hasSize(2)
        assertThat(results.first()).isEqualTo(MediaPlayer.Output.SourceNotStarted)
        assertThat(results[1]).isEqualTo(MediaPlayer.Output.SourceHasNoImage)
    }

    @Test
    fun `When a video is being played and playing a new media, the video flow in output should finish (close)`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val video = getResourceAsPath("test.ts")
            val image = getResourceAsPath("test.png")
            mediaPlayer.play(video.let(Source::Local).let(DefaultUrlMaker::makeUrl))
            var isFinished = false
            backgroundScope.launch(dispatcher) {
                mediaPlayer.output.collectIndexed { index, output ->
                    if (index == 1) {
                        (output as MediaPlayer.Output.Video)
                            .video
                            .onCompletion { isFinished = true }
                            .collect()
                    }
                }
            }
            mediaPlayer.play(image.let(Source::Local).let(DefaultUrlMaker::makeUrl))
            assertThat(isFinished).isTrue()
        }

    @Test
    fun `When the media file name contains non-ASCII characters, it should play properly`() =
        runTest(timeout = 5.seconds /* To fail faster because this test fails by getting stuck instead of throwing an exception */) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val tempDirectory = createTempDirectory()
            val source = getResourceAsPath("test.ts")
                .copyTo(tempDirectory / "یک پرونده رسانه با نام فارسی و حاوی نویسه فاصله.ts")
            val results = mutableListOf<MediaPlayer.Output>()
            backgroundScope.launch(dispatcher) { mediaPlayer.output.toList(results) }
            mediaPlayer.play(source.let(Source::Local).let(DefaultUrlMaker::makeUrl))
            assertThat(results).hasSize(2)
            assertThat(results.first()).isEqualTo(MediaPlayer.Output.SourceNotStarted)
            assertThat(results[1]).isInstanceOf(MediaPlayer.Output.Video::class.java)
            assertThat((results[1] as MediaPlayer.Output.Video).video.take(3).toList()).hasSize(3)
        }
}
