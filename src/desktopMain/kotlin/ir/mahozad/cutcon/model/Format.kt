package ir.mahozad.cutcon.model

import androidx.compose.ui.res.loadImageBitmap
import ir.mahozad.cutcon.localization.Language

/**
 * These are the intersection of the formats supported by both FFmpeg and Skia [loadImageBitmap]
 * (except the SVG format which we support with a workaround because FFmpeg does not support it universally).
 */
enum class SupportedImageFormat(vararg val extensions: String) {
    SVG("svg" /* .svgz is NOT supported by skia (yet) */),
    PNG("png"),
    JPEG("jpg", "jpeg", "jfif", "jif", "jfi", "jpe"),
    WebP("webp"),
    GIF("gif"),
    BMP("bmp", "dib"),
    ICO("ico")
}

val supportedImageFileExtensions = SupportedImageFormat
    .entries
    .flatMap { it.extensions.toList() }
    .toTypedArray()

enum class LocalSourceSupportedFileType(vararg val extensions: String) {
    MP4("mp4"),
    MP3("mp3"),
    MKV("mkv"),
    TS("ts"),
    // We support displaying images but do not want to pollute this with all image types.
    // The user can either select all types in the dialog or drag images on the player box.
}

enum class Format(
    override val label: (Language) -> String,
    val actualName: (Source) -> String,
    val extension: (Source) -> String
) : Labeled {
    MP4(
        label = { it.messages.radLblFormatMp4 },
        actualName = { "MP4" },
        extension = { "mp4" }
    ),
    MP3(
        label = { it.messages.radLblFormatMp3 },
        actualName = { "MP3" },
        extension = { "mp3" }
    ),
    /**
     * Alternative names: ORIGINAL or SOURCE or COPY
     */
    RAW(
        label = { it.messages.radLblFormatRaw },
        actualName = Source::formatName,
        extension = Source::fileExtension
    )
}
