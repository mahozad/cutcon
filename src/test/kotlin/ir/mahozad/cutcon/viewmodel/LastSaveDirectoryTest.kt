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
abstract class LastSaveDirectoryTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    @Test
    fun `For first time app launch, lastSaveDirectory should be null`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.lastSaveDirectory.first()
        assertThat(result).isEqualTo(null)
    }

    @Test
    fun `When settings has last save directory but the directory does not exist on filesystem, lastSaveDirectory should be initialized to null`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val directory = Path("a/bc/d")
            settings.put(PreferenceKeys.PREF_LAST_SAVE_DIRECTORY, directory.absolutePathString())
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                settings = settings
            )
            val results = mutableListOf<Path?>()
            backgroundScope.launch(dispatcher) { viewModel.lastSaveDirectory.toList(results) }
            assertThat(results).containsExactly(null)
        }

    @Test
    fun `When settings has last save directory and the directory exists on filesystem, lastSaveDirectory should be initialized to it`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val directory = createTempDirectory()
            settings.put(PreferenceKeys.PREF_LAST_SAVE_DIRECTORY, directory.absolutePathString())
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                settings = settings
            )
            val results = mutableListOf<Path?>()
            backgroundScope.launch(dispatcher) { viewModel.lastSaveDirectory.toList(results) }
            assertThat(results).containsExactly(directory.absolute())
        }

    @Test
    fun `After setting a save file, lastSaveDirectory should be updated to its directory`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Path?>()
        backgroundScope.launch(dispatcher) { viewModel.lastSaveDirectory.toList(results) }
        viewModel.setSaveFile(Path("a/bc/d/1.mp4"))
        assertThat(results).containsExactly(null, Path("a/bc/d"))
    }

    @Test
    fun `After setting new save file, new lastSaveDirectory should be persisted in settings`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val saveFile = Path("a/bc/d/1.mp4")
        viewModel.setSaveFile(saveFile)
        val directory = settings[PreferenceKeys.PREF_LAST_SAVE_DIRECTORY, null]?.let(::Path)
        assertThat(directory).isEqualTo(saveFile.parent.absolute())
    }
}
