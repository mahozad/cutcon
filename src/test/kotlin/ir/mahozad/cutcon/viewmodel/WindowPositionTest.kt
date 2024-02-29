package ir.mahozad.cutcon.viewmodel

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import ir.mahozad.cutcon.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.awt.GraphicsEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
abstract class WindowPositionTest {
    @Test
    fun `Window position should initially be center`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
        val viewModel = constructMainViewModel(dispatcher)
        val position = viewModel.windowPosition.first()
        assertThat(position).isEqualTo(
            WindowPosition(
                x = ((screenSize.width - WINDOW_WIDTH_WITH_PANEL) / 2).dp,
                y = ((screenSize.height - WINDOW_HEIGHT_REGULAR) / 2).dp
            )
        )
    }

    @Test
    fun `When hiding side panel, window position should stay the same`() = runTest {
        val screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<WindowPosition>()
        backgroundScope.launch(dispatcher) { viewModel.windowPosition.toList(results) }
        viewModel.toggleSidePanel()
        assertThat(results).containsExactly(
            WindowPosition(
                x = ((screenSize.width - WINDOW_WIDTH_WITH_PANEL) / 2).dp,
                y = ((screenSize.height - WINDOW_HEIGHT_REGULAR) / 2).dp
            )
        )
    }

    @Test
    fun `After switching to mini player, window position should update to bottom end`() = runTest {
        val screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<WindowPosition>()
        backgroundScope.launch(dispatcher) { viewModel.windowPosition.toList(results) }
        viewModel.toggleMiniScreen()
        // This is set automatically in real app
        viewModel.onWindowPositionChanged(
            WindowPosition(
                x = (screenSize.width - WINDOW_WIDTH_MINI).dp,
                y = (screenSize.height - WINDOW_HEIGHT_MINI).dp
            )
        )
        assertThat(results).containsExactly(
            WindowPosition(
                x = ((screenSize.width - WINDOW_WIDTH_WITH_PANEL) / 2).dp,
                y = ((screenSize.height - WINDOW_HEIGHT_REGULAR) / 2).dp
            ),
            WindowPosition(
                x = (screenSize.width - WINDOW_WIDTH_MINI).dp,
                y = (screenSize.height - WINDOW_HEIGHT_MINI).dp
            )
        )
    }

    @Test
    fun `After restoring from mini player and app side panel was shown before entering mini, window position should update to center`() =
        runTest {
            val screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                toggleMiniScreen()
                // This is set automatically in real app
                onWindowPositionChanged(
                    WindowPosition(
                        x = (screenSize.width - WINDOW_WIDTH_MINI).dp,
                        y = (screenSize.height - WINDOW_HEIGHT_MINI).dp
                    )
                )
            }
            val results = mutableListOf<WindowPosition>()
            backgroundScope.launch(dispatcher) { viewModel.windowPosition.toList(results) }
            viewModel.toggleMiniScreen()
            assertThat(results).containsExactly(
                WindowPosition(
                    x = (screenSize.width - WINDOW_WIDTH_MINI).dp,
                    y = (screenSize.height - WINDOW_HEIGHT_MINI).dp
                ),
                WindowPosition(
                    x = ((screenSize.width - WINDOW_WIDTH_WITH_PANEL) / 2).dp,
                    y = ((screenSize.height - WINDOW_HEIGHT_REGULAR) / 2).dp
                )
            )
        }

    @Test
    fun `After restoring from mini player and app side panel was hidden before entering mni, window position should update to center`() =
        runTest {
            val screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                toggleSidePanel()
                toggleMiniScreen()
                // This is set automatically in real app
                onWindowPositionChanged(
                    WindowPosition(
                        x = (screenSize.width - WINDOW_WIDTH_MINI).dp,
                        y = (screenSize.height - WINDOW_HEIGHT_MINI).dp
                    )
                )
            }
            val results = mutableListOf<WindowPosition>()
            backgroundScope.launch(dispatcher) { viewModel.windowPosition.toList(results) }
            viewModel.toggleMiniScreen()
            assertThat(results).containsExactly(
                WindowPosition(
                    x = (screenSize.width - WINDOW_WIDTH_MINI).dp,
                    y = (screenSize.height - WINDOW_HEIGHT_MINI).dp
                ),
                WindowPosition(
                    x = ((screenSize.width - WINDOW_WIDTH_NO_PANEL) / 2).dp,
                    y = ((screenSize.height - WINDOW_HEIGHT_REGULAR) / 2).dp
                )
            )
        }

    @Test
    fun `After dragging window and then hiding side panel, window position should stay where it is`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            onWindowPositionChanged(WindowPosition(x = (100).dp, y = (222).dp))
            toggleSidePanel()
        }
        val results = mutableListOf<WindowPosition>()
        backgroundScope.launch(dispatcher) { viewModel.windowPosition.toList(results) }
        assertThat(results).containsExactly(WindowPosition(x = (100).dp, y = (222).dp))
    }

    @Test
    fun `After dragging window and then hiding side panel and then showing side panel, window position should stay where it is`() =
        runTest {
            val screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher)
            val results = mutableListOf<WindowPosition>()
            backgroundScope.launch(dispatcher) { viewModel.windowPosition.toList(results) }
            viewModel.onWindowPositionChanged(WindowPosition(x = (100).dp, y = (222).dp))
            viewModel.toggleSidePanel()
            viewModel.toggleSidePanel()
            assertThat(results).containsExactly(
                WindowPosition(
                    x = ((screenSize.width - WINDOW_WIDTH_WITH_PANEL) / 2).dp,
                    y = ((screenSize.height - WINDOW_HEIGHT_REGULAR) / 2).dp
                ),
                WindowPosition(x = (100).dp, y = (222).dp)
            )
        }
}
