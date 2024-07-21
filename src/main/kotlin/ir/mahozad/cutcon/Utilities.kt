package ir.mahozad.cutcon

import androidx.compose.material.Colors
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import com.github.mfathi91.time.PersianDate
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.transformLatest
import org.apache.tika.Tika
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.file.Path
import java.time.LocalDate
import javax.imageio.ImageIO
import kotlin.io.path.*
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalStdlibApi::class)
private val hexFormat = HexFormat {
    upperCase = false
    number.prefix = ""
    number.removeLeadingZeros = false
}
private val digitRegex = Regex("[۰-۹0-9٠-٩]") // Farsi | Latin | Arabic
private val numberRegex = Regex("${digitRegex.pattern}+")
private val durationRegex = Regex("""^(\d{1,2}:){1,2}\d{1,2}$""")
private val ipRegex = Regex("""(${digitRegex.pattern}{1,3}\.){3}${digitRegex.pattern}{1,3}""")
private val dateDelimiterRegex = Regex("""[-و \t_.,،:;؛|/\\]""")
private val redundantDelimiterRegex = Regex("${dateDelimiterRegex.pattern}+")
private val tika = Tika()
private val logger = logger(name = "Utilities")

fun parseMarkdownAsChangelog(
    inputStream: InputStream,
    languageTag: String
): Changelog = inputStream
    .use { it.reader().readLines() }
    .dropWhile { !it.startsWith("## ") }
    .filterNot(String::isBlank)
    .fold(Changelog(versions = listOf())) { changelog, line ->
        if (line.startsWith("## ")) {
            val (name, date) = line
                .substringAfter("## v")
                .replace(Regex("[()]"), "")
                .split(' ')
            changelog + ChangelogVersion(name = name, date = LocalDate.parse(date), categories = listOf())
        } else if (line.startsWith("#### ")) {
            val type = line.substringAfter("#### ").lowercase().let {
                if ("feature" in it) {
                    CategoryType.FEATURE
                } else if ("bug" in it) {
                    CategoryType.BUGFIX
                } else if ("update" in it || "improve" in it) {
                    CategoryType.UPDATE
                } else if (Regex("(drop)|(clean)|(remov)|(delet)|(deprecat)") in it) {
                    CategoryType.REMOVAL
                } else {
                    CategoryType.INTERNAL
                }
            }
            val newVersion = changelog.versions.last() + ChangelogCategory(type = type, entries = listOf())
            changelog.replaceLast(newVersion)
        } else {
            val tag = line.substringAfter('(').substringBefore(')')
            if (tag.lowercase() !in setOf("@", languageTag.lowercase())) return@fold changelog
            val newItem = line
                .replaceFirst("($tag) ", "")
                .replace(Regex("""^\s*[-+*]?\s*"""), "")         // Bullets
                .replace(Regex(""" [(](\[|various).+[)]"""), "") // Commits
            val newEntries = if (line matches Regex("""^\s*-.+""")) { // If starts with -
                changelog.versions.last().categories.last().entries + ChangelogEntry(listOf(newItem))
            } else {
                val newEntry = changelog.versions.last().categories.last().entries.last() + newItem
                changelog.versions.last().categories.last().entries.dropLast(1) + newEntry
            }
            val newCategory = changelog.versions.last().categories.last().copy(entries = newEntries)
            val newCategories = changelog.versions.last().categories.dropLast(1) + newCategory
            changelog.replaceLast(changelog.versions.last().copy(categories = newCategories))
        }
    }.also {
        logger.info { "Parsed changelog" }
    }

fun convertSvgToPng(path: Path, size: Float? = null): Path? = runCatching {
    val imageData = decodeImage(path, size)?.toAwtImage()
    val imagePath = createTempFile(suffix = ".png")
    imagePath.outputStream().use { ImageIO.write(imageData, "PNG", it) }
    imagePath
}
    .onSuccess { logger.info { "Converted $path to PNG in $it" } }
    .onFailure { logger.warn(it) { "Could not convert $path to PNG" } }
    .getOrNull()


/**
 * If changed the parameter from [Path] to [InputStream], pay attention to the following notes.
 *
 * The stream should be read twice by [Tika] and the load*bitmap() methods.
 * But, a stream cannot be read multiple times. Also, input stream does not support mark and reset.
 * So, we should read all the stream bytes into memory (like `val bytes = stream.readBytes()`) but,
 * unlike what is said in https://stackoverflow.com/q/9501237, it may not be a bad idea because,
 * at the end, we want a decoded image and a decoded image has all its bytes in memory.
 */
fun decodeImage(
    path: Path,
    sizeOverrideIfVector: Float? = null
): ImageBitmap? {
    val mimeType = path.detectMimeType()
    return if (mimeType == "image/svg+xml") {
        runCatching { path.inputStream().use { loadSvgAsBitmap(it, sizeOverrideIfVector) } }.getOrNull()
    } else {
        runCatching { path.inputStream().use(::loadImageBitmap) }.getOrNull()
    }.also {
        logger.info { "Decoded image $path as $it" }
    }
}

