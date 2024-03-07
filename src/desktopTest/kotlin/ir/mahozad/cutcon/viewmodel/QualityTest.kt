package ir.mahozad.cutcon.viewmodel

import ir.mahozad.cutcon.constructMainViewModel
import ir.mahozad.cutcon.defaultQuality
import ir.mahozad.cutcon.model.Quality
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class QualityTest {
    @Test
    fun `Quality should initially be the default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher)
        val result = viewModel.quality.first()
        assertThat(result).isEqualTo(defaultQuality)
    }

    @Test
    fun `When setting quality to new value, it should update to that`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val viewModel = constructMainViewModel(dispatcher).apply {
            setQuality(Quality.HIGH.value.toFloat())
        }
        val results = mutableListOf<Quality>()
        backgroundScope.launch(dispatcher) { viewModel.quality.toList(results) }
        viewModel.setQuality(Quality.LOWEST.value.toFloat())
        assertThat(results).containsExactly(Quality.HIGH, Quality.LOWEST)
    }
}
