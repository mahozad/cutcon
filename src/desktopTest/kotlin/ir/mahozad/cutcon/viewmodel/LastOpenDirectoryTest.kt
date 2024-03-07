package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.model.PreferenceKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.prefs.Preferences
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory

@OptIn(ExperimentalCoroutinesApi::class)
abstract class LastOpenDirectoryTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    @Test
    fun `For first time app launch, lastOpenDirectory should be null`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.lastOpenDirectory.first()
        assertThat(result).isEqualTo(null)
    }

    @Test
    fun `When settings has last open directory but the directory does not exist on filesystem, lastOpenDirectory should be initialized to null`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val directory = Path("a/bc/d")
            settings.put(PreferenceKeys.PREF_LAST_OPEN_DIRECTORY, directory.absolutePathString())
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                settings = settings
            )
            val results = mutableListOf<Path?>()
            backgroundScope.launch(dispatcher) { viewModel.lastOpenDirectory.toList(results) }
            assertThat(results).containsExactly(null)
        }

    @Test
    fun `When settings has last open directory and the directory exists on filesystem, lastOpenDirectory should be initialized to it`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val directory = createTempDirectory()
            settings.put(PreferenceKeys.PREF_LAST_OPEN_DIRECTORY, directory.absolutePathString())
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                settings = settings
            )
            val results = mutableListOf<Path?>()
            backgroundScope.launch(dispatcher) { viewModel.lastOpenDirectory.toList(results) }
            assertThat(results).containsExactly(directory.absolute())
        }

    @Test
    fun `After setting local source to a new file, lastOpenDirectory should be updated to its directory`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Path?>()
        backgroundScope.launch(dispatcher) { viewModel.lastOpenDirectory.toList(results) }
        viewModel.setSourceToLocal(Path("a/bc/d/1.mp4"))
        assertThat(results).containsExactly(null, Path("a/bc/d"))
    }

    @Test
    fun `After setting intro to a new file, lastOpenDirectory should be updated to its directory`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Path?>()
        backgroundScope.launch(dispatcher) { viewModel.lastOpenDirectory.toList(results) }
        viewModel.setIntroFile(Path("a/bc/d/1.png"))
        assertThat(results).containsExactly(null, Path("a/bc/d"))
    }

    @Test
    fun `After setting cover to a new file, lastOpenDirectory should be updated to its directory`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Path?>()
        backgroundScope.launch(dispatcher) { viewModel.lastOpenDirectory.toList(results) }
        viewModel.setCoverFile(Path("a/bc/d/2.jpg"))
        assertThat(results).containsExactly(null, Path("a/bc/d"))
    }

    @Test
    fun `After setting new local source file, new lastOpenDirectory should be persisted in settings`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val file = Path("w/xy/z/1.mp4")
        viewModel.setSourceToLocal(file)
        val directory = settings[PreferenceKeys.PREF_LAST_OPEN_DIRECTORY, null]?.let(::Path)
        assertThat(directory).isEqualTo(file.parent.absolute())
    }

    @Test
    fun `After setting new intro file, new lastOpenDirectory should be persisted in settings`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val file = Path("w/xy/z/1.png")
        viewModel.setIntroFile(file)
        val directory = settings[PreferenceKeys.PREF_LAST_OPEN_DIRECTORY, null]?.let(::Path)
        assertThat(directory).isEqualTo(file.parent.absolute())
    }

    @Test
    fun `After setting new cover file, new lastOpenDirectory should be persisted in settings`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val file = Path("w/xy/z/2.jpg")
        viewModel.setCoverFile(file)
        val directory = settings[PreferenceKeys.PREF_LAST_OPEN_DIRECTORY, null]?.let(::Path)
        assertThat(directory).isEqualTo(file.parent.absolute())
    }

    @Test
    fun `After setting intro to null, lastOpenDirectory should not change`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val file = Path("w/xy/z/1.png")
        val viewModel = constructMainViewModel(dispatcher).apply {
            setIntroFile(file)
        }
        val results = mutableListOf<Path?>()
        backgroundScope.launch(dispatcher) { viewModel.lastOpenDirectory.toList(results) }
        viewModel.setIntroFile(null)
        assertThat(results).containsExactly(Path("w/xy/z"))
    }

    @Test
    fun `After setting cover to null, lastOpenDirectory should not change`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val file = Path("w/xy/z/2.jpg")
        val viewModel = constructMainViewModel(dispatcher).apply {
            setIntroFile(file)
        }
        val results = mutableListOf<Path?>()
        backgroundScope.launch(dispatcher) { viewModel.lastOpenDirectory.toList(results) }
        viewModel.setCoverFile(null)
        assertThat(results).containsExactly(Path("w/xy/z"))
    }
}
