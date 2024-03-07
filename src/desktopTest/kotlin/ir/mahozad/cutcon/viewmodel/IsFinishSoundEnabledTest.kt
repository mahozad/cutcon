package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultIsFinishSoundEnabled
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
abstract class IsFinishSoundEnabledTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    @Test
    fun `For first time app launch, the isFinishSoundEnabled should be default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.isFinishSoundEnabled.first()
        assertThat(result).isEqualTo(defaultIsFinishSoundEnabled)
    }

    @Test
    fun `When settings has isFinishSoundEnabled, the isFinishSoundEnabled should be initialized to it`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        settings.put(PreferenceKeys.PREF_FINISH_SOUND, false.toString())
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isFinishSoundEnabled.toList(results) }
        assertThat(results).containsExactly(false)
    }

    @Test
    fun `After changing isFinishSoundEnabled, the isFinishSoundEnabled should be updated`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isFinishSoundEnabled.toList(results) }
        viewModel.setIsFinishSoundEnabled(false)
        assertThat(results).containsExactly(defaultIsFinishSoundEnabled, false)
    }

    @Test
    fun `After changing isFinishSoundEnabled, the isFinishSoundEnabled should be persisted`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        viewModel.setIsFinishSoundEnabled(false)
        val isFinishSoundEnabled = settings[PreferenceKeys.PREF_FINISH_SOUND, null]?.let(String::toBoolean)
        assertThat(isFinishSoundEnabled).isEqualTo(false)
    }
}
