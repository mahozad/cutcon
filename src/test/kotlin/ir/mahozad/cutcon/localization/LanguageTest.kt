package ir.mahozad.cutcon.localization

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@Nested
class LocalizeDigitsTest {
    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class FaLocaleTest {
        @ParameterizedTest
        @MethodSource("generateStringsAndExpectedResults")
        fun `Result should be as expected`(
            argument: Pair<String, String>
        ) {
            val (string, expectedResult) = argument
            val result = LanguageFa.localizeDigits(string)
            assertThat(result).isEqualTo(expectedResult)
        }

        private fun generateStringsAndExpectedResults() = listOf(
            "a.b.c" to "a.b.c",
            "a.1.2" to "a.۱.۲",
            "3.۲.c" to "۳.۲.c",
            "3.۲.c ک ?" to "۳.۲.c ک ?",
            "۱.۲.۳" to "۱.۲.۳"
        )
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class EnLocaleTest {
        @ParameterizedTest
        @MethodSource("generateStringsAndExpectedResults")
        fun `Result should be as expected`(
            argument: Pair<String, String>
        ) {
            val (string, expectedResult) = argument
            val result = LanguageEn.localizeDigits(string)
            assertThat(result).isEqualTo(expectedResult)
        }

        private fun generateStringsAndExpectedResults() = listOf(
            "a.b.c" to "a.b.c",
            "a.۱.۲" to "a.1.2",
            "۳.2.c" to "3.2.c",
            "۳.2.c ک ?" to "3.2.c ک ?",
            "1.2.3" to "1.2.3"
        )
    }
}
