package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultFormat
import ir.mahozad.cutcon.model.Format
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class FormatTest {
    @Test
    fun `Format should initially be the default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.format.first()
        assertThat(result).isEqualTo(defaultFormat)
    }

    @Test
    fun `When setting format to new value, it should update to that`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            setFormat(Format.RAW)
        }
        val results = mutableListOf<Format>()
        backgroundScope.launch(dispatcher) { viewModel.format.toList(results) }
        viewModel.setFormat(Format.MP3)
        assertThat(results).containsExactly(Format.RAW, Format.MP3)
    }
}
