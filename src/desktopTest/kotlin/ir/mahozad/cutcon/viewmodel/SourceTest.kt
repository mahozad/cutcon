package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultSource
import ir.mahozad.cutcon.model.Source
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@OptIn(ExperimentalCoroutinesApi::class)
abstract class SourceTest {
    @Test
    fun `Initially, the source should be default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.source.first()
        assertThat(result).isEqualTo(defaultSource)
    }

    @Test
    fun `When source is changed, it should update to the new one`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val results = mutableListOf<Source>()
        backgroundScope.launch(dispatcher) { viewModel.source.toList(results) }
        viewModel.setSourceToLocal(Path("a/b/c.mp4"))
        assertThat(results).containsExactly(defaultSource, Source.Local(Path("a/b/c.mp4")))
    }
}
