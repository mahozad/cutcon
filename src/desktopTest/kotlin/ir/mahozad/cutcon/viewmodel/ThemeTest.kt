package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultTheme
import ir.mahozad.cutcon.model.PreferenceKeys
import ir.mahozad.cutcon.model.Theme
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
abstract class ThemeTest {

    private val settings = Preferences
        .userRoot()
        .node("/${BuildConfig.APP_NAME}/test")!!

    @BeforeEach
    fun setUp() {
        settings.clear()
    }

    @Test
    fun `For first time app launch, the theme should be default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.theme.first()
        assertThat(result).isEqualTo(defaultTheme)
    }

    @Test
    fun `When settings has theme, the theme should be initialized to it`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        settings.put(PreferenceKeys.PREF_THEME, Theme.DARK.name)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        val results = mutableListOf<Theme>()
        backgroundScope.launch(dispatcher) { viewModel.theme.toList(results) }
        assertThat(results).containsExactly(Theme.DARK)
    }

    @Test
    fun `After changing theme, the theme should be updated`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Theme>()
        backgroundScope.launch(dispatcher) { viewModel.theme.toList(results) }
        viewModel.setTheme(Theme.DARK)
        assertThat(results).containsExactly(defaultTheme, Theme.DARK)
    }

    @Test
    fun `After changing theme, the theme should be persisted`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(
            dispatcher = dispatcher,
            settings = settings
        )
        viewModel.setTheme(Theme.DARK)
        val theme = settings[PreferenceKeys.PREF_THEME, null]?.let(Theme::valueOf)
        assertThat(theme).isEqualTo(Theme.DARK)
    }
}