/**
 * Passing null to [desiredSize] makes size default to the intrinsic dimensions of the SVG.
 *
 * See https://stackoverflow.com/q/13605248
 * and https://stackoverflow.com/q/4251383
 */
private fun loadSvgAsBitmap(
    stream: InputStream,
    desiredSize: Float? = null
): ImageBitmap {
    val density = Density(1f)
    val image = loadSvgPainter(stream, density).run {
        val aspectRatio = intrinsicSize.width / intrinsicSize.height
        val size = if (desiredSize == null) {
            intrinsicSize
        } else if (aspectRatio > 1f) {
            Size(desiredSize, desiredSize / aspectRatio)
        } else {
            Size(desiredSize * aspectRatio, desiredSize)
        }
        toAwtImage(density, LayoutDirection.Ltr, size)
    }
    val bufferedImage = BufferedImage(
        image.getWidth(null),
        image.getHeight(null),
        BufferedImage.TYPE_INT_ARGB
    )
    bufferedImage.createGraphics().apply {
        drawImage(image, 0, 0, null)
        dispose()
    }
    val file = createTempFile("temp.png")
    ImageIO.write(bufferedImage, "PNG", file.toFile())
    return file.inputStream().use(::loadImageBitmap)
}

/**
 * NOTE: Using Dispatchers.Main Requires the dependency
 *  org.jetbrains.kotlinx:kotlinx-coroutines-swing
 *  See https://github.com/JetBrains/compose-multiplatform/blob/master/CHANGELOG.md#111-mar-2022
 */
suspend fun <T> onMain(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Main, block)

val TextRange.Companion.One get() = TextRange(1)
val TextRange.Companion.Two get() = TextRange(2)
val TextRange.Companion.Three get() = TextRange(3)

@OptIn(ExperimentalStdlibApi::class)
fun Color.toHex() = "#${toArgb().toHexString(hexFormat).takeLast(6)}"

@Suppress("UnusedReceiverParameter")
val Colors.success: Color get() = Color(red = 88, green = 150, blue = 0)

fun Int.toTwoDigit() = "%02d".format(this)

fun String.isValidIp() = matches(ipRegex)

fun String.normalizeDigits() = replace(numberRegex) { it.value.toLong().toString() }

fun String.substringBetween(l: String, r: String) = substringAfter(l).substringBefore(r)

fun String.toDuration(): Duration? {
    val (s, m, h) = this
        .takeIf { it matches durationRegex }
        ?.split(':')
        ?.map(String::toInt)
        ?.reversed()
        ?.plus(0) // Ensures contains hour
        ?: return null
    return h.hours + m.minutes + s.seconds
}

fun Float.toQuality() = Quality
    .entries
    .single { it.value == roundToInt().coerceIn(1..5) }
    .also { logger.info { "Converted $this to quality $it" } }

fun calculateMaxSizeInFrame(
    frameWidth: Dp,
    frameHeight: Dp,
    displayDensity: Float,
    desiredAspectRatio: Float
): DpSize {
    val frameAspectRatio = (frameWidth / frameHeight)
    val (width, height) = if (frameAspectRatio > desiredAspectRatio) {
        frameHeight * desiredAspectRatio / displayDensity to frameHeight
    } else {
        frameWidth to frameWidth / desiredAspectRatio / displayDensity
    }
    return DpSize(width, height)
}

fun CoroutineScope.openAppLogFolder() {
    launch(Dispatchers.IO) {
        runCatching { System.getenv("AppData") }
            .onFailure { logger.warn(it) { "Could not get system AppData path" } }
            .getOrNull()
            ?.let(::Path)
            ?.resolve(BuildConfig.APP_NAME)
            ?.let(Path::toFile)
            ?.let(Desktop.getDesktop()::open)
            ?.also { logger.info { "Opened App log folder" } }
    }
}

/**
 * Continuously emit the latest value of the flow.
 *
 * See https://stackoverflow.com/q/67325125
 * and https://stackoverflow.com/q/54827455
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.emitLatestEvery(
    duration: Duration,
    onChange: suspend FlowCollector<T?>.() -> Unit = {}
) = transformLatest {
    onChange()
    while (true) {
        emit(it)
        delay(duration)
    }
}

/**
 * Note that Apache tika returns `application/octet-stream`
 * if it does not know what the type of file is.
 * For example, for .ts video files, instead of video/mp2t, it returns that.
 *
 * See https://stackoverflow.com/q/5507565
 */
fun Path.detectMimeType(): String? {
    if (extension.lowercase() == "ts") return "video/mp2t"
    return runCatching(tika::detect)
        .onSuccess { logger.info { "Detected mime type $it for $this" } }
        .onFailure { logger.warn(it) { "Could not detect mime type of $this" } }
        .getOrNull()
        .takeIf { it != "application/octet-stream" }
}

