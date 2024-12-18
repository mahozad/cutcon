package ir.mahozad.cutcon

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.properties.SystemProperties
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension

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

    @Nested
    @ExtendWith(SystemStubsExtension::class)
    @TestInstance(PER_CLASS)
    inner class GetCurrentOsTest {

        @SystemStub
        val systemProperties = SystemProperties()

        @ParameterizedTest
        @MethodSource("generateOsNamesAndExpectedResults")
        fun `When getting current OS, should return proper value`(
            argument: Pair<String, OS>
        ) {
            val (osName, expected) = argument
            systemProperties.set("os.name", osName)
            val result = getCurrentOs()
            assertThat(result).isEqualTo(expected)
        }

        fun generateOsNamesAndExpectedResults() = listOf(
            "win" to OS.WINDOWS,
            "windows" to OS.WINDOWS,
            "Windows" to OS.WINDOWS,
            "WINDOWS" to OS.WINDOWS,
            "Windows 8" to OS.WINDOWS,
            "Windows XP" to OS.WINDOWS,
            "windows 10" to OS.WINDOWS,
            "WINDOWS 10" to OS.WINDOWS,
            "  wiNDOws  7 " to OS.WINDOWS,
            "linux" to OS.LINUX,
            "LINUX" to OS.LINUX,
            "Mac OS X" to OS.MAC,
            "mac" to OS.MAC,
            "abcd" to OS.OTHER,
            "" to OS.OTHER
        )
    }
}
