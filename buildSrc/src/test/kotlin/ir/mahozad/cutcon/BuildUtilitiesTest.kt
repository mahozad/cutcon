package ir.mahozad.cutcon

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class BuildUtilitiesTest {

    @ParameterizedTest
    @MethodSource("generateStringsAndExpectedResults")
    fun `Converting various strings to boolean should produce proper result (especially for null and empty string)`(
        argument: Pair<String?, Boolean?>
    ) {
        val (string, expectedResult) = argument
        val result = string.toBooleanOrNull()
        assertThat(result).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun generateStringsAndExpectedResults() = listOf(
            null to null,
            "" to null,
            "f" to null,
            "t" to null,
            "fal" to null,
            "tru" to null,
            "abcd" to null,
            "xfalse" to null,
            "falsex" to null,
            "xtrue" to null,
            "truex" to null,
            /////////////////
            "false" to false,
            "FalSE" to false,
            "true" to true,
            "TrUE" to true,
            " fAlse \t " to false,
            " tRue \t " to true
        )
    }
}
