package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.constructMainViewModel
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
abstract class IsQualityInputApplicableTest {

    @Test
    fun `Initially, the isQualityInputApplicable should be true`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.isQualityInputApplicable.first()
        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `When format is not RAW and setting format to RAW, isQualityInputApplicable should update to false`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val viewModel = constructMainViewModel(dispatcher).apply {
                setFormat(Format.MP4)
            }
            val results = mutableListOf<Boolean>()
            backgroundScope.launch(dispatcher) { viewModel.isQualityInputApplicable.toList(results) }
            viewModel.setFormat(Format.RAW)
            assertThat(results).containsExactly(true, false)
        }

    @Test
    fun `When format is RAW and setting format to not RAW, isQualityInputApplicable should update to true`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            setFormat(Format.RAW)
        }
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(dispatcher) { viewModel.isQualityInputApplicable.toList(results) }
        viewModel.setFormat(Format.MP3)
        assertThat(results).containsExactly(false, true)
    }
}
