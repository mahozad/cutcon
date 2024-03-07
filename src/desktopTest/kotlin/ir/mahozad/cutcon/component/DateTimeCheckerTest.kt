package ir.mahozad.cutcon.component

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ir.mahozad.cutcon.defaultDateTimeCheckingPeriod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class DateTimeCheckerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initialize() {
            mockkObject(SystemDateTime)
        }

        @JvmStatic
        @AfterAll
        fun terminate() {
            unmockkAll()
        }
    }

    @Test
    fun `DateTimeChecker should emit new time value if day of month changes`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val dateTimeChecker = DefaultDateTimeChecker(dispatcher)
        every { SystemDateTime.nowDate() } returnsMany listOf(
            LocalDate.of(1725, 6, 7),
            LocalDate.of(1725, 6, 8),
        )
        every { SystemDateTime.nowTime() } returns LocalTime.of(3, 4, 5, 6)
        val results = mutableListOf<Pair<LocalDate, LocalTime>>()
        backgroundScope.launch(dispatcher) { dateTimeChecker.dateTimeFlow().toList(results) }
        delay(defaultDateTimeCheckingPeriod + 50.milliseconds)
        assertThat(results).containsExactly(
            LocalDate.of(1725, 6, 7) to LocalTime.of(3, 4),
            LocalDate.of(1725, 6, 8) to LocalTime.of(3, 4)
        )
    }

    @Test
    fun `DateTimeChecker should emit new time value if month of year changes`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val dateTimeChecker = DefaultDateTimeChecker(dispatcher)
        every { SystemDateTime.nowDate() } returnsMany listOf(
            LocalDate.of(1725, 6, 7),
            LocalDate.of(1725, 8, 7),
        )
        every { SystemDateTime.nowTime() } returns LocalTime.of(3, 4, 5, 6)
        val results = mutableListOf<Pair<LocalDate, LocalTime>>()
        backgroundScope.launch(dispatcher) { dateTimeChecker.dateTimeFlow().toList(results) }
        delay(defaultDateTimeCheckingPeriod + 50.milliseconds)
        assertThat(results).containsExactly(
            LocalDate.of(1725, 6, 7) to LocalTime.of(3, 4),
            LocalDate.of(1725, 8, 7) to LocalTime.of(3, 4)
        )
    }

    @Test
    fun `DateTimeChecker should emit new time value if year changes`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val dateTimeChecker = DefaultDateTimeChecker(dispatcher)
        every { SystemDateTime.nowDate() } returnsMany listOf(
            LocalDate.of(1725, 6, 7),
            LocalDate.of(1728, 6, 7),
        )
        every { SystemDateTime.nowTime() } returns LocalTime.of(3, 4, 5, 6)
        val results = mutableListOf<Pair<LocalDate, LocalTime>>()
        backgroundScope.launch(dispatcher) { dateTimeChecker.dateTimeFlow().toList(results) }
        delay(defaultDateTimeCheckingPeriod + 50.milliseconds)
        assertThat(results).containsExactly(
            LocalDate.of(1725, 6, 7) to LocalTime.of(3, 4),
            LocalDate.of(1728, 6, 7) to LocalTime.of(3, 4)
        )
    }

    @Test
    fun `DateTimeChecker should not emit new time value if nanosecond changes`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val dateTimeChecker = DefaultDateTimeChecker(dispatcher)
        every { SystemDateTime.nowDate() } returns LocalDate.of(1725, 6, 7)
        every { SystemDateTime.nowTime() } returnsMany listOf(
            LocalTime.of(3, 4, 5, 6),
            LocalTime.of(3, 4, 5, 7)
        )
        val results = mutableListOf<Pair<LocalDate, LocalTime>>()
        backgroundScope.launch(dispatcher) { dateTimeChecker.dateTimeFlow().toList(results) }
        delay(defaultDateTimeCheckingPeriod + 50.milliseconds)
        assertThat(results).containsExactly(
            LocalDate.of(1725, 6, 7) to LocalTime.of(3, 4)
        )
    }

    @Test
    fun `DateTimeChecker should not emit new time value if second changes`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val dateTimeChecker = DefaultDateTimeChecker(dispatcher)
        every { SystemDateTime.nowDate() } returns LocalDate.of(1725, 6, 7)
        every { SystemDateTime.nowTime() } returnsMany listOf(
            LocalTime.of(3, 4, 5, 0),
            LocalTime.of(3, 4, 6, 0)
        )
        val results = mutableListOf<Pair<LocalDate, LocalTime>>()
        backgroundScope.launch(dispatcher) { dateTimeChecker.dateTimeFlow().toList(results) }
        delay(defaultDateTimeCheckingPeriod + 50.milliseconds)
        assertThat(results).containsExactly(
            LocalDate.of(1725, 6, 7) to LocalTime.of(3, 4)
        )
    }

    @Test
    fun `DateTimeChecker should emit new time value if minute changes`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val dateTimeChecker = DefaultDateTimeChecker(dispatcher)
        every { SystemDateTime.nowDate() } returns LocalDate.of(1725, 6, 7)
        every { SystemDateTime.nowTime() } returnsMany listOf(
            LocalTime.of(3, 4, 5, 6),
            LocalTime.of(3, 5, 5, 6)
        )
        val results = mutableListOf<Pair<LocalDate, LocalTime>>()
        backgroundScope.launch(dispatcher) { dateTimeChecker.dateTimeFlow().toList(results) }
        delay(defaultDateTimeCheckingPeriod + 50.milliseconds)
        assertThat(results).containsExactly(
            LocalDate.of(1725, 6, 7) to LocalTime.of(3, 4),
            LocalDate.of(1725, 6, 7) to LocalTime.of(3, 5)
        )
    }

    @Test
    fun `DateTimeChecker should emit new time value if hour changes`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val dateTimeChecker = DefaultDateTimeChecker(dispatcher)
        every { SystemDateTime.nowDate() } returns LocalDate.of(1725, 6, 7)
        every { SystemDateTime.nowTime() } returnsMany listOf(
            LocalTime.of(3, 5, 6, 7),
            LocalTime.of(4, 5, 6, 7)
        )
        val results = mutableListOf<Pair<LocalDate, LocalTime>>()
        backgroundScope.launch(dispatcher) { dateTimeChecker.dateTimeFlow().toList(results) }
        delay(defaultDateTimeCheckingPeriod + 50.milliseconds)
        assertThat(results).containsExactly(
            LocalDate.of(1725, 6, 7) to LocalTime.of(3, 5),
            LocalDate.of(1725, 6, 7) to LocalTime.of(4, 5)
        )
    }

    @Test
    fun `DateTimeChecker should emit new time value if hour and minute change`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val dateTimeChecker = DefaultDateTimeChecker(dispatcher)
        every { SystemDateTime.nowDate() } returns LocalDate.of(1725, 6, 7)
        every { SystemDateTime.nowTime() } returnsMany listOf(
            LocalTime.of(3, 5, 6, 7),
            LocalTime.of(4, 6, 6, 7)
        )
        val results = mutableListOf<Pair<LocalDate, LocalTime>>()
        backgroundScope.launch(dispatcher) { dateTimeChecker.dateTimeFlow().toList(results) }
        delay(defaultDateTimeCheckingPeriod + 50.milliseconds)
        assertThat(results).containsExactly(
            LocalDate.of(1725, 6, 7) to LocalTime.of(3, 5),
            LocalDate.of(1725, 6, 7) to LocalTime.of(4, 6)
        )
    }

    @Test
    fun `DateTimeChecker should not emit same value as the last one`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val dateTimeChecker = DefaultDateTimeChecker(dispatcher)
        every { SystemDateTime.nowDate() } returns LocalDate.of(1725, 6, 7)
        every { SystemDateTime.nowTime() } returns LocalTime.of(2, 3, 4)
        val results = mutableListOf<Pair<LocalDate, LocalTime>>()
        backgroundScope.launch(dispatcher) { dateTimeChecker.dateTimeFlow().toList(results) }
        delay(defaultDateTimeCheckingPeriod * 3 + 50.milliseconds)
        assertThat(results).containsExactly(
            LocalDate.of(1725, 6, 7) to LocalTime.of(2, 3)
        )
    }
}
