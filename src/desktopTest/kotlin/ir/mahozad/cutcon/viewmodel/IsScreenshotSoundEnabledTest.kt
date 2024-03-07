package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultIsScreenshotSoundEnabled
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
abstract class IsScreenshotSoundEnabledTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    @Test
    fun `For first time app launch, the isScreenshotSoundEnabled should be default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.isScreenshotSoundEnabled.first()
        assertThat(result).isEqualTo(defaultIsScreenshotSoundEnabled)
    }

    @Test
    fun `When settings has isScreenshotSoundEnabled, the isScreenshotSoundEnabled should be initialized to it`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            settings.put(PreferenceKeys.PREF_SCREENSHOT_SOUND, false.toString())
            val viewModel = constructMainViewModel(
                dispatcher = dispatcher,
                settings = settings
            )
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isScreenshotSoundEnabled.toList(results) }
            assertThat(results).containsExactly(false)
        }

    @Test
    fun `After changing isScreenshotSoundEnabled, the isScreenshotSoundEnabled should be updated`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isScreenshotSoundEnabled.toList(results) }
        viewModel.setIsScreenshotSoundEnabled(false)
        assertThat(results).containsExactly(defaultIsScreenshotSoundEnabled, false)
    }

    @Test
    fun `After changing isScreenshotSoundEnabled, it should be persisted`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        viewModel.setIsScreenshotSoundEnabled(false)
        val isScreenshotSoundEnabled = settings[PreferenceKeys.PREF_SCREENSHOT_SOUND, null]?.let(String::toBoolean)
        assertThat(isScreenshotSoundEnabled).isEqualTo(false)
    }
}
