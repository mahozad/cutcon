package ir.mahozad.cutcon.component

import com.github.mfathi91.time.PersianDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.div

class SaveFileNameGeneratorTest {

    // See the README
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    lateinit var tempDirectory: Path

    @Nested
    inner class GregorianCalendarTest {
        @Test
        fun `When directory does not contain a file with name, default name should be the provided date and time numbered 1`() {
            val result = DefaultSaveFileNameGenerator.generate(
                Path("DOES_NOT_MATTER"),
                LocalDate.of(2020, 6, 17),
                LocalTime.of(21, 17, 3)
            )
            assertThat(result).isEqualTo("2020-06-17_21-00_1")
        }

        @Test
        fun `When directory contains a file with same name, default name should be provided date and time numbered 2`() {
            (tempDirectory / "2020-06-17_21-00_1.any").createFile()
            val result = DefaultSaveFileNameGenerator.generate(
                tempDirectory,
                LocalDate.of(2020, 6, 17),
                LocalTime.of(21, 17, 3)
            )
            assertThat(result).isEqualTo("2020-06-17_21-00_2")
        }

        @Test
        fun `When directory contains two numbered files with same name, default name should be provided date and time numbered 3`() {
            (tempDirectory / "2020-06-17_21-00_1.any").createFile()
            (tempDirectory / "2020-06-17_21-00_2.any").createFile()
            val result = DefaultSaveFileNameGenerator.generate(
                tempDirectory,
                LocalDate.of(2020, 6, 17),
                LocalTime.of(21, 17, 3)
            )
            assertThat(result).isEqualTo("2020-06-17_21-00_3")
        }
    }

    @Nested
    inner class SolarHijriCalendarTest {
        @Test
        fun `When directory does not contain a file with name, default name should be the provided date and time numbered 1`() {
            val result = DefaultSaveFileNameGenerator.generate(
                Path("DOES_NOT_MATTER"),
                PersianDate.of(1399, 3, 28),
                LocalTime.of(21, 17, 3)
            )
            assertThat(result).isEqualTo("1399-03-28_21-00_1")
        }

        @Test
        fun `When directory contains a file with same name, default name should be provided date and time numbered 2`() {
            (tempDirectory / "1399-03-28_21-00_1.any").createFile()
            val result = DefaultSaveFileNameGenerator.generate(
                tempDirectory,
                PersianDate.of(1399, 3, 28),
                LocalTime.of(21, 17, 3)
            )
            assertThat(result).isEqualTo("1399-03-28_21-00_2")
        }

        @Test
        fun `When directory contains two numbered files with same name, default name should be provided date and time numbered 3`() {
            (tempDirectory / "1399-03-28_21-00_1.any").createFile()
            (tempDirectory / "1399-03-28_21-00_2.any").createFile()
            val result = DefaultSaveFileNameGenerator.generate(
                tempDirectory,
                PersianDate.of(1399, 3, 28),
                LocalTime.of(21, 17, 3)
            )
            assertThat(result).isEqualTo("1399-03-28_21-00_3")
        }
    }
}
