package ir.mahozad.cutcon.viewmodel

import io.mockk.coEvery
import io.mockk.spyk
import ir.mahozad.cutcon.component.UrlMaker
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.converter.Converter
import ir.mahozad.cutcon.converter.ConverterFactory
import ir.mahozad.cutcon.converter.FFmpegOption
import ir.mahozad.cutcon.model.CoverOptions
import ir.mahozad.cutcon.model.Format
import ir.mahozad.cutcon.model.IntroOptions
import ir.mahozad.cutcon.model.Quality
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.name

@OptIn(ExperimentalCoroutinesApi::class)
@TestInstance(Lifecycle.PER_CLASS)
abstract class SaveFileTest {
    @Test
    fun `saveFile should initially be null`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.saveFile.first()
        assertThat(result).isEqualTo(null)
    }

    /**
     * Otherwise, when trying to create a new clip, it will overwrite the previous one because the save file is still the same.
     */
    @Test
    fun `When the conversion process is finished, saveFile should reset to null so the user will be forced to select a save file again`() =
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
            coEvery { converter.convert(any(), any(), any(), any(), any(), any(), any(), any()) } answers {
                println("I'm a fake converter and I'm pretending to convert...")
            }
            val converterFactory = ConverterFactory { converter }
            val urlMaker = UrlMaker { URI("file://localhost").toURL() }
            val saveFile = Path("xyz") / "1.mp4"
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                urlMaker = urlMaker,
                converterFactory = converterFactory
            ).apply {
                setSaveFile(saveFile)
            }
            val results = mutableListOf<Path?>()
            backgroundScope.launch(dispatcher) { viewModel.saveFile.toList(results) }
            viewModel.startOperation()
            assertThat(results).containsExactly(saveFile, null)
        }

    @ParameterizedTest
    @MethodSource("generateFileNamesAndExpectedResults")
    fun `Changing save file should reflect in saveFile flow`(
        argument: Pair<String, String>
    ) = runTest {
        val (userFileNameInput, expectedResultFileName) = argument
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            setFormat(Format.MP4)
        }
        val saveFile = Path("xyz") / userFileNameInput
        val results = mutableListOf<Path?>()
        backgroundScope.launch(dispatcher) { viewModel.saveFile.toList(results) }
        viewModel.setSaveFile(saveFile)
        assertThat(results.map { it?.name }).containsExactly(null, expectedResultFileName)
    }

    private fun generateFileNamesAndExpectedResults() = listOf(
        "1" to "1.mp4",
        "mp4" to "mp4.mp4",
        ".mp4" to ".mp4",
        "1.mp4" to "1.mp4",
        "1.MP4" to "1.mp4",
        "1.jpg" to "1.jpg.mp4",
        "1.abc.mp4" to "1.abc.mp4",
        "1.mp4.abc" to "1.mp4.abc.mp4"
    )

    @Test
    fun `When save file is specified, changing the format should update the save file extension`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val saveFile = Path("xyz") / "1.mp4"
        val viewModel = constructMainViewModel(dispatcher).apply {
            setFormat(Format.MP4)
            setSaveFile(saveFile)
        }
        val results = mutableListOf<Path?>()
        backgroundScope.launch(dispatcher) { viewModel.saveFile.toList(results) }
        viewModel.setFormat(Format.MP3)
        assertThat(results).containsExactly(Path("xyz") / "1.mp4", Path("xyz") / "1.mp3")
    }

    @Test
    fun `When save file is specified and the format is raw, changing the local source file should update the save file extension`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setFormat(Format.RAW)
                setSourceToLocal(Path("a/b/c/d.mp4"))
                setSaveFile(Path("xyz") / "1.mp4")
            }
            val results = mutableListOf<Path?>()
            backgroundScope.launch(dispatcher) { viewModel.saveFile.toList(results) }
            viewModel.setSourceToLocal(Path("a/b/c/d.qwe"))
            assertThat(results).containsExactly(Path("xyz") / "1.mp4", Path("xyz") / "1.qwe")
        }

    @Test
    fun `When save file is not specified, changing the format should not update the save file`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            setFormat(Format.MP4)
        }
        val results = mutableListOf<Path?>()
        backgroundScope.launch(dispatcher) { viewModel.saveFile.toList(results) }
        viewModel.setFormat(Format.MP3)
        assertThat(results).containsExactly(null)
    }
}
