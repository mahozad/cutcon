package ir.mahozad.cutcon.localization

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class MessagesTest {
    @ParameterizedTest
    @MethodSource("generateDurationsAndExpectedResultsFa")
    fun `Generating total time string for various durations should produce proper result (Fa)`(
        argument: Pair<Duration, String>
    ) {
        val (duration, expectedResult) = argument
        val result = MessagesFa.totalClipCreationTime(duration)
        assertThat(result).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("generateDurationsAndExpectedResultsEn")
    fun `Generating total time string for various durations should produce proper result (En)`(
        argument: Pair<Duration, String>
    ) {
        val (duration, expectedResult) = argument
        val result = MessagesEn.totalClipCreationTime(duration)
        assertThat(result).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun generateDurationsAndExpectedResultsFa() = listOf(
            Duration.ZERO to "کمتر از یک ثانیه طول کشید",
            1.seconds to "۱ ثانیه طول کشید",
            2.seconds + 700.milliseconds to "۲ ثانیه طول کشید",
            59.seconds to "۵۹ ثانیه طول کشید",
            59.seconds + 700.milliseconds to "۵۹ ثانیه طول کشید",
            1.minutes to "۱ دقیقه طول کشید",
            1.minutes + 300.milliseconds to "۱ دقیقه طول کشید",
            1.minutes + 700.milliseconds to "۱ دقیقه طول کشید",
            1.minutes + 1.seconds + 700.milliseconds to "۱ دقیقه و ۱ ثانیه طول کشید",
            59.minutes + 57.seconds + 700.milliseconds to "۵۹ دقیقه و ۵۷ ثانیه طول کشید",
            1.hours to "۱ ساعت طول کشید",
            1.hours + 700.milliseconds to "۱ ساعت طول کشید",
            1.hours + 1.seconds + 700.milliseconds to "۱ ساعت طول کشید",
            1.hours + 1.minutes + 1.seconds + 700.milliseconds to "۱ ساعت و ۱ دقیقه طول کشید",
            2.hours + 3.minutes + 1.seconds + 700.milliseconds to "۲ ساعت و ۳ دقیقه طول کشید",
            1.days + 3.minutes + 1.seconds + 700.milliseconds to "۲۴ ساعت و ۳ دقیقه طول کشید",
            2.days + 3.hours + 1.minutes + 1.seconds + 700.milliseconds to "۵۱ ساعت و ۱ دقیقه طول کشید"
        )

        @JvmStatic
        fun generateDurationsAndExpectedResultsEn() = listOf(
            Duration.ZERO to "Took less than one second",
            1.seconds to "Took 1 second",
            2.seconds + 700.milliseconds to "Took 2 seconds",
            59.seconds to "Took 59 seconds",
            59.seconds + 700.milliseconds to "Took 59 seconds",
            1.minutes to "Took 1 minute",
            1.minutes + 300.milliseconds to "Took 1 minute",
            1.minutes + 700.milliseconds to "Took 1 minute",
            1.minutes + 1.seconds + 700.milliseconds to "Took 1 minute 1 second",
            59.minutes + 57.seconds + 700.milliseconds to "Took 59 minutes 57 seconds",
            1.hours to "Took 1 hour",
            1.hours + 700.milliseconds to "Took 1 hour",
            1.hours + 1.seconds + 700.milliseconds to "Took 1 hour",
            1.hours + 1.minutes + 1.seconds + 700.milliseconds to "Took 1 hour 1 minute",
            2.hours + 3.minutes + 1.seconds + 700.milliseconds to "Took 2 hours 3 minutes",
            1.days + 3.minutes + 1.seconds + 700.milliseconds to "Took 24 hours 3 minutes",
            2.days + 3.hours + 1.minutes + 1.seconds + 700.milliseconds to "Took 51 hours 1 minute"
        )
    }
}
