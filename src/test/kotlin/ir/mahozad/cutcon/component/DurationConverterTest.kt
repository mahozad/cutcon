package ir.mahozad.cutcon.component

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DurationConverterTest {
    @ParameterizedTest
    @MethodSource("generateDurationsAndExpectedResults")
    fun `Formatting various durations should produce proper result`(
        argument: Triple<Duration, Int, String>
    ) {
        val (duration, desiredParts, expectedResult) = argument
        val converter = DefaultDurationConverter
        val result = converter.format(duration, numberOfParts = desiredParts)
        assertThat(result).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("generateDurationStringsAndExpectedResults")
    fun `Parsing various duration strings should produce proper result`(
        argument: Pair<String, Duration?>
    ) {
        val (durationString, expectedResult) = argument
        val converter = DefaultDurationConverter
        val result = converter.parse(durationString)
        assertThat(result).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun generateDurationsAndExpectedResults() = listOf(
            Triple(Duration.ZERO, 1, "00"),
            Triple(700.milliseconds, 1, "00"),
            Triple(1.seconds, 1, "01"),
            Triple(59.seconds, 1, "59"),
            Triple(60.seconds, 1, "60"),
            Triple(61.seconds, 1, "61"),
            Triple(69.seconds, 1, "69"),
            Triple(70.seconds, 1, "70"),
            Triple(119.seconds, 1, "119"),
            Triple(120.seconds, 1, "120"),
            Triple(121.seconds, 1, "121"),
            Triple(59.minutes + 59.seconds, 1, "3599"),
            Triple(60.minutes, 2, "60:00"),
            Triple(60.minutes + 1.seconds, 1, "3601"),
            Triple(70.minutes, 2, "70:00"),
            Triple(119.minutes + 59.seconds, 1, "7199"),
            Triple(120.minutes, 2, "120:00"),
            Triple(121.minutes + 59.seconds, 1, "7319"),
            //////////
            Triple(Duration.ZERO, 2, "00:00"),
            Triple(700.milliseconds, 2, "00:00"),
            Triple(1.seconds, 2, "00:01"),
            Triple(59.seconds, 2, "00:59"),
            Triple(60.seconds, 2, "01:00"),
            Triple(61.seconds, 2, "01:01"),
            Triple(69.seconds, 2, "01:09"),
            Triple(70.seconds, 2, "01:10"),
            Triple(119.seconds, 2, "01:59"),
            Triple(120.seconds, 2, "02:00"),
            Triple(121.seconds, 2, "02:01"),
            Triple(59.minutes + 59.seconds, 2, "59:59"),
            Triple(60.minutes, 2, "60:00"),
            Triple(60.minutes + 1.seconds, 2, "60:01"),
            Triple(70.minutes, 2, "70:00"),
            Triple(119.minutes + 59.seconds, 2, "119:59"),
            Triple(120.minutes, 2, "120:00"),
            Triple(121.minutes + 59.seconds, 2, "121:59"),
            //////////
            Triple(Duration.ZERO, 3, "00:00:00"),
            Triple(700.milliseconds, 3, "00:00:00"),
            Triple(1.seconds, 3, "00:00:01"),
            Triple(59.seconds, 3, "00:00:59"),
            Triple(60.seconds, 3, "00:01:00"),
            Triple(61.seconds, 3, "00:01:01"),
            Triple(69.seconds, 3, "00:01:09"),
            Triple(70.seconds, 3, "00:01:10"),
            Triple(119.seconds, 3, "00:01:59"),
            Triple(120.seconds, 3, "00:02:00"),
            Triple(121.seconds, 3, "00:02:01"),
            Triple(59.minutes + 59.seconds, 3, "00:59:59"),
            Triple(60.minutes, 3, "01:00:00"),
            Triple(60.minutes + 1.seconds, 3, "01:00:01"),
            Triple(70.minutes, 3, "01:10:00"),
            Triple(119.minutes + 59.seconds, 3, "01:59:59"),
            Triple(120.minutes, 3, "02:00:00"),
            Triple(121.minutes + 59.seconds, 3, "02:01:59"),
            //////////
            Triple(-(59.seconds), 1, "-59"),
            Triple(-(12.minutes + 59.seconds), 2, "-12:59"),
            Triple(-(7.hours + 12.minutes + 59.seconds), 3, "-07:12:59"),
            ////////// Very big durations
            Triple(100.hours + 12.minutes + 59.seconds, 1, "--"),
            Triple(100.hours + 12.minutes + 59.seconds, 2, "--:--"),
            Triple(100.hours + 12.minutes + 59.seconds, 3, "--:--:--"),
            Triple((-100).hours + (-12).minutes + (-59).seconds, 3, "--:--:--"),
            ////////// Very small durations
            Triple(9.milliseconds, 1, "00"),
            Triple((-9).milliseconds, 1, "00"),
            Triple(9.milliseconds, 2, "00:00"),
            Triple((-9).milliseconds, 2, "00:00"),
            Triple(9.milliseconds, 3, "00:00:00"),
            Triple((-9).milliseconds, 3, "00:00:00")
        )

        @JvmStatic
        fun generateDurationStringsAndExpectedResults() = listOf(
            "00:00:00" to Duration.ZERO,
            "00:00:01" to 1.seconds,
            "00:00:59" to 59.seconds,
            "00:01:00" to 60.seconds,
            "00:01:01" to 61.seconds,
            "00:00:61" to 61.seconds,
            "00:00:1543" to 1543.seconds,
            "00:78:89" to 78.minutes + 89.seconds,
            "00:01:09" to 69.seconds,
            "00:01:10" to 70.seconds,
            "00:01:59" to 119.seconds,
            "00:02:00" to 120.seconds,
            "00:02:01" to 121.seconds,
            "00:59:59" to 59.minutes + 59.seconds,
            "01:00:00" to 60.minutes,
            "01:00:01" to 60.minutes + 1.seconds,
            "01:10:00" to 70.minutes,
            "01:59:59" to 119.minutes + 59.seconds,
            "02:00:00" to 120.minutes,
            "02:01:59" to 121.minutes + 59.seconds,
            "12:34" to 12.minutes + 34.seconds,
            "00:12:34" to 12.minutes + 34.seconds,
            "0:12:34" to 12.minutes + 34.seconds,
            "0:00:34" to 34.seconds,
            "00:0:34" to 34.seconds,
            "00:00:34" to 34.seconds,
            "04:00:4" to 4.hours + 4.seconds,
            "01:3:4" to 1.hours + 3.minutes + 4.seconds,
            "۰۰:۰۰:۰۰" to Duration.ZERO,
            "۰0:5۹:۵9" to 59.minutes + 59.seconds,
            ":00:34" to null,
            ":01:34" to null,
            ":1:34" to null,
            "" to null,
            " " to null,
            "  :  :  " to null,
            "..:..:.." to null,
            "::::::::" to null,
            ":" to null,
            "::" to null,
            ":::" to null,
            "a" to null,
            "ab:cd:ef" to null,
            "12:a3:45" to null,
            "12::3:45" to null,
            "12::03:45" to null,
            "12:.03:45" to null,
            "12:0.3:45" to null,
            "12:1.3:45" to null,
            "12.34.56" to null,
            "12@34a56" to null
        )
    }
}
