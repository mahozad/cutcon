package ir.mahozad.cutcon.component

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

interface DurationConverter {
    fun format(duration: Duration, numberOfParts: Int): String
    fun parse(string: String): Duration?
}

object DefaultDurationConverter : DurationConverter {
    override fun format(duration: Duration, numberOfParts: Int): String {
        val durationValue = duration.absoluteValue
        if (durationValue >= 100.hours) {
            return "--:".repeat(numberOfParts).dropLast(1)
        } else if (durationValue < 1.seconds) {
            return "00:".repeat(numberOfParts).dropLast(1)
        }
        val sign = if (duration.isNegative()) "-" else ""
        val hours = durationValue.inWholeHours.format(false)
        val minutes = durationValue.inWholeMinutes.format(numberOfParts > 2)
        val seconds = durationValue.inWholeSeconds.format(numberOfParts > 1)
        return when (numberOfParts) {
            3 -> "$sign$hours:$minutes:$seconds"
            2 -> "$sign$minutes:$seconds"
            else -> "$sign$seconds"
        }
    }

    private fun Long.format(shouldCap: Boolean) =
        (if (shouldCap) (this % 60) else this)
            .toString().padStart(2, '0')

    /**
     * See https://stackoverflow.com/q/54970799
     */
    override fun parse(string: String): Duration? {
        val (s, m, h) = string
            .split(":")
            .runCatching { map(String::toInt) }
            .getOrNull()
            ?.reversed()
            ?.plus(0) /* Ensures contains hour */
            ?: return null
        return h.hours + m.minutes + s.seconds
    }
}
