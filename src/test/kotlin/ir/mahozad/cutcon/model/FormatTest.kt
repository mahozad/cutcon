package ir.mahozad.cutcon.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class FormatTest {

    @Nested
    inner class LocalSourceTest {
        @Test
        fun `When source is a local file and getting MP4 format name, it should return the proper result`() {
            val source = Source.Local(Path("a/b.xYz"))
            val result = Format.MP4.actualName(source)
            assertThat(result).isEqualTo("MP4")
        }

        @Test
        fun `When source is a local file and getting MP3 format name, it should return the proper result`() {
            val source = Source.Local(Path("a/b.xYz"))
            val result = Format.MP3.actualName(source)
            assertThat(result).isEqualTo("MP3")
        }

        @Test
        fun `When source is a local file and getting RAW format name, it should return the local file extension name`() {
            val source = Source.Local(Path("a/b.xYz"))
            val result = Format.RAW.actualName(source)
            assertThat(result).isEqualTo("XYZ")
        }

        @Test
        fun `When source is a local file and getting MP4 extension, it should return the proper result`() {
            val source = Source.Local(Path("a/b.xYz"))
            val result = Format.MP4.extension(source)
            assertThat(result).isEqualTo("mp4")
        }

        @Test
        fun `When source is a local file and getting MP3 extension, it should return the proper result`() {
            val source = Source.Local(Path("a/b.xYz"))
            val result = Format.MP3.extension(source)
            assertThat(result).isEqualTo("mp3")
        }

        @Test
        fun `When source is a local file and getting RAW extension, it should return the local file extension`() {
            val source = Source.Local(Path("a/b.xYz"))
            val result = Format.RAW.extension(source)
            assertThat(result).isEqualTo("xyz")
        }
    }
}