fun Path.trim(maxLength: Int): Path {
    fun String.prune(): String {
        return if (length <= maxLength) {
            this
        } else if ('/' !in this) {
            "...${takeLast(maxLength - 3)}"
        } else if ('/' !in removePrefix(".../")) {
            removePrefix(".../").prune()
        } else {
            (".../${removePrefix(".../").substringAfter('/')}").prune()
        }
    }
    return invariantSeparatorsPathString
        .prune()
        .let(::Path)
        .also { logger.info { "Trimmed '$this' to '$it'" } }
}

/**
 * u202A (Left-To-Right Embedding) has two purposes:
 * - If the path starts with ellipsis, makes the ellipsis stay on the left side in RTL layouts
 * - Makes the RTL directory names (like Persian names) in the path not break the left-to-right flow of the path
 */
fun Path.toLtrString(): String {
    return "\u202A$invariantSeparatorsPathString"
        .split('/')
        .joinToString(separator = "/\u202A")
        .also { logger.info { "Converted path $this to LTR" } }
}

fun String.parseAsDate(): LocalDate? = runCatching {
    val (year, month, day) = this
        .trim()
        .replace(dateDelimiterRegex, "-")
        .replace(redundantDelimiterRegex, "-")
        .split("-")
        .takeIf { it.size == 3 }
        ?.map(String::toInt)
        ?: return@runCatching null
    if (day !in 1..31 || month !in 1..12) {
        return@runCatching null
    } else if (year < 1800) {
        return@runCatching PersianDate.of(year, month, day).toGregorian()
    } else {
        return@runCatching LocalDate.of(year, month, day)
    }
}
    .onSuccess { logger.info { "Parsed $this as date $it" } }
    .onFailure { logger.debug(it) { "Could not parse $this as date" } }
    .getOrNull()

@Suppress("LiftReturnOrAssignment")
fun handleInputForTimeSecond(
    old: String,
    input: String,
    newCursor: TextRange
): TextFieldValue {
    if (input.any { !it.isDigit() }) {
        return TextFieldValue(old, TextRange.One)
    } else if (input.length < 2) {
        return TextFieldValue(input, TextRange(input.length))
    } else if (input.length < 3 && input.toInt() < 60) {
        return TextFieldValue(input, TextRange(newCursor.end))
    } else if (input[0] == input[1] && newCursor.end == 1) {
        return TextFieldValue(old, TextRange.One)
    } else if ("${input.first()}${input.last()}" == old) {
        return TextFieldValue(input.take(2), TextRange.Two)
    } else if (input.endsWith(old) && input.first().digitToInt() < 6) {
        return TextFieldValue("${input.first()}${input.last()}", TextRange.One)
    } else {
        return TextFieldValue(old, TextRange(newCursor.end - 1))
    }
}

@Suppress("LiftReturnOrAssignment")
fun handleInputForTimeMinute(
    old: String,
    input: String,
    newCursor: TextRange
): TextFieldValue {
    if (input.any { !it.isDigit() }) {
        return TextFieldValue(old, TextRange.One)
    } else if (input.length < 3) {
        return TextFieldValue(input, TextRange(newCursor.end))
    } else if (input[0] == input[1] && newCursor.end == 1) {
        return TextFieldValue(old, TextRange.One)
    } else if ("${input.first()}${input.last()}" == old) {
        return TextFieldValue(input.take(2), TextRange.Two)
    } else if (input.endsWith(old)) {
        return TextFieldValue("${input.first()}${input.last()}", TextRange(newCursor.end))
    } else {
        return TextFieldValue(old, TextRange.Two)
    }
}

@Suppress("LiftReturnOrAssignment")
fun compareVersionStrings(
    thisVersion: String,
    thatVersion: String?
): VersionComparisonResult {
    if (thisVersion.trim() == thatVersion?.trim()) return VersionComparisonResult.SAME
    val thisSuffix = thisVersion.substringAfter(delimiter = '-', missingDelimiterValue = "")
    val thatSuffix = thatVersion?.substringAfter(delimiter = '-', missingDelimiterValue = "") ?: ""
    val (thisMajor, thisMinor, thisPatch) = "$thisVersion.0.0".split(".").mapNotNull(String::toIntOrNull)
    val (thatMajor, thatMinor, thatPatch) = "$thatVersion.0.0.0".split(".").mapNotNull(String::toIntOrNull)
    if (thisMajor > thatMajor) {
        return VersionComparisonResult.NEWER
    } else if (thisMajor == thatMajor && thisMinor > thatMinor) {
        return VersionComparisonResult.NEWER
    } else if (thisMinor == thatMinor && thisPatch > thatPatch) {
        return VersionComparisonResult.NEWER
    } else if (thisSuffix > thatSuffix && thatSuffix.isNotEmpty()) {
        return VersionComparisonResult.NEWER
    } else {
        return VersionComparisonResult.OLDER
    }
}
