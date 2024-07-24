package ir.mahozad.cutcon.component

import ir.mahozad.cutcon.model.Source
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

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

        @Disabled("FIXME")
        @Test
        fun `When the file name contains hash sign, should produce proper url (should not treat hash as fragment identifier)`() {
            val source = Source.Local(Path("a/a-file-name containing #hash.mp4"))
            val url = DefaultUrlMaker.makeUrl(source)
            val expected = source.path.name
            assertThat(url.path.substringAfterLast('/')).isEqualTo(expected)
        }
    }
}
