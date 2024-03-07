package ir.mahozad.cutcon.component

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.assertj.core.data.Offset.offset
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class SystemDateTimeTest {
    @Test
    fun `Getting nowTime should return current time`() {
        val result = SystemDateTime.nowTime()
        assertThat(result).isCloseTo(LocalTime.now(), within(3, ChronoUnit.SECONDS))
    }

    @Test
    fun `Getting nowDate should return current date`() {
        val result = SystemDateTime.nowDate()
        assertThat(result).isEqualTo(LocalDate.now())
    }

    @Test
    fun `Getting nowMillis should return current milliseconds`() {
        val result = SystemDateTime.nowMillis()
        assertThat(result).isCloseTo(System.currentTimeMillis(), offset(500))
    }
}
