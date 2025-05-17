package ir.mahozad.cutcon.converter

import androidx.compose.ui.graphics.Color
import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.ImageComparisonUtil
import io.mockk.spyk
import io.mockk.verifyOrder
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jaudiotagger.audio.AudioFileIO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS
import org.junit.jupiter.api.io.TempDir
import java.net.URI
import java.net.URL
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ConvertersTest {

    // See the README
    @TempDir(cleanup = ON_SUCCESS)
    lateinit var tempDirectory: Path

    @Nested
    inner class ConverterFactoryTest {
        @Test
        fun `StandardFactory should create correct converter given MP3 type`() {
            val format = Format.MP3
            val factory = DefaultConverterFactory(Dispatchers.Main)
            val converter = factory.createFor(format)
            assertThat(converter).isInstanceOf(Mp3Converter::class.java)
        }

        @Test
        fun `StandardFactory should create correct converter given MP4 type`() {
            val format = Format.MP4
            val factory = DefaultConverterFactory(Dispatchers.Main)
            val converter = factory.createFor(format)
            assertThat(converter).isInstanceOf(Mp4Converter::class.java)
        }

        @Test
        fun `StandardFactory should create correct converter given RAW aka ORIGINAL aka SOURCE aka COPY type`() {
            val format = Format.RAW
            val factory = DefaultConverterFactory(Dispatchers.Main)
            val converter = factory.createFor(format)
            assertThat(converter).isInstanceOf(RawConverter::class.java)
        }
    }

    @Nested
    inner class VideoInputTest {
        @Test
        fun `Converting to MP4 with no intro and no watermark should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test.ts")
            val output = tempDirectory / "result.mp4"
            Mp4Converter(dispatcher).convert(
                input = input,
                clip = Clip(6.seconds, 8.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions,
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            extractFrame(from = output, time = "00:00:00", into = tempDirectory / "frame.png")
            val reference = ImageComparisonUtil.readImageFromResources("reference/1.png")
            val actual = (tempDirectory / "frame.png").inputStream().use(ImageIO::read)
            val comparisonResult = ImageComparison(reference, actual).compareImages()
            assertThat(comparisonResult.differencePercent).isLessThan(1f)
        }

        @Test
        fun `Converting to MP4 with no intro and a watermark (with options) should succeed`() =
            runTest(timeout = testTimeout) {
                val dispatcher = UnconfinedTestDispatcher(testScheduler)
                val input = getResourceAsURL("test.ts")
                val image = getResourceAsPath("test.png")
                val output = tempDirectory / "result.mp4"
                Mp4Converter(dispatcher).convert(
                    input = input,
                    clip = Clip(6.seconds, 8.seconds),
                    intro = defaultIntroOptions,
                    cover = CoverOptions(
                        path = image,
                        scale = 2.3f,
                        opacity = 0.4f,
                        position = WatermarkPosition.BOTTOM_MIDDLE
                    ),
                    quality = Quality.MEDIUM,
                    fixInterlaced = false,
                    output = output,
                    listener = {}
                )
                extractFrame(from = output, time = "00:00:00", into = tempDirectory / "frame.png")
                val reference = ImageComparisonUtil.readImageFromResources("reference/2.png")
                val actual = (tempDirectory / "frame.png").inputStream().use(ImageIO::read)
                val comparisonResult = ImageComparison(reference, actual).compareImages()
                assertThat(comparisonResult.differencePercent).isLessThan(1f)
            }

        @Test
        fun `Converting to MP4 with an intro (with options) and no watermark should succeed`() =
            runTest(timeout = testTimeout) {
                val dispatcher = UnconfinedTestDispatcher(testScheduler)
                val input = getResourceAsURL("test.ts")
                val image = getResourceAsPath("test-wide.png")
                val output = tempDirectory / "result.mp4"
                Mp4Converter(dispatcher).convert(
                    input = input,
                    clip = Clip(6.seconds, 8.seconds),
                    intro = IntroOptions(
                        path = image,
                        duration = 3.seconds,
                        backgroundColor = Color.Yellow
                    ),
                    cover = defaultCoverOptions,
                    quality = Quality.MEDIUM,
                    fixInterlaced = false,
                    output = output,
                    listener = {}
                )
                extractFrame(from = output, time = "00:00:02", into = tempDirectory / "frame1.png")
                extractFrame(from = output, time = "00:00:04", into = tempDirectory / "frame2.png")
                val reference1 = ImageComparisonUtil.readImageFromResources("reference/3.png")
                val reference2 = ImageComparisonUtil.readImageFromResources("reference/4.png")
                val actual1 = (tempDirectory / "frame1.png").inputStream().use(ImageIO::read)
                val actual2 = (tempDirectory / "frame2.png").inputStream().use(ImageIO::read)
                val comparisonResult1 = ImageComparison(reference1, actual1).compareImages()
                val comparisonResult2 = ImageComparison(reference2, actual2).compareImages()
                assertThat(comparisonResult1.differencePercent).isLessThan(1f)
                assertThat(comparisonResult2.differencePercent).isLessThan(1f)
            }

        @Test
        fun `Converting to MP4 with an intro (with options) and a watermark (with options) should succeed`() =
            runTest(timeout = testTimeout) {
                val dispatcher = UnconfinedTestDispatcher(testScheduler)
                val input = getResourceAsURL("test.ts")
                val intro = getResourceAsPath("test-wide.png")
                val watermark = getResourceAsPath("test.png")
                val output = tempDirectory / "result.mp4"
                Mp4Converter(dispatcher).convert(
                    input = input,
                    clip = Clip(6.seconds, 8.seconds),
                    intro = IntroOptions(
                        path = intro,
                        duration = 3.seconds,
                        backgroundColor = Color.Yellow
                    ),
                    cover = CoverOptions(
                        path = watermark,
                        scale = 2.3f,
                        opacity = 0.4f,
                        position = WatermarkPosition.BOTTOM_MIDDLE
                    ),
                    quality = Quality.MEDIUM,
                    fixInterlaced = false,
                    output = output,
                    listener = {}
                )
                extractFrame(from = output, time = "00:00:02", into = tempDirectory / "frame1.png")
                extractFrame(from = output, time = "00:00:04", into = tempDirectory / "frame2.png")
                val reference1 = ImageComparisonUtil.readImageFromResources("reference/5.png")
                val reference2 = ImageComparisonUtil.readImageFromResources("reference/6.png")
                val actual1 = (tempDirectory / "frame1.png").inputStream().use(ImageIO::read)
                val actual2 = (tempDirectory / "frame2.png").inputStream().use(ImageIO::read)
                val comparisonResult1 = ImageComparison(reference1, actual1).compareImages()
                val comparisonResult2 = ImageComparison(reference2, actual2).compareImages()
                assertThat(comparisonResult1.differencePercent).isLessThan(1f)
                assertThat(comparisonResult2.differencePercent).isLessThan(1f)
            }

        @Test
        fun `Converting to MP4 with an intro (with options) that is larger than the video resolution and a watermark (with options) that is larger than the video resolution should succeed`() =
            runTest(timeout = testTimeout) {
                val dispatcher = UnconfinedTestDispatcher(testScheduler)
                val input = getResourceAsURL("test.ts")
                val intro = convertSvgToPng(getResourceAsPath("test.svg"), size = 1000f)
                val watermark = convertSvgToPng(getResourceAsPath("test.svg"), size = 1000f)
                val output = tempDirectory / "result.mp4"
                Mp4Converter(dispatcher).convert(
                    input = input,
                    clip = Clip(6.seconds, 8.seconds),
                    intro = IntroOptions(
                        path = intro,
                        duration = 3.seconds,
                        backgroundColor = Color.Yellow
                    ),
                    cover = CoverOptions(
                        path = watermark,
                        scale = 2.1f,
                        opacity = 0.4f,
                        position = WatermarkPosition.BOTTOM_MIDDLE
                    ),
                    quality = Quality.MEDIUM,
                    fixInterlaced = false,
                    output = output,
                    listener = {}
                )
                extractFrame(from = output, time = "00:00:02", into = tempDirectory / "frame1.png")
                extractFrame(from = output, time = "00:00:04", into = tempDirectory / "frame2.png")
                val reference1 = ImageComparisonUtil.readImageFromResources("reference/8.png")
                val reference2 = ImageComparisonUtil.readImageFromResources("reference/9.png")
                val actual1 = (tempDirectory / "frame1.png").inputStream().use(ImageIO::read)
                val actual2 = (tempDirectory / "frame2.png").inputStream().use(ImageIO::read)
                val comparisonResult1 = ImageComparison(reference1, actual1).compareImages()
                val comparisonResult2 = ImageComparison(reference2, actual2).compareImages()
                assertThat(comparisonResult1.differencePercent).isLessThan(1f)
                assertThat(comparisonResult2.differencePercent).isLessThan(1f)
            }

        @Test
        fun `Converting to MP3 with no album art should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test.ts")
            val output = tempDirectory / "result.mp3"
            Mp3Converter(dispatcher).convert(
                input = input,
                clip = Clip(6.seconds, 8.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions,
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            val albumArt = output
                .toFile()
                .runCatching(AudioFileIO::read)
                .getOrNull()
                ?.tag
                ?.firstArtwork
                ?.binaryData
            assertThat(albumArt).isEqualTo(Mp3Converter.defaultAlbumArtPath?.readBytes())
            assertThat(output.fileSize()).isBetween(50_000, 100_000)
        }

        @Test
        fun `Converting to MP3 with an album art should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test.ts")
            val image = getResourceAsPath("test.png")
            val output = tempDirectory / "result.mp3"
            Mp3Converter(dispatcher).convert(
                input = input,
                clip = Clip(6.seconds, 8.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions.copy(path = image),
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            val albumArt = output
                .toFile()
                .runCatching(AudioFileIO::read)
                .getOrNull()
                ?.tag
                ?.firstArtwork
                ?.binaryData
            assertThat(albumArt).isEqualTo(image.readBytes())
            assertThat(output.fileSize()).isGreaterThan(50_000)
        }

        @Test
        fun `Converting to RAW should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test.ts")
            val output = tempDirectory / "result.ts"
            RawConverter(dispatcher).convert(
                input = input,
                clip = Clip(6.seconds, 8.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions,
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            extractFrame(from = output, time = "00:00:00", into = tempDirectory / "frame.png")
            val reference = getResourceAsPath("reference/7.png")
            assertThat((tempDirectory / "frame.png").readBytes()).isEqualTo(reference.readBytes())
        }
    }

    @Nested
    inner class AudioInputTest {
        @Test
        fun `Converting a TS audio file to MP4 should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test-no-video.ts")
            val output = tempDirectory / "result.mp4"
            Mp4Converter(dispatcher).convert(
                input = input,
                clip = Clip(6.seconds, 10.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions,
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            assertThat(output.fileSize()).isBetween(20_000, 30_000)
        }

        @Test
        fun `Converting an MP3 audio file with album art to MP4 should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test.mp3")
            val output = tempDirectory / "result.mp4"
            Mp4Converter(dispatcher).convert(
                input = input,
                clip = Clip(1.seconds, 3.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions,
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            assertThat(output.fileSize()).isBetween(25_000, 50_000)
        }

        @Test
        fun `Converting an MP3 audio file with no album art to MP4 should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test-no-cover.mp3")
            val output = tempDirectory / "result.mp4"
            Mp4Converter(dispatcher).convert(
                input = input,
                clip = Clip(1.seconds, 3.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions,
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            assertThat(output.fileSize()).isBetween(25_000, 50_000)
        }

        @Test
        fun `Converting to MP3 with no album art should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test-no-video.ts")
            val output = tempDirectory / "result.mp3"
            Mp3Converter(dispatcher).convert(
                input = input,
                clip = Clip(6.seconds, 10.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions,
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            val albumArt = output
                .toFile()
                .runCatching(AudioFileIO::read)
                .getOrNull()
                ?.tag
                ?.firstArtwork
                ?.binaryData
            assertThat(albumArt).isEqualTo(Mp3Converter.defaultAlbumArtPath?.readBytes())
            assertThat(output.fileSize()).isBetween(50_000, 70_000)
        }

        @Test
        fun `Converting to MP3 with an album art should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test-no-video.ts")
            val image = getResourceAsPath("test.png")
            val output = tempDirectory / "result.mp3"
            Mp3Converter(dispatcher).convert(
                input = input,
                clip = Clip(6.seconds, 10.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions.copy(path = image),
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            val albumArt = output
                .toFile()
                .runCatching(AudioFileIO::read)
                .getOrNull()
                ?.tag
                ?.firstArtwork
                ?.binaryData
            assertThat(albumArt).isEqualTo(image.readBytes())
            assertThat(output.fileSize()).isGreaterThan(50_000)
        }

        @Test
        fun `Converting to RAW should succeed`() = runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test-no-video.ts")
            val output = tempDirectory / "result.ts"
            RawConverter(dispatcher).convert(
                input = input,
                clip = Clip(6.seconds, 8.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions,
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = {}
            )
            assertThat(output.fileSize()).isBetween(20_000, 30_000)
        }
    }

    @Test
    fun `When conversion is not successful, convert function should throw exception`() =
        runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = URI.create("file:/non-existent-file.ts").toURL()
            val output = createTempDirectory() / "result.mp4"
            assertThrows<Exception> {
                Mp4Converter(dispatcher).convert(
                    input = input,
                    clip = Clip(Duration.ZERO, 14.seconds),
                    intro = defaultIntroOptions,
                    cover = defaultCoverOptions,
                    quality = Quality.MEDIUM,
                    fixInterlaced = false,
                    output = output,
                    listener = {}
                )
            }
        }

    @Test
    fun `Converting a local file to MP4 should succeed`() = runTest(timeout = testTimeout) {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val input = getResourceAsURL("test.ts")
        val output = tempDirectory / "result.mp4"
        Mp4Converter(dispatcher).convert(
            input = input,
            clip = Clip(6.seconds, 8.seconds),
            intro = defaultIntroOptions,
            cover = defaultCoverOptions,
            quality = Quality.MEDIUM,
            fixInterlaced = false,
            output = output,
            listener = {}
        )
        assertThat(output.fileSize()).isGreaterThan(100_000)
    }

    @Test
    fun `When converting, the progress listener should be called`() = runTest(timeout = testTimeout) {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val input = getResourceAsURL("test.ts")
        val output = tempDirectory / "result.mp4"
        val listener = spyk<ProgressListener>()
        Mp4Converter(dispatcher).convert(
            input = input,
            clip = Clip(6.seconds, 8.seconds),
            intro = defaultIntroOptions,
            cover = defaultCoverOptions,
            quality = Quality.MEDIUM,
            fixInterlaced = false,
            output = output,
            listener = listener
        )
        verifyOrder {
            listener.onProgressUpdate(range(0f, 0.99f))
            listener.onProgressUpdate(1f)
        }
    }

    /**
     * Could also have used FFprobe to inspect the result (for example, its duration).
     * See https://superuser.com/a/945604
     */
    @Test
    fun `Converter should be cancelable while in the process of converting`() = runTest(timeout = testTimeout) {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val input = getResourceAsURL("test.ts")
        val output = createTempDirectory() / "result.mp4"
        var hasProgressed = false
        val job1 = launch(Dispatchers.Default) {
            Mp4Converter(dispatcher).convert(
                input = input,
                clip = Clip(Duration.ZERO, 14.seconds),
                intro = defaultIntroOptions,
                cover = defaultCoverOptions,
                quality = Quality.MEDIUM,
                fixInterlaced = false,
                output = output,
                listener = { hasProgressed = true }
            )
        }
        val job2 = launch {
            while (!hasProgressed) delay(50.milliseconds)
            job1.cancel()
        }
        job1.join()
        job2.join()
        assertThat(output.fileSize()).isLessThan(500_000)
    }

    /**
     * The cancellation of coroutines in Kotlin is by cooperation, meaning,
     * the coroutine should be cooperative for proper cancellation to work.
     * See https://kotlinlang.org/docs/cancellation-and-timeouts.html#cancellation-is-cooperative
     *
     * In the current implementation, the [Converter.convert] function eventually
     * throws [CancellationException] if canceled, no matter what,
     * but if we do not make the process cancellable either with calling [yield] periodically or
     * checking for [CoroutineContext.isActive] periodically or, for example,
     * calling [cancellable] on the internal [Flow] if there is any,
     * the exception is thrown only after the conversion is complete.
     */
    @Test
    fun `When conversion is canceled (in other words, the coroutine executing the convert function is canceled), convert function should throw an exception of type CancellationException (and it should throw it immediately)`() =
        runTest(timeout = testTimeout) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val input = getResourceAsURL("test.ts")
            val output = createTempDirectory() / "result.mp4"
            var exception: Throwable? = null
            var progress = 0f
            val job = launch(Dispatchers.Default) {
                Mp4Converter(dispatcher)
                    .runCatching {
                        convert(
                            input = input,
                            clip = Clip(Duration.ZERO, 14.seconds),
                            intro = defaultIntroOptions,
                            cover = defaultCoverOptions,
                            quality = Quality.MEDIUM,
                            fixInterlaced = false,
                            output = output,
                            listener = { progress = it }
                        )
                    }
                    .onFailure { exception = it }
            }
            launch(Dispatchers.Default) {
                while (progress < 0.01f) delay(50.milliseconds)
                job.cancel()
            }.join()
            while (exception == null) {
                delay(50.milliseconds)
            }
            assertThat(progress).isLessThan(0.3f)
            assertThat(exception).isInstanceOf(CancellationException::class.java)
        }
}
