package ir.mahozad.cutcon.component

import ir.mahozad.cutcon.model.Source
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

class UrlMakerTest {

    @Nested
    inner class LocalSourceTest {
        @Test
        fun `Make URL for relative path containing only file name`() {
            val source = Source.Local(Path("1.mp4"))
            val url = DefaultUrlMaker.makeUrl(source)
            val expected = URL("file://localhost/${source.path.absolutePathString()}")
            assertThat(url).isEqualTo(expected)
        }

        @Test
        fun `Make URL for relative path containing directory`() {
            val source = Source.Local(Path("a/1.mp4"))
            val url = DefaultUrlMaker.makeUrl(source)
            val expected = URL("file://localhost/${source.path.absolutePathString()}")
            assertThat(url).isEqualTo(expected)
        }
    }
}
