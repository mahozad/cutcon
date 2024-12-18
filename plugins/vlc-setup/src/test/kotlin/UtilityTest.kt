import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import uk.org.webcompere.systemstubs.properties.SystemProperties

class UtilityTest {

    @Nested
    @ExtendWith(SystemStubsExtension::class)
    @TestInstance(Lifecycle.PER_CLASS)
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
