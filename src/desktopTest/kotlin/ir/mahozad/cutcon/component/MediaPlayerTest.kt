package ir.mahozad.cutcon.component

import androidx.compose.ui.graphics.ImageBitmap
import ir.mahozad.cutcon.getResourceAsPath
import ir.mahozad.cutcon.model.Source
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class MediaPlayerTest {

    @Test
    fun `Initially, the video should be null`() = runTest {
        val player = DefaultMediaPlayer()
        val result = player.video.first()
        assertThat(result).isNull()
    }

    @Disabled("Because when running all tests, they do not run completely")
    @Test
    fun `When playing a new media, the video should update`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val player = DefaultMediaPlayer()
        val source = getResourceAsPath("test.ts")
            .let(Source::Local)
            .let(DefaultUrlMaker::makeUrl)
        val results = mutableListOf<ImageBitmap?>()
        val job = backgroundScope.launch(dispatcher) {
            player.video.take(10).toList(results)
        }
        player.play(source)
        job.join()
        assertThat(results.first()).isNull()
        assertThat(results[1]).isNotNull()
        assertThat(results.last()).isNotNull()
    }
}
