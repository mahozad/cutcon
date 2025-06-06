package ir.mahozad.cutcon.converter

import ir.mahozad.cutcon.assetsPath
import ir.mahozad.cutcon.convertSvgToPng
import ir.mahozad.cutcon.model.CoverOptions
import ir.mahozad.cutcon.model.IntroOptions
import ir.mahozad.cutcon.model.Quality
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.io.path.div

/**
 * See https://trac.ffmpeg.org/wiki/Encode/MP3
 *
 * Could also first convert the input to mp3 and then add metadata to it via another command:
 * - `ffmpeg -nostdin -ss 5 -to 12 -i input.ts -map a temp.mp3`
 * - `ffmpeg -i temp.mp3 -i cover.png -map 0:0 -map 1:0 -c copy
 *    id3v2_version 3 -metadata:s:v title="Album cover" -metadata:s:v comment="Cover (front)" output.mp3`
 *
 * For adding an album art see:
 *  - https://stackoverflow.com/a/73706680
 *  - https://stackoverflow.com/a/24840831
 *  - https://stackoverflow.com/q/23581866
 */
class Mp3Converter(dispatcher: CoroutineDispatcher) : Converter(dispatcher) {

    override fun ffmpegOptions(
        quality: Quality,
        introOptions: IntroOptions,
        coverOptions: CoverOptions,
        flags: ConverterFlags
    ) = listOf(
        "-i" to (coverOptions.path ?: defaultAlbumArtPath).toString(), // Adds an image to be used as album art
        "-map" to "0:a",  // Selects the audio from the first input
        "-map" to "1:0",  // Selects first stream from the second input
        "-c:1" to "copy", // Sets the encoding for the second input to "copy" (otherwise, FFmpeg may convert the image)
        "-b:a" to qualityOptionValue(quality),
        // Sets ID3 version; version 4 doesn't seem to work on Windows
        // Also, additional options (might need to add them as well):
        // "-metadata:s:v" to """title="Album cover"""",
        // "-metadata:s:v" to """comment="Cover (front)"""",
        // "-f" to "mp3", // Specify output format explicitly
        "-id3v2_version" to "3"
    )

    /**
     * Allowed bit rates are 8|16|24|32|40|48|64|80|96|112|128|160|192|224|256|320
     */
    private fun qualityOptionValue(quality: Quality) = when (quality) {
        Quality.LOWEST -> "96k"
        Quality.LOW -> "128k"
        Quality.MEDIUM -> "192k"
        Quality.HIGH -> "256k"
        Quality.HIGHEST -> "320k"
    }

    companion object {
        private const val DEFAULT_ALBUM_ART_SIZE = 512f
        val defaultAlbumArtPath = convertSvgToPng(
            path = assetsPath / "logo-little-padding.svg",
            size = DEFAULT_ALBUM_ART_SIZE
        )
    }
}
