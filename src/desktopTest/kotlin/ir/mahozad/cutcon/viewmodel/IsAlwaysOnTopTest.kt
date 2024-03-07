package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultIsAlwaysOnTop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class IsAlwaysOnTopTest {
    @Test
    fun `IsAlwaysOnTop should initially be the default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.isAlwaysOnTop.first()
        assertThat(result).isEqualTo(defaultIsAlwaysOnTop)
    }

    @Test
    fun `When setting isAlwaysOnTop to new value, it should update to that`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isAlwaysOnTop.toList(results) }
        viewModel.toggleIsAlwaysOnTop()
        assertThat(results).containsExactly(defaultIsAlwaysOnTop, !defaultIsAlwaysOnTop)
    }
}
