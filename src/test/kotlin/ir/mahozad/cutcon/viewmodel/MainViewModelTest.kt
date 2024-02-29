package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.text.TextRange
import io.mockk.*
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.component.DefaultUrlMaker
import ir.mahozad.cutcon.component.MediaPlayerTest
import ir.mahozad.cutcon.component.SystemDateTime
import ir.mahozad.cutcon.component.UrlMaker
import ir.mahozad.cutcon.converter.Converter
import ir.mahozad.cutcon.converter.DefaultConverterFactory
import ir.mahozad.cutcon.converter.FFmpegOption
import ir.mahozad.cutcon.converter.ProgressListener
import ir.mahozad.cutcon.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.net.URL
import java.nio.file.Path
import java.time.LocalTime
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.fileSize
import kotlin.io.path.notExists
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    // See the README
    @Nested inner class ThemeTests : ThemeTest()
    @Nested inner class IntroTests : IntroTest()
    @Nested inner class CoverTests : CoverTest()
    @Nested inner class StatusTests : StatusTest()
    @Nested inner class SourceTests : SourceTest()
    @Nested inner class FormatTests : FormatTest()
    @Nested inner class QualityTests : QualityTest()
    @Nested inner class AppExitTests : AppExitTest()
    @Nested inner class SaveFilTests : SaveFileTest()
    @Nested inner class ClipLoopTests : ClipLoopTest()
    @Nested inner class KeyEventTests : KeyEventTest()
    @Nested inner class LanguageTests : LanguageTest()
    @Nested inner class CalendarTests : CalendarTest()
    @Nested inner class ClipInfoTests : ClipInfoTest()
    @Nested inner class MediaInfoTests : MediaInfoTest()
    @Nested inner class MediaPlayerTests : MediaPlayerTest()
    @Nested inner class AspectRatioTests : AspectRatioTest()
    @Nested inner class IsMiniScreenTests : IsMiniScreenTest()
    @Nested inner class IsFullscreenTests : IsFullscreenTest()
    @Nested inner class IsAlwaysOnTopTests : IsAlwaysOnTopTest()
    @Nested inner class WindowPositionTests : WindowPositionTest()
    @Nested inner class LastOpenDirectoryTests : LastOpenDirectoryTest()
    @Nested inner class LastSaveDirectoryTests : LastSaveDirectoryTest()
    @Nested inner class IsFinishSoundEnabledTests : IsFinishSoundEnabledTest()
    @Nested inner class IsInterlacedFixEnabledTests : IsInterlacedFixEnabledTest()
    @Nested inner class IsQualityInputApplicableTests : IsQualityInputApplicableTest()
    @Nested inner class IsScreenshotInputEnabledTests : IsScreenshotInputEnabledTest()
    @Nested inner class IsScreenshotSoundEnabledTests : IsScreenshotSoundEnabledTest()
    @Nested inner class IsChangelogDialogDisplayedTests : IsChangelogDialogDisplayedTest()

    @Test
    fun `When the current time minutes and seconds is zero, live seek fraction should be zero, not infinity`() =
        runTest {
            mockkObject(SystemDateTime)
            every { SystemDateTime.nowTime() } returns LocalTime.of(10, 0, 0)
            assertThat(liveSeekFraction).isEqualTo(0f)
            unmockkAll()
        }

    @Test
    fun `Converting a TV clip to MP3 format should succeed`(
        @TempDir(cleanup = CleanupMode.ON_SUCCESS) tempOutputDirectory: Path
    ) = runTest(timeout = testTimeout) {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val cover = getResourceAsPath("test.png")
        val urlMaker = UrlMaker { getResourceAsURL("test.ts") }
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            urlMaker = urlMaker,
            mediaPlayer = FakeMediaPlayer(100.seconds),
            converterFactory = DefaultConverterFactory(dispatcher)
        ).apply {
            startMediaProgressListener()
            setFormat(Format.MP3)
            setSaveFile(tempOutputDirectory / "1.mp3")
            onClipStartSecondChanged("0", TextRange.Zero)
            onClipEndSecondChanged("1", TextRange.Zero)
            setCoverFile(cover)
        }
        viewModel.startProcess()
        assertThat((tempOutputDirectory / "1.mp3").fileSize()).isGreaterThan(15_000)
    }

    @Disabled(
        """
            Fails because we detect files with .ts extension to have video/* mime type.
            More generally, currently converting any video container format that contains only audio fails.
            See the main README file for the related FIXME.
        """
    )
    @Test
    fun `Converting an audio file with TS extension to MP4 format should succeed`(
        @TempDir(cleanup = CleanupMode.ON_SUCCESS) tempOutputDirectory: Path
    ) = runTest(timeout = testTimeout) {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            urlMaker = DefaultUrlMaker,
            mediaPlayer = FakeMediaPlayer(100.seconds),
            converterFactory = DefaultConverterFactory(dispatcher)
        ).apply {
            startMediaProgressListener()
            setSourceToLocal(getResourceAsPath("test-no-video.ts"))
            setFormat(Format.MP4)
            setSaveFile(tempOutputDirectory / "1.mp4")
            onClipStartSecondChanged("0", TextRange.Zero)
            onClipEndSecondChanged("1", TextRange.Zero)
        }
        viewModel.startProcess()
        assertThat((tempOutputDirectory / "1.mp4").fileSize()).isGreaterThan(10_000)
    }

    @Test
    fun `After cancelling the conversion process, converter should be canceled and state be reset`() =
        runTest(timeout = testTimeout) {
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
                }
            }
            val urlMaker = UrlMaker { URL("file://") }
            val saveFileValues = mutableListOf<Path?>()
            val statusValues = mutableListOf<Status>()
            val saveFile = Path("xyz") / "1.mp4"
            val sourcePath = getResourceAsPath("test.ts")
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                mediaPlayer = FakeMediaPlayer(100.seconds),
                converterFactory = { converter }
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("8", TextRange.Zero)
                setSaveFile(saveFile)
                setSourceToLocal(sourcePath)
            }
            backgroundScope.launch(dispatcher) { viewModel.saveFile.toList(saveFileValues) }
            backgroundScope.launch(dispatcher) { viewModel.status.toList(statusValues) }

            viewModel.startProcess()
            delay(50.milliseconds)
            viewModel.cancelProcess()

            assertThat(statusValues).contains(
                Status.Ready,
                Status.InProgress(source = Source.Local(sourcePath), progress = 0f)
            )
            assertThat(saveFileValues).containsExactly(saveFile, null)
            assertThat(saveFile).satisfiesAnyOf(
                { assertThat(it.notExists()).isTrue() },
                { assertThat(it.fileSize()).isLessThan(1_000_000) }
            )
        }

    @Test
    fun `If the source is changed and then clip creation is started, progress source should be the new source`() =
        runTest(timeout = testTimeout) {
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
                var progress = 0f
                println("I'm a fake converter and I'm running an infinite loop  pretending to convert...")
                while (isActive) {
                    delay(15.milliseconds)
                    (args[7] as ProgressListener).onProgressUpdate(progress)
                    println("Fake conversion emitted progress $progress")
                    progress += 0.01f
                }
            }
            val urlMaker = UrlMaker { URL("file://") }
            val statusValues = mutableListOf<Status>()
            val saveFile = Path("xyz") / "1.mp4"
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                mediaPlayer = FakeMediaPlayer(100.seconds),
                converterFactory = { converter }
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("8", TextRange.Zero)
                setSaveFile(saveFile)
            }
            backgroundScope.launch(dispatcher) { viewModel.status.toList(statusValues) }

            viewModel.setSourceToLocal(Path("x/y/z.mp4"))
            viewModel.startProcess()
            delay(50.milliseconds)
            viewModel.setSourceToLocal(Path("abc.mp4"))
            delay(50.milliseconds)
            viewModel.cancelProcess()

            assertThat(
                statusValues
                    .filterIsInstance<Status.InProgress>()
                    .map(Status.InProgress::source)
                    .toSet()
            ).containsExactly(
                Source.Local(Path("x/y/z.mp4"))
            )
        }

    @Test
    fun `If the source is changed while the clip is being created, progress source should be the original one from which the clip is being created`() =
        runTest(timeout = testTimeout) {
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
                var progress = 0f
                println("I'm a fake converter and I'm running an infinite loop  pretending to convert...")
                while (isActive) {
                    delay(15.milliseconds)
                    (args[7] as ProgressListener).onProgressUpdate(progress)
                    println("Fake conversion emitted progress $progress")
                    progress += 0.01f
                }
            }
            val urlMaker = UrlMaker { URL("file://") }
            val statusValues = mutableListOf<Status>()
            val saveFile = Path("xyz") / "1.mp4"
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                mediaPlayer = FakeMediaPlayer(100.seconds),
                converterFactory = { converter }
            ).apply {
                startMediaProgressListener()
                onClipEndSecondChanged("8", TextRange.Zero)
                setSaveFile(saveFile)
            }
            backgroundScope.launch(dispatcher) { viewModel.status.toList(statusValues) }

            viewModel.startProcess()
            delay(50.milliseconds)
            viewModel.setSourceToLocal(Path("abc.mp4"))
            delay(50.milliseconds)
            viewModel.cancelProcess()

            assertThat(
                statusValues
                    .filterIsInstance<Status.InProgress>()
                    .map(Status.InProgress::source)
                    .toSet()
            ).containsExactly(
                defaultSource
            )
        }
}
