package ir.mahozad.cutcon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.model.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import uk.org.webcompere.systemstubs.properties.SystemProperties
import java.nio.file.Path
import java.time.LocalDate
import java.util.function.Consumer
import kotlin.io.path.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.div
import kotlin.io.path.inputStream
import kotlin.io.path.readBytes
import kotlin.io.path.readLines
import kotlin.io.path.writeBytes
import kotlin.time.Duration.Companion.seconds

class UtilitiesTest {

    @Test
    fun `The logs directory should be a cross-platform directory`() {
        val logbackConfigFile = getResourceAsPath("logback.xml")
        val lines = logbackConfigFile.readLines()
        val fileLine = lines.find { line -> line.matches(Regex("""\s*<file>.+""")) }
        val archiveLine = lines.find { line -> line.matches(Regex("""\s*<fileNamePattern>.+""")) }
        assertThat(fileLine).contains("<file>\${user.home}/\${APP_NAME}/")
        assertThat(archiveLine).contains("<fileNamePattern>\${user.home}/\${APP_NAME}/")
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DetectMimeTypeTest {
        @ParameterizedTest
        @MethodSource("generatePathsAndExpectedResults")
        fun `Detecting various files should return proper mime type`(
            argument: Pair<Path, String?>
        ) {
            val (path, expectedResult) = argument
            val result = path.detectMimeType()
            assertThat(result).isEqualTo(expectedResult)
        }

        private fun generatePathsAndExpectedResults() = listOf(
            createTempFile(suffix = ".png") to "image/png",
            createTempFile(suffix = ".mp3") to "audio/mpeg",
            createTempFile(suffix = ".mp4") to "video/mp4",
            createTempFile(suffix = ".ts") to "video/mp2t",
            createTempFile(suffix = ".txt") to "text/plain",
            createTempFile(suffix = ".abxzyc") to null,
            createTempFile(suffix = ".png.mp3") to "audio/mpeg", // Multipart file extension
            createTempFile(suffix = ".mp3.png") to "image/png", // Multipart file extension
            createTempFile(suffix = "") to null, // No file extension
            Path("non-existent-file.png") to null // Non-existent file
        )
    }

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

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DecodeImageTest {
        @Test
        fun `Decoding a PNG file should produce proper result`() {
            val file = getResourceAsPath("test.png")
            val result = decodeImage(file)
            val resultData = result
                ?.asSkiaBitmap()
                ?.let(Image::makeFromBitmap)
                ?.encodeToData(EncodedImageFormat.PNG) // Stores as PNG to be lossless
                ?: error("Could not encode image as png")
            val referencePath = getResourceAsPath("reference/10.png")
            val actualPath = (createTempDirectory() / "image.png")
            actualPath.writeBytes(resultData.bytes)
            assert(resultData.bytes.contentEquals(referencePath.readBytes())) {
                "The image '$actualPath' does not match the reference '$referencePath'"
            }
        }

        @Test
        fun `Decoding a JPEG file should produce proper result`() {
            val file = getResourceAsPath("test.jpg")
            val result = decodeImage(file)
            val resultData = result
                ?.asSkiaBitmap()
                ?.let(Image::makeFromBitmap)
                ?.encodeToData(EncodedImageFormat.PNG) // Stores as PNG to be lossless
                ?: error("Could not encode image as png")
            val referencePath = getResourceAsPath("reference/11.png")
            val actualPath = (createTempDirectory() / "image.png")
            actualPath.writeBytes(resultData.bytes)
            assert(resultData.bytes.contentEquals(referencePath.readBytes())) {
                "The image '$actualPath' does not match the reference '$referencePath'"
            }
        }

        @Test
        fun `Decoding an SVG file with its intrinsic size should produce proper result`() {
            val file = getResourceAsPath("test.svg")
            val result = decodeImage(file)
            val resultData = result
                ?.asSkiaBitmap()
                ?.let(Image::makeFromBitmap)
                ?.encodeToData(EncodedImageFormat.PNG) // Stores as PNG to be lossless
                ?: error("Could not encode image as png")
            val referencePath = getResourceAsPath("reference/12.png")
            val actualPath = (createTempDirectory() / "image.png")
            actualPath.writeBytes(resultData.bytes)
            assert(resultData.bytes.contentEquals(referencePath.readBytes())) {
                "The image '$actualPath' does not match the reference '$referencePath'"
            }
        }

        @Test
        fun `Decoding an SVG file with overridden size should produce proper result`() {
            val file = getResourceAsPath("test.svg")
            val result = decodeImage(file, 73f)
            val resultData = result
                ?.asSkiaBitmap()
                ?.let(Image::makeFromBitmap)
                ?.encodeToData(EncodedImageFormat.PNG) // Stores as PNG to be lossless
                ?: error("Could not encode image as png")
            val referencePath = getResourceAsPath("reference/13.png")
            val actualPath = (createTempDirectory() / "image.png")
            actualPath.writeBytes(resultData.bytes)
            assert(resultData.bytes.contentEquals(referencePath.readBytes())) {
                "The image '$actualPath' does not match the reference '$referencePath'"
            }
        }

        /**
         * SVGZ is NOT supported by skia (at least yet).
         */
        @Test
        fun `Decoding a compressed SVGZ file should produce null`() {
            val file = getResourceAsPath("test.svgz")
            val result = decodeImage(file)
            assertThat(result).isNull()
        }

        @Test
        fun `Decoding an unsupported image format file should produce null`() {
            val file = getResourceAsPath("test.avif")
            val result = decodeImage(file)
            assertThat(result).isNull()
        }

        @Test
        fun `Decoding a non-image file should produce null`() {
            val file = getResourceAsPath("test.ts")
            val result = decodeImage(file)
            assertThat(result).isNull()
        }

        @Test
        fun `Decoding a non-existent file should produce null`() {
            val file = Path("non-existent-file.png") // Non-existent file
            val result = decodeImage(file)
            assertThat(result).isNull()
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class NormalizeDigitsTest {
        @ParameterizedTest
        @MethodSource("generateStringsAndExpectedResults")
        fun `Detecting various files should return proper mime type`(
            argument: Pair<String, String>
        ) {
            val (string, expectedResult) = argument
            val result = string.normalizeDigits()
            assertThat(result).isEqualTo(expectedResult)
        }

        private fun generateStringsAndExpectedResults() = listOf(
            "" to "",
            " " to " ",
            "a" to "a",
            "آ" to "آ",
            "1" to "1",
            "۱" to "1",
            "۱1۲۳4ab .آ 4 ـ" to "11234ab .آ 4 ـ"
        )
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class ToTwoDigitTest {
        @ParameterizedTest
        @MethodSource("generateNumbersAndExpectedResults")
        fun `Detecting various files should return proper mime type`(
            argument: Pair<Int, String>
        ) {
            val (number, expectedResult) = argument
            val result = number.toTwoDigit()
            assertThat(result).isEqualTo(expectedResult)
        }

        private fun generateNumbersAndExpectedResults() = listOf(
            0 to "00",
            1 to "01",
            9 to "09",
            12 to "12",
            99 to "99"
        )
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CompareVersionStringsTest {
        @ParameterizedTest
        @MethodSource("generateVersionsAndExpectedResults")
        fun `Converting various versions should return proper numbers`(
            argument: Triple<String, String?, VersionComparisonResult>
        ) {
            val (thisVersion, thatVersion, expectedResult) = argument
            val result = compareVersionStrings(thisVersion, thatVersion)
            assertThat(result).isEqualTo(expectedResult)
        }

        private fun generateVersionsAndExpectedResults() = listOf(
            Triple("1", null, VersionComparisonResult.NEWER),
            Triple("1", "", VersionComparisonResult.NEWER),
            Triple("1", "0", VersionComparisonResult.NEWER),
            Triple("0", "0", VersionComparisonResult.SAME),
            Triple("0", "1", VersionComparisonResult.OLDER),
            Triple("1.2", "4.3", VersionComparisonResult.OLDER),
            Triple("3.2", "1.4", VersionComparisonResult.NEWER),
            Triple("0.0.1", "0.1.0", VersionComparisonResult.OLDER),
            Triple("0.1.0", "0.0.1", VersionComparisonResult.NEWER),
            Triple("1.2.3", "2.0.0", VersionComparisonResult.OLDER),
            Triple("2.0.0", "1.2.3", VersionComparisonResult.NEWER),
            Triple("2.3.0", "2.1.0", VersionComparisonResult.NEWER),
            Triple("2.3.0", "2.1.7", VersionComparisonResult.NEWER),
            Triple("2.1.7", "2.1.7", VersionComparisonResult.SAME),
            Triple(" 2.1.7", "2.1.7  ", VersionComparisonResult.SAME),
            Triple("2.1.7", null, VersionComparisonResult.NEWER),
            Triple("2.1.7", "", VersionComparisonResult.NEWER),
            Triple("2.1", "1.7.18", VersionComparisonResult.NEWER),
            Triple("2.1.0", "1.7.18-rc01", VersionComparisonResult.NEWER),
            Triple("2.1.7-alpha03", "2.1.7", VersionComparisonResult.OLDER),
            Triple("2.1.7", "2.1.7-alpha03", VersionComparisonResult.NEWER),
            Triple("2.1.7-alpha03", "2.1.7-rc", VersionComparisonResult.OLDER),
            Triple("2.1.7-alpha03", "2.1.7-beta01", VersionComparisonResult.OLDER),
            Triple("2.1.7-alpha03", "2.1.7-alpha04", VersionComparisonResult.OLDER),
            Triple("2.1.7-alpha04", "2.1.7-alpha03", VersionComparisonResult.NEWER),
            Triple("2.1.7-beta03", "2.1.7-rc01", VersionComparisonResult.OLDER),
            Triple("2.1.7-beta03", "2.1.7-alpha01", VersionComparisonResult.NEWER),
            Triple("2.1.7-beta.1", "2.1.7-alpha.3", VersionComparisonResult.NEWER),
            Triple("2.1.7-9", "2.1.7-8", VersionComparisonResult.NEWER),
            Triple("2.1.7-8", "2.1.7-9", VersionComparisonResult.OLDER)
        )
    }

    @Nested
    inner class CalculateMaxSizeInFrameTest {
        @Test
        fun `When desired ratio is 16_9 and frame ratio is greater than desired ratio`() {
            val result = calculateMaxSizeInFrame(
                frameWidth = 320.dp,
                frameHeight = 90.dp,
                displayDensity = 1f,
                desiredAspectRatio = 16f / 9f
            )
            assertThat(result).isEqualTo(DpSize(160.dp, 90.dp))
        }

        @Test
        fun `When desired ratio is 16_9 and frame ratio is less than desired ratio`() {
            val result = calculateMaxSizeInFrame(
                frameWidth = 45.dp,
                frameHeight = 90.dp,
                displayDensity = 1f,
                desiredAspectRatio = 16f / 9f
            )
            assertThat(result).isEqualTo(DpSize(45.dp, (45f / AspectRatio.W16H9.ratio!!).dp))
        }

        @Test
        fun `When desired ratio is 4_3 and frame ratio is greater than desired ratio`() {
            val result = calculateMaxSizeInFrame(
                frameWidth = 800.dp,
                frameHeight = 300.dp,
                displayDensity = 1f,
                desiredAspectRatio = 4f / 3f
            )
            assertThat(result).isEqualTo(DpSize((300 * (4f / 3f)).dp, 300.dp))
        }

        @Test
        fun `When desired ratio is 4_3 and frame ratio is less than desired ratio`() {
            val result = calculateMaxSizeInFrame(
                frameWidth = 200.dp,
                frameHeight = 300.dp,
                displayDensity = 1f,
                desiredAspectRatio = 4f / 3f
            )
            assertThat(result).isEqualTo(DpSize(200.dp, (200 / (4f / 3f)).dp))
        }

        /**
         * When the display scaling is something other than 100% (1.0).
         * This is especially prevalent in laptops.
         * Display scaling can be changed via OS settings.
         */
        @Nested
        inner class DisplayScaled {
            @Test
            fun `When display scaling is 125 percent and the image is taller than the frame`() {
                val result = calculateMaxSizeInFrame(
                    frameWidth = 300.dp,
                    frameHeight = 200.dp,
                    displayDensity = 1.25f,
                    desiredAspectRatio = 3f / 6f
                )
                assertThat(result).isEqualTo(DpSize(80.dp, 200.dp))
            }

            @Test
            fun `When display scaling is 125 percent and the image is wider than the frame`() {
                val result = calculateMaxSizeInFrame(
                    frameWidth = 300.dp,
                    frameHeight = 200.dp,
                    displayDensity = 1.25f,
                    desiredAspectRatio = 6f / 3f
                )
                assertThat(result).isEqualTo(DpSize(300.dp, 120.dp))
            }
        }
    }

    @Nested
    inner class IsValidIpTest {
        @Test
        fun `An empty string should be invalid`() {
            val result = "".isValidIp()
            assertThat(result).isFalse()
        }

        @Test
        fun `A string with no IP in it should be invalid`() {
            val result = "hello1234abc".isValidIp()
            assertThat(result).isFalse()
        }

        @Test
        fun `A string with IP of all Latin digits should be valid`() {
            val result = "192.168.1.50".isValidIp()
            assertThat(result).isTrue()
        }

        @Test
        fun `A string with letters and IP of all Latin digits should be invalid`() {
            val result = "hello192.168.1.50abc".isValidIp()
            assertThat(result).isFalse()
        }

        @Test
        fun `A string with IP of all Farsi digits should be valid`() {
            val result = "۱۹۲.۱۶۸.۱.۵۰".isValidIp()
            assertThat(result).isTrue()
        }

        @Test
        fun `A string with letters and IP of all Farsi digits should be invalid 1`() {
            val result = "hello۱۹۲.۱۶۸.۱.۵۰abc".isValidIp()
            assertThat(result).isFalse()
        }

        @Test
        fun `A string with letters and IP of all Farsi digits should be invalid 2`() {
            val result = "سلام۱۹۲.۱۶۸.۱.۵۰آب".isValidIp()
            assertThat(result).isFalse()
        }

        @Test
        fun `A string with IP of mixed digits should be valid`() {
            val result = "۱9۲.۱۶8.۱.۵۰".isValidIp()
            assertThat(result).isTrue()
        }

        @Test
        fun `A string with letters and IP of mixed digits should be invalid`() {
            val result = "hello۱9۲.۱۶8.۱.۵۰abc".isValidIp()
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @ValueSource(strings = ["192.168.1.", "192.168.1", "۱192.168.1.50"])
        fun `A string with invalid IP should be invalid`(string: String) {
            val result = string.isValidIp()
            assertThat(result).isFalse()
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PathTrimTest {
        @ParameterizedTest
        @MethodSource("generatePathsAndExpectedResults")
        fun `Trimming various paths should produce proper result`(
            argument: Pair<Path, Path>
        ) {
            val (path, expectedResult) = argument
            val result = path.trim(maxLength = 30)
            assertThat(result).isEqualTo(expectedResult)
        }

        @Suppress("SpellCheckingInspection")
        private fun generatePathsAndExpectedResults() = listOf(
            Path("a/b/c") to Path("a/b/c"),
            Path("""C:/Users/User/Desktop/abc.mp4""") to Path("C:/Users/User/Desktop/abc.mp4"),
            Path("""C:/Users/User/Desktop/abcde.mp4""") to Path(".../User/Desktop/abcde.mp4"),
            Path("abcdefghiklmnopqrstuvwxyzABCDE") to Path("abcdefghiklmnopqrstuvwxyzABCDE"),
            Path("abcdefghiklmnopqrstuvwxyzABCDEF") to Path("...efghiklmnopqrstuvwxyzABCDEF"),
            Path("x/abcdefghiklmnopqrstuvwxyzABC") to Path("x/abcdefghiklmnopqrstuvwxyzABC"),
            Path("x/abcdefghiklmnopqrstuvwxyzABCDE") to Path("abcdefghiklmnopqrstuvwxyzABCDE"),
            Path(".../abcdefghiklmnopqrstuvwxyzA") to Path(".../abcdefghiklmnopqrstuvwxyzA"),      // Has ... as a directory name
            Path(".../abcdefghiklmnopqrstuvwxyzAB") to Path("abcdefghiklmnopqrstuvwxyzAB"),        // Has ... as a directory name
            Path(".../abcdefghiklmnopqrstuvwxyzABCDE") to Path("abcdefghiklmnopqrstuvwxyzABCDE"),  // Has ... as a directory name
            Path(".../abcdefghiklmnopqrstuvwxyzABCDEF") to Path("...efghiklmnopqrstuvwxyzABCDEF"), // Has ... as a directory name
            Path("a/b/c/d/.../abcdefghiklmnopqrst") to Path(".../d/.../abcdefghiklmnopqrst"),      // Has ... as a directory name
            Path(".../a/b/c/d/abcdefghiklmnopqrst") to Path(".../b/c/d/abcdefghiklmnopqrst"),      // Has ... as a directory name
            Path("""C:\Users\User\Desktop\abcdefghiklmnopqrstuvwxyzABCDEF""") to Path("...efghiklmnopqrstuvwxyzABCDEF"),
            Path("abcdefgh/qwertyiop/zxcvbnm") to Path("abcdefgh/qwertyiop/zxcvbnm"),
            Path("abcdefghjkl/qwertyiop/zxcvbnm") to Path("abcdefghjkl/qwertyiop/zxcvbnm"),
            Path("abcdefghjklop/qwertyiop/zxcvbnm") to Path(".../qwertyiop/zxcvbnm")
        )

        /**
         * See https://youtrack.jetbrains.com/issue/KT-62225 for why.
         */
        @EnabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
        @ParameterizedTest
        @MethodSource("generateWindowPathsAndExpectedResults")
        fun `Trimming paths with backslash separators should produce proper result`(
            argument: Pair<Path, Path>
        ) {
            val (path, expectedResult) = argument
            val result = path.trim(maxLength = 30)
            assertThat(result).isEqualTo(expectedResult)
        }

        private fun generateWindowPathsAndExpectedResults() = listOf(
            Path("""a\b\c""") to Path("a/b/c"),
            Path("""C:\Users\User\Desktop\abc.mp4""") to Path("C:/Users/User/Desktop/abc.mp4"),
            Path("""C:\Users\User\Desktop\abcde.mp4""") to Path(".../User/Desktop/abcde.mp4")
        )
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PathToLtrString {
        @Test
        fun `Converting a Linux path to LTR string should produce proper result`() {
            val path = Path(""".../موسیقی/abcdefghijklmnopq""")
            val result = path.toLtrString()
            assertThat(result).isEqualTo("\u202A.../\u202Aموسیقی/\u202Aabcdefghijklmnopq")
        }

        /**
         * See https://youtrack.jetbrains.com/issue/KT-62225 for why.
         */
        @EnabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
        @Test
        fun `Converting a Windows path to LTR string should produce proper result`() {
            val path = Path("""...\موسیقی\abcdefghijklmnopq""")
            val result = path.toLtrString()
            assertThat(result).isEqualTo("\u202A.../\u202Aموسیقی/\u202Aabcdefghijklmnopq")
        }
    }

    @Nested
    inner class ParseMarkdownAsChangelogTest {
        @Test
        fun `Parsing changelog that contains language-agnostic entries (@ language tag) should succeed`() {
            val expected = ChangelogVersion(
                name = "1.2.5",
                date = LocalDate.of(2023, 6, 19),
                categories = listOf(
                    ChangelogCategory(
                        type = CategoryType.INTERNAL,
                        entries = listOf(
                            ChangelogEntry(items = listOf("Add some changes")),
                            ChangelogEntry(items = listOf("Update some TODOs")),
                            ChangelogEntry(items = listOf("Add another change")),
                            ChangelogEntry(items = listOf("Refactor build code")),
                            ChangelogEntry(items = listOf("Rename some methods"))
                        )
                    )
                )
            )
            val result = getResourceAsPath("test-2.md")
                .inputStream()
                .use { parseMarkdownAsChangelog(it, languageTag = /* Does NOT matter */ "En") }
                .versions
                .first()
            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun `For English language`() {
            val expected = Changelog(
                versions = listOf(
                    ChangelogVersion(
                        name = "1.2.0",
                        date = LocalDate.of(2023, 6, 15),
                        categories = listOf(
                            ChangelogCategory(
                                type = CategoryType.FEATURE,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("Add progress/seek bar and live button to mini player")),
                                    ChangelogEntry(items = listOf("Add a new slow speed (0.75)"))
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.BUGFIX,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("Fix the bug with speed number being reset when toggling side panel or mini player"))
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.UPDATE,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("Redesign the speed input")),
                                    ChangelogEntry(items = listOf("Update some of the icons")),
                                    ChangelogEntry(items = listOf("Update the clip creation label"))
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.INTERNAL,
                                entries = listOf(
                                    ChangelogEntry(
                                        items = listOf(
                                            "Update the inputs aesthetics",
                                            "Also, change how they handle clicks"
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    ChangelogVersion(
                        name = "1.1.0",
                        date = LocalDate.of(2023, 6, 8),
                        categories = listOf(
                            ChangelogCategory(
                                type = CategoryType.FEATURE, entries = listOf(
                                    ChangelogEntry(items = listOf("Add native splash screen")),
                                    ChangelogEntry(items = listOf("Show success window with a notification sound when the clip creation is done")),
                                    ChangelogEntry(items = listOf("Add mini player mode"))
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.BUGFIX,
                                entries = listOf(
                                    ChangelogEntry(
                                        items = listOf(
                                            "Fix minor bugs",
                                            "bug 1",
                                            "bug 2",
                                            "bug 3"
                                        )
                                    )
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.REMOVAL,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("Remove the screenshot button"))
                                )
                            )
                        )
                    ),
                    ChangelogVersion(
                        name = "1.0.0",
                        date = LocalDate.of(2023, 6, 1),
                        categories = listOf(
                            ChangelogCategory(
                                type = CategoryType.INTERNAL,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("Release the first version of the app"))
                                )
                            )
                        )
                    )
                )
            )
            val result = getResourceAsPath("test-1.md")
                .inputStream()
                .use { parseMarkdownAsChangelog(it, languageTag = "En") }
            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun `For Farsi (Persian) language`() {
            val expected = Changelog(
                versions = listOf(
                    ChangelogVersion(
                        name = "1.2.0",
                        date = LocalDate.of(2023, 6, 15),
                        categories = listOf(
                            ChangelogCategory(
                                type = CategoryType.FEATURE,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("اضافه شدن نوار پیشرفت و جلو/عقب و دکمه پخش زنده به پخش\u200Cکننده مینی")),
                                    ChangelogEntry(items = listOf("اضافه شدن یک سرعت جدید (۰٫۷۵)"))
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.BUGFIX,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("رفع مشکل ریست شدن عدد ورودی سرعت هنگام فعال یا غیر فعال کردن پخش\u200Cکننده مینی"))
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.UPDATE,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("بازطراحی ورودی سرعت")),
                                    ChangelogEntry(items = listOf("بروزرسانی بعضی از آیکون\u200Cها")),
                                    ChangelogEntry(items = listOf("بروزرسانی برچسب ایجاد کلیپ"))
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.INTERNAL,
                                entries = listOf(
                                    ChangelogEntry(
                                        items = listOf(
                                            "بروزرسانی ظاهر ورودی\u200Cها",
                                            "همچنین، تغییر نحوه\u200Cی انجام کلیک بر روی آن\u200Cها"
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    ChangelogVersion(
                        name = "1.1.0",
                        date = LocalDate.of(2023, 6, 8),
                        categories = listOf(
                            ChangelogCategory(
                                type = CategoryType.FEATURE, entries = listOf(
                                    ChangelogEntry(items = listOf("اضافه شدن صفحه اسپلش (تصویر شروع)")),
                                    ChangelogEntry(items = listOf("نمایش پنجره موفقیت همراه با یک افکت صوتی هنگام تکمیل ایجاد کلیپ")),
                                    ChangelogEntry(items = listOf("اضافه شدن پخش\u200Cکننده مینی"))
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.BUGFIX,
                                entries = listOf(
                                    ChangelogEntry(
                                        items = listOf(
                                            "رفع برخی باگ\u200Cهای جزئی",
                                            "باگ ۱",
                                            "باگ ۲",
                                            "باگ ۳"
                                        )
                                    )
                                )
                            ),
                            ChangelogCategory(
                                type = CategoryType.REMOVAL,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("حذف دکمه اسکرین\u200Cشات"))
                                )
                            )
                        )
                    ),
                    ChangelogVersion(
                        name = "1.0.0",
                        date = LocalDate.of(2023, 6, 1),
                        categories = listOf(
                            ChangelogCategory(
                                type = CategoryType.INTERNAL,
                                entries = listOf(
                                    ChangelogEntry(items = listOf("انتشار نخستین ویرایش پایدار برنامه"))
                                )
                            )
                        )
                    )
                )
            )
            val result = getResourceAsPath("test-1.md")
                .inputStream()
                .use { parseMarkdownAsChangelog(it, languageTag = "Fa") }
            assertThat(result).isEqualTo(expected)
        }
    }

    @Nested
    inner class ParseStringToDurationTest {
        @Test
        fun `Parsing an empty string should return null`() {
            val duration = "".toDuration()
            assertThat(duration).isNull()
        }

        @Test
        fun `Parsing an invalid string should return null`() {
            val duration = "N/A".toDuration()
            assertThat(duration).isNull()
        }

        @Test
        fun `Parsing a valid string with all non-zero components should return valid duration`() {
            val duration = "1:24:17".toDuration()!!
            assertThat(duration).isEqualTo(5057.seconds)
        }

        @Test
        fun `Parsing a valid string with zero hour should return valid duration`() {
            val duration = "0:24:17".toDuration()!!
            assertThat(duration).isEqualTo(1457.seconds)
        }

        @Test
        fun `Parsing a valid string with no hour should return valid duration`() {
            val duration = "24:17".toDuration()!!
            assertThat(duration).isEqualTo(1457.seconds)
        }

        @Test
        fun `Parsing a valid string with no hour and zero minute should return valid duration`() {
            val duration = "0:17".toDuration()!!
            assertThat(duration).isEqualTo(17.seconds)
        }
    }

    @Nested
    inner class ParseDateTest {
        @Test
        fun `parseDate should return null when input is empty`() {
            val date = "".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should return null when input is not valid date`() {
            val date = "meow-to-it".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should return null when date misses month or day number`() {
            val date = "1401--10".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should return null when date misses month or day part`() {
            val date = "1401-10".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should return null when date misses year part`() {
            val date = "10-10".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should return null when day is zero`() {
            val date = "1401-06-0".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should return null when month is zero`() {
            val date = "1401-0-03".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should return null when month is greater than 12`() {
            val date = "1401-13-03".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should return null when day is greater than 31`() {
            val date = "1401-06-32".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should not convert to another calendar system when year is greater than 1800`() {
            val date = "2001-06-03".parseAsDate()!!
            assertThat(date.year).isEqualTo(2001)
            assertThat(date.monthValue).isEqualTo(6)
            assertThat(date.dayOfMonth).isEqualTo(3)
        }

        @Test
        fun `parseDate should convert to Gregorian calendar system when year is less than 1800`() {
            val date = "1391-06-03".parseAsDate()!!
            assertThat(date.year).isEqualTo(2012)
            assertThat(date.monthValue).isEqualTo(8)
            assertThat(date.dayOfMonth).isEqualTo(24)
        }

        @Test
        fun `parseDate should return a gregorian date when input is well-formed jalali date`() {
            val date = "1401-06-03".parseAsDate()!!
            assertThat(date.year).isEqualTo(2022)
            assertThat(date.monthValue).isEqualTo(8)
            assertThat(date.dayOfMonth).isEqualTo(25)
        }

        @Test
        fun `parseDate should return a date when date has redundant delimiters`() {
            val date = "1401..06,,,03".parseAsDate()!!
            assertThat(date.year).isEqualTo(2022)
            assertThat(date.monthValue).isEqualTo(8)
            assertThat(date.dayOfMonth).isEqualTo(25)
        }

        @Test
        fun `parseDate should return null when date has more than 3 components`() {
            val date = "1401/06/03/04".parseAsDate()
            assertThat(date).isNull()
        }

        @Test
        fun `parseDate should return a date when date has surrounding spaces`() {
            val date = "   1401/06/03  ".parseAsDate()!!
            assertThat(date.year).isEqualTo(2022)
            assertThat(date.monthValue).isEqualTo(8)
            assertThat(date.dayOfMonth).isEqualTo(25)
        }

        @Test
        fun `parseDate should return a date when date has eastern arabic digits`() {
            val date = "۱۴۰۱/۰۶/۰۳".parseAsDate()!!
            assertThat(date.year).isEqualTo(2022)
            assertThat(date.monthValue).isEqualTo(8)
            assertThat(date.dayOfMonth).isEqualTo(25)
        }

        @Test
        fun `parseDate should return a date when date has mixed western and eastern arabic digits`() {
            val date = "۱۴۰۱/۰۶/03".parseAsDate()!!
            assertThat(date.year).isEqualTo(2022)
            assertThat(date.monthValue).isEqualTo(8)
            assertThat(date.dayOfMonth).isEqualTo(25)
        }

        @Test
        fun `parseDate should return a date when date has a single digit component`() {
            val date = "1401/6/03".parseAsDate()!!
            assertThat(date.year).isEqualTo(2022)
            assertThat(date.monthValue).isEqualTo(8)
            assertThat(date.dayOfMonth).isEqualTo(25)
        }

        @ParameterizedTest
        @ValueSource(chars = [' ', '\t', '-', '_', '.', ',', '،', ':', ';', '؛', '|', '/', '\\', 'و'])
        fun `parseDate should return date when date is delimited with these characters`(character: Char) {
            val date = "1401${character}06${character}03".parseAsDate()!!
            assertThat(date.year).isEqualTo(2022)
            assertThat(date.monthValue).isEqualTo(8)
            assertThat(date.dayOfMonth).isEqualTo(25)
        }
    }

    @Nested
    inner class HandleInputForTimeMinuteTest {
        @ParameterizedTest
        @ValueSource(strings = ["a", ".", "-", "س"])
        fun `When user enters any non-digit character, new value should be current (En)`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("3", "3$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("3")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["a", ".", "-", "س"])
        fun `When user enters any non-digit character, new value should be current (Fa)`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("۳", "۳$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("۳")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is empty and user enters any digit, new value should be exactly that digit`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("", argument, TextRange.One)
            assertThat(textFieldValue.text).isEqualTo(argument)
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["3", "۳"])
        fun `When current value is a digit and user clears input, new value should be empty`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute(argument, "", TextRange.Zero)
            assertThat(textFieldValue.text).isEqualTo("")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Zero)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is a digit less than '6' (3) and cursor is before it and user enters any digit, new value should be 'digit3'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("3", "${argument}3", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("${argument}3")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is a digit less than '6' (3) and cursor is after it and user enters any digit, new value should be '3digit'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("3", "3$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("3$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is a digit greater than '5' (6) and cursor is before it and user enters any digit, new value should be 'digit6'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("6", "${argument}6", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("${argument}6")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is a digit greater than '5' (6) and cursor is after it and user enters any digit, new value should be '6digit'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("6", "6$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("6$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is a zero (0) and cursor is after it and user enters any digit, new value should be '0digit'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("0", "0$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("0$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is two digit (13) and cursor is at start and user enters any digit, new value should be to 'digit3'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("13", "${argument}13", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("${argument}3")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is two zero (00) and cursor is in between and user enters any digit, new value should be '0digit'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("00", "0${argument}0", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("0$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is two digit (13) and cursor is at end and user enters any digit, new value should be current '13'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("13", "13$argument", TextRange.Three)
            assertThat(textFieldValue.text).isEqualTo("13")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is two digit (13) and cursor is in between and user enters any digit, new value should be '1digit'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("13", "1${argument}3", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("1$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["5", "۵"])
        fun `When current value is two digit (50) and cursor is in between and user enters a digit that is equal to current first digit (5), new value should be equal to '55'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("50", "5${argument}0", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("5$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["5", "۵"])
        fun `When current value is two digit (50) and cursor is at start and user enters a digit that is equal to current first digit (5), new value should be equal to '50'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("50", "${argument}50", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("${argument}0")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["1234", "5912", "6031", "8545"])
        fun `When input value has 3 or more digits (because it was entered very fast), new value should be equal to current`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeMinute("41", argument, TextRange.Three)
            assertThat(textFieldValue.text).isEqualTo("41")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }
    }

    @Nested
    inner class HandleInputForTimeSecondTest {
        @ParameterizedTest
        @ValueSource(strings = ["a", ".", "-", "س"])
        fun `When user enters any non-digit character, new value should be current`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("3", "3$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("3")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is empty and user enters any digit, new value should be exactly that digit`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("", argument, TextRange.One)
            assertThat(textFieldValue.text).isEqualTo(argument)
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["3", "۳"])
        fun `When current value is a digit and user clears input, new value should be empty`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond(argument, "", TextRange.Zero)
            assertThat(textFieldValue.text).isEqualTo("")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Zero)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "1", "4", "5", "۰", "۱", "۴", "۵"])
        fun `When current value is a digit less than '6' (3) and cursor is before it and user enters a digit less than '6', new value should be 'digit3'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("3", "${argument}3", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("${argument}3")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["6", "7", "9", "۶", "۷", "۹"])
        fun `When current value is a digit less than '6' (3) and cursor is before it and user enters a digit greater than '5', new value should be current '3'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("3", "${argument}3", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("3")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Zero)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is a digit less than '6' (3) and cursor is after it and user enters any digit, new value should be '3digit' (En)`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("3", "3$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("3$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is a digit less than '6' (3) and cursor is after it and user enters any digit, new value should be '3digit' (Fa)`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("۳", "۳$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("۳$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is a digit greater than '5' (6) and cursor is after it and user enters any digit, new value should be current`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("6", "6$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("6")
            assertThat(textFieldValue.selection).satisfiesAnyOf(
                Consumer { assertThat(it!!).isEqualTo(TextRange.One) },
                Consumer { assertThat(it!!).isEqualTo(TextRange.Two) }
            )
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "1", "4", "5", "۰", "۱", "۴", "۵"])
        fun `When current value is a digit greater than '5' (6) and cursor is before it and user enters a digit less than '6', new value should be 'digit6'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("6", "${argument}6", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("${argument}6")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @Test
        fun `When current value is a digit greater than '5' (6) and cursor is before it and user enters that same digit (6), new value should be '6'`() {
            val textFieldValue = handleInputForTimeSecond("6", "66", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("6")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["6", "7", "9", "۶", "۷", "۹"])
        fun `When current value is a digit greater than '5' (6) and cursor is before it and user enters a digit greater than '5', new value should be current '6'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("6", "${argument}6", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("6")
            assertThat(textFieldValue.selection).satisfiesAnyOf(
                Consumer { assertThat(it!!).isEqualTo(TextRange.Zero) },
                Consumer { assertThat(it!!).isEqualTo(TextRange.One) }
            )
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "1", "4", "5", "۰", "۱", "۴", "۵"])
        fun `When current value is two digit (23) and cursor is at start and user enters a digit less than '6', new value should be 'digit3'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("23", "${argument}23", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("${argument}3")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "1", "4", "5", "۰", "۱", "۴", "۵"])
        fun `When current value is two zero (00) and cursor is in between and user enters a digit less than '6', new value should be '0digit'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("00", "0${argument}0", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("0$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is a zero (0) and cursor is after it and user enters any digit, new value should be '0digit'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("0", "0$argument", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("0$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["6", "7", "9", "۶", "۷", "۹"])
        fun `When current value is two digit (13) and cursor is at start and user enters a digit greater than '5', new value should be current '13'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("13", "${argument}13", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("13")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Zero)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is two digit (13) and cursor is at end and user enters any digit, new value should be current '13'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("13", "13$argument", TextRange.Three)
            assertThat(textFieldValue.text).isEqualTo("13")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "5", "6", "7", "9", "۰", "۵", "۶", "۷", "۹"])
        fun `When current value is two digit (13) and cursor is in between and user enters any digit, new value should be '1digit'`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("13", "1${argument}3", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("1$argument")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @Test
        fun `When current value is two digit (50) and cursor is in between and user enters a digit that is equal to current first digit (5), new value should be equal to '55'`() {
            val textFieldValue = handleInputForTimeSecond("50", "550", TextRange.Two)
            assertThat(textFieldValue.text).isEqualTo("55")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }

        @Test
        fun `When current value is two digit (50) and cursor is at start and user enters a digit that is equal to current first digit (5), new value should be equal to '50'`() {
            val textFieldValue = handleInputForTimeSecond("50", "550", TextRange.One)
            assertThat(textFieldValue.text).isEqualTo("50")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.One)
        }

        @ParameterizedTest
        @ValueSource(strings = ["1234", "5912", "6031", "8545"])
        fun `When input value has 3 or more digits (because it was entered very fast), new value should be equal to current`(
            argument: String
        ) {
            val textFieldValue = handleInputForTimeSecond("41", argument, TextRange.Three)
            assertThat(textFieldValue.text).isEqualTo("41")
            assertThat(textFieldValue.selection).isEqualTo(TextRange.Two)
        }
    }

    @Test
    fun `EmitLatestEvery should emit the latest value periodically`() = runTest {
        val results = mutableListOf<Int?>()
        flowOf(1, 2, 3)
            .emitLatestEvery(7.seconds)
            .take(5)
            .collect(results::add)
        assertThat(results).containsExactly(1, 2, 3, 3, 3)
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    inner class ColorToHexTest {
        @ParameterizedTest
        @MethodSource("generateColorsAndExpectedResults")
        fun `Formatting color to hex should produce correct result`(
            argument: Pair<Color, String>
        ) {
            val (color, expectedResult) = argument
            val result = color.toHex()
            assertThat(result).isEqualTo(expectedResult)
        }

        private fun generateColorsAndExpectedResults() = listOf(
            Color(0xffffff) to "#ffffff",
            Color(0x00ffff) to "#00ffff",
            Color(0xff00ff) to "#ff00ff",
            Color(0xffff00) to "#ffff00",
            Color(0x000000) to "#000000"
        )
    }
}
