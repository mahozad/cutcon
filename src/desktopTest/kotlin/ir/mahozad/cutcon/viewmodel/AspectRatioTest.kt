package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultAspectRatio
import ir.mahozad.cutcon.getResourceAsPath
import ir.mahozad.cutcon.model.AspectRatio
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
import java.util.prefs.Preferences

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AspectRatioTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    @Test
    fun `For first time app launch, the aspect ratio override should be default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.aspectRatio.first()
        assertThat(result).isEqualTo(defaultAspectRatio)
    }

    @Test
    fun `When settings has aspect ratio override, the aspect ratio override should be initialized to it`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        settings.put(PreferenceKeys.PREF_ASPECT_RATIO, AspectRatio.SOURCE.name)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val results = mutableListOf<AspectRatio>()
        backgroundScope.launch(dispatcher) { viewModel.aspectRatio.toList(results) }
        assertThat(results).containsExactly(AspectRatio.SOURCE)
    }

    @Test
    fun `After changing aspect ratio override, the aspect ratio override should be updated`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            setAspectRatio(AspectRatio.W16H9)
        }
        val results = mutableListOf<AspectRatio>()
        backgroundScope.launch(dispatcher) { viewModel.aspectRatio.toList(results) }
        viewModel.setAspectRatio(AspectRatio.SOURCE)
        assertThat(results).containsExactly(AspectRatio.W16H9, AspectRatio.SOURCE)
    }

    @Test
    fun `After changing aspect ratio override, the aspect ratio override should be persisted`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        ).apply {
            setAspectRatio(AspectRatio.W16H9)
        }
        viewModel.setAspectRatio(AspectRatio.SOURCE)
        val result = settings[PreferenceKeys.PREF_ASPECT_RATIO, null]?.let(AspectRatio::valueOf)
        assertThat(result).isEqualTo(AspectRatio.SOURCE)
    }

    @Test
    fun `When aspect ratio is not source and media is set to non-video, the aspect ratio override should be updated to source`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setAspectRatio(AspectRatio.W16H9)
            }
            val results = mutableListOf<AspectRatio>()
            backgroundScope.launch(dispatcher) { viewModel.aspectRatio.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test.png"))
            assertThat(results).containsExactly(AspectRatio.W16H9, AspectRatio.SOURCE)
        }

    @Test
    fun `When the aspect ratio is not source and the media is set to non-video and then to video, the aspect ratio override should be updated to the last aspect ratio override`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setAspectRatio(AspectRatio.W16H9)
            }
            val results = mutableListOf<AspectRatio>()
            backgroundScope.launch(dispatcher) { viewModel.aspectRatio.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test.png"))
            viewModel.setSourceToLocal(getResourceAsPath("test.ts"))
            assertThat(results).containsExactly(AspectRatio.W16H9, AspectRatio.SOURCE, AspectRatio.W16H9)
        }

    @Test
    fun `When the aspect ratio is source and the media is set to non-video and then to video, the aspect ratio override should be updated to the last aspect ratio override`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setAspectRatio(AspectRatio.SOURCE)
            }
            val results = mutableListOf<AspectRatio>()
            backgroundScope.launch(dispatcher) { viewModel.aspectRatio.toList(results) }
            viewModel.setSourceToLocal(getResourceAsPath("test.png"))
            viewModel.setSourceToLocal(getResourceAsPath("test.ts"))
            assertThat(results).containsExactly(AspectRatio.SOURCE)
        }
}
