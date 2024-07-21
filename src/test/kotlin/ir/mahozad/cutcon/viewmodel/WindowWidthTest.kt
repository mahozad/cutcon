package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.MainViewModel
import ir.mahozad.cutcon.WINDOW_WIDTH_MINI
import ir.mahozad.cutcon.WINDOW_WIDTH_NO_PANEL
import ir.mahozad.cutcon.WINDOW_WIDTH_WITH_PANEL
import ir.mahozad.cutcon.constructMainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class WindowWidthTest {
    @Test
    fun `Window width should initially be WINDOW_WIDTH_WITH_PANEL and animated`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val (width, isAnimated) = viewModel.windowWidth.first()
        assertThat(width).isEqualTo(WINDOW_WIDTH_WITH_PANEL)
        assertThat(isAnimated).isEqualTo(true)
    }

    @Test
    fun `When hiding side panel, window width should update to proper value`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<MainViewModel.WindowWidth>()
        backgroundScope.launch(dispatcher) { viewModel.windowWidth.toList(results) }
        viewModel.toggleSidePanel()
        assertThat(results).containsExactly(
            MainViewModel.WindowWidth(WINDOW_WIDTH_WITH_PANEL, true),
            MainViewModel.WindowWidth(WINDOW_WIDTH_NO_PANEL, true)
        )
    }

    @Test
    fun `When showing side panel, window width should update to proper value`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<MainViewModel.WindowWidth>()
        viewModel.toggleSidePanel()
        backgroundScope.launch(dispatcher) { viewModel.windowWidth.toList(results) }
        viewModel.toggleSidePanel()
        assertThat(results).containsExactly(
            MainViewModel.WindowWidth(WINDOW_WIDTH_NO_PANEL, true),
            MainViewModel.WindowWidth(WINDOW_WIDTH_WITH_PANEL, true)
        )
    }

    @Test
    fun `When going from regular screen to mini screen, window width should update to proper value`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<MainViewModel.WindowWidth>()
        backgroundScope.launch(dispatcher) { viewModel.windowWidth.toList(results) }
        viewModel.toggleMiniScreen()
        assertThat(results).containsExactly(
            MainViewModel.WindowWidth(WINDOW_WIDTH_WITH_PANEL, true),
            MainViewModel.WindowWidth(WINDOW_WIDTH_MINI, false)
        )
    }

    @Test
    fun `When going from mini screen to regular screen, window width should update to proper value`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<MainViewModel.WindowWidth>()
        viewModel.toggleMiniScreen()
        backgroundScope.launch(dispatcher) { viewModel.windowWidth.toList(results) }
        viewModel.toggleMiniScreen()
        assertThat(results).containsExactly(
            MainViewModel.WindowWidth(WINDOW_WIDTH_MINI, false),
            MainViewModel.WindowWidth(WINDOW_WIDTH_WITH_PANEL, false)
        )
    }
}
