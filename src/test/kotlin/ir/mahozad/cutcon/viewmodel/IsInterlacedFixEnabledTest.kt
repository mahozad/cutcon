package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultIsInterlacedFixEnabled
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
abstract class IsInterlacedFixEnabledTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    @Test
    fun `For first time app launch, the isInterlacedFixEnabled should be default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.isInterlacedFixEnabled.first()
        assertThat(result).isEqualTo(defaultIsInterlacedFixEnabled)
    }

    @Test
    fun `When settings has isInterlacedFixEnabled, the isInterlacedFixEnabled should be initialized to it`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        settings.put(PreferenceKeys.PREF_INTERLACED_FIX, false.toString())
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isInterlacedFixEnabled.toList(results) }
        assertThat(results).containsExactly(false)
    }

    @Test
    fun `After changing isInterlacedFixEnabled, the isInterlacedFixEnabled should be updated`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isInterlacedFixEnabled.toList(results) }
        viewModel.setIsInterlacedFixEnabled(false)
        assertThat(results).containsExactly(defaultIsInterlacedFixEnabled, false)
    }

    @Test
    fun `After changing isInterlacedFixEnabled, the isInterlacedFixEnabled should be persisted`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        viewModel.setIsInterlacedFixEnabled(false)
        val isInterlacedFixEnabled = settings[PreferenceKeys.PREF_INTERLACED_FIX, null]?.let(String::toBoolean)
        assertThat(isInterlacedFixEnabled).isEqualTo(false)
    }
}
