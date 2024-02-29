package ir.mahozad.cutcon.viewmodel

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
abstract class IsMiniScreenTest {
    @Test
    fun `IsMiniScreen should initially be false`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.isMiniScreen.first()
        assertThat(result).isFalse()
    }

    @Test
    fun `When toggling mini screen, isMini should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isMiniScreen.toList(results) }
        viewModel.toggleMiniScreen()
        assertThat(results.last()).isTrue()
    }

    @Test
    fun `When entering mini screen, side panel should get hidden`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
        viewModel.toggleMiniScreen()
        assertThat(results.last()).isFalse()
    }

    @Test
    fun `When side panel is off and then entering mini screen and then restoring to regular screen, side panel should be off`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                toggleSidePanel()
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
            viewModel.toggleMiniScreen()
            viewModel.toggleMiniScreen()
            assertThat(results).containsExactly(false)
        }

    /**
     * This happens, for example, when using the keyboard shortcut.
     */
    @Test
    fun `When mini screen is on, toggling the side panel should have no effect`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            toggleMiniScreen()
        }
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
        viewModel.toggleSidePanel()
        assertThat(results).containsExactly(false)
    }

    @Test
    fun `When side panel is on and then entering mini screen and then restoring to regular screen, side panel should be on`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher)
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isSidePanelDisplayed.toList(results) }
            viewModel.toggleMiniScreen()
            viewModel.toggleMiniScreen()
            assertThat(results).containsExactly(true, false, true)
        }
}
