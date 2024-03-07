package ir.mahozad.cutcon.converter

import ir.mahozad.cutcon.defaultOutputFrameRate
import ir.mahozad.cutcon.model.*
import ir.mahozad.cutcon.toHex
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.time.Duration

class Mp4Converter(dispatcher: CoroutineDispatcher) : Converter(dispatcher) {

    override fun ffmpegOptions(
        quality: Quality,
        introOptions: IntroOptions,
        coverOptions: CoverOptions,
        flags: ConverterFlags
    ) = inputOptions(introOptions, coverOptions, flags) + outputOptions(quality)

    private fun inputOptions(
        introOptions: IntroOptions,
        coverOptions: CoverOptions,
        flags: ConverterFlags
    ): List<FFmpegOption> {
        // To check if the input has video or not,
        // could have probably used org.bytedeco.*** classes or
        // could have used ffprobe (see https://stackoverflow.com/q/32278277)
        if (!flags.isVideoAvailableInInput) return emptyList()

        return buildList {
            if (introOptions.path != null && introOptions.duration > Duration.ZERO) {
                // The 4 options below create a video, n seconds long, from the image
                add("-loop" to "1")
                add("-framerate" to "$defaultOutputFrameRate")
                add("-t" to "${introOptions.duration.inWholeSeconds}s")
                add("-i" to introOptions.path.toString())
                // The 3 options below create a silent audio, n seconds long, to be used for the intro.
                // It is needed because if some part of final output has audio then all the output must have audio.
                // Could also have used anullsrc instead of aevalsrc=0 but the video size would differ a little bit.
                add("-t" to "${introOptions.duration.inWholeSeconds}s")
                add("-f" to "lavfi")
                add("-i" to "aevalsrc=0")
            }
            if (coverOptions.path != null) {
                add("-i" to coverOptions.path.toString())
            }
            add("-filter_complex" to filterChains(flags, introOptions, coverOptions))
        }
    }

    /**
     * Note: Every output of filter chains should be used somewhere.
     * otherwise, FFmpeg conversion will fail.
     * So, to ignore an output of a filter chain, use `[outputName] nullsink`
     * (`nullsink` does not produce any output, in contrast to `null` that outputs the input).
     */
    private fun filterChains(
        flags: ConverterFlags,
        introOptions: IntroOptions,
        coverOptions: CoverOptions
    ): String {
        val hasIntro = introOptions.path != null
        val coverIndex = if (hasIntro) 3 else 1
        return """
            ${videoFilterChains(flags)}
            ${introFilterChains(introOptions)}
            ${coverFilterChains(coverOptions, coverIndex)}
            ${finalFilterChains(hasIntro)}
        """
            .lines()
            .map(String::trim)
            .map { "    $it" } // Indentation to prettify
            .filter(String::isNotBlank)
            .joinToString(separator = "\n  ")
    }

    /**
     * Description of each filter chain:
     * - For the 0 (first) input (aka the video), apply de-interlacing (yet-another-de-interlacing-filter)
     *   and  name the output `media-de-interlaced`.
     *   To just copy the stream unmodified, use `copy` or `null` instead of `yadif=1`
     * - Most regular videos have sar (source/storage aspect ratio) of `1`.
     *   But some videos are anamorphic, meaning, they have a different sar (`16/11`).
     *   So, apply the sar to fix the anamorphic problem (to get the final width displayed on TVs)
     * - Multiplying width or height by a fraction in previous step may result in an odd number of pixels
     *   for width or height (for example, `843` pixels for width) which is not acceptable for a video.
     *   So, round the width and height to have an even number of pixels
     *   (for example, instead of `843` pixels, it becomes `844` pixels)
     * - We have applied the video sar to its dimensions in previous steps,
     *   so now we set the sar to `1` so all inputs will have sar of `1`.
     *   This is to prevent the `concat` filter chain in next steps from complaining
     * - Name the final output `media` for subsequent steps to use
     */
    private fun videoFilterChains(flags: ConverterFlags) = """
        [0] ${if (flags.isInterlacingFixEnabled) "yadif=1" else "null"} [media-de-interlaced];
        [media-de-interlaced] scale=iw*sar:ih [media-anamorphic-fixed];
        [media-anamorphic-fixed] pad=ceil(iw/2)*2:ceil(ih/2)*2 [media-odd-pixel-number-fixed];
        [media-odd-pixel-number-fixed] setsar=1/1 [media-source-aspect-ratio-normalized];
        [media-source-aspect-ratio-normalized] null [media];
    """

    /**
     * Description of each filter chain:
     * - Coerce maximum width and height of the intro image to be not larger than
     *   the video width and height, correspondingly, preserving the intro image aspect ratio.
     *   (Note that, `force_original_aspect_ratio` refers to aspect ratio of the second argument
     *   of filter chain (the media here) and that's the reason we could not use it for simplification)
     * - Use the video (media) and also use the intro image as a dummy input because `scale2ref` requires 2 inputs,
     *   to create an output in this step that has the same size as the video
     * - Use the output from the last step which has the same size as the main video
     *   to create a background with desired color for the intro in case
     *   the intro is smaller than the video or its aspect ratio is different from the video
     *   (because intro and video must have the same size i.e. one part of video cannot have different size than another part)
     * - Use the intro background from previous step and the possibly resized intro image and place the image on the background
     * - Like what was done for the main video, make sure that the intro has sar of `1` (just in case)
     * - Name the final output `intro` for subsequent steps to use
     */
    private fun introFilterChains(options: IntroOptions) = """
        [1][media] scale2ref='if(gt(main_a,a),min(main_w,iw),min(main_h,ih)*main_a)':'if(gt(main_a,a),min(main_w,iw)/main_a,min(main_h,ih))' [intro-image-max-size-coerced][media];
        [1][media] scale2ref=iw:ih [size-template][media];
        [size-template] drawbox=w=iw:h=ih:t=fill:color=${options.backgroundColor.toHex()} [intro-background];
        [intro-background][intro-image-max-size-coerced] overlay=x='(W-w)/2':y='(H-h)/2' [intro-with-background];
        [intro-with-background] setsar=1/1 [intro-source-aspect-ratio-normalized];
        [intro-source-aspect-ratio-normalized] null [intro];
    """.takeIf { options.path != null } ?: ""

    /**
     * Description of each filter chain:
     * - Apply desired scale and alpha to the watermark input.
     *   Note that because we have normalized the sar of the main video to `1` there is no more need to
     *   stretch or squeeze the watermark by multiplying its width by the input asr ratio (for exampel, `0.6875`)
     * - This step is like what was done for the intro image.
     *   Note: size coercion should be applied after the watermark scale is applied
     *   to coerce the dimensions of the **final scaled** watermark
     * - Place the watermark on the video in the desired position.
     *   Make sure to include the `format=yuv420p` so, Windows OS can show the video thumbnail and,
     *   more importantly, the output is playable in all players (specifically, Eitaa mobile player).
     *   See https://www.canva.dev/blog/engineering/a-journey-through-colour-space-with-ffmpeg/
     * - Name the final output `media` for subsequent steps to use
     */
    private fun coverFilterChains(options: CoverOptions, watermarkIndex: Int) = """
        [$watermarkIndex] format=rgba, colorchannelmixer=aa=${options.opacity}, scale=iw*${options.scale}:ih*${options.scale} [watermark];
        [watermark][media] scale2ref='if(gt(main_a,a),min(main_w,iw),min(main_h,ih)*main_a)':'if(gt(main_a,a),min(main_w,iw)/main_a,min(main_h,ih))' [watermark-max-size-coerced][media];
        [media][watermark-max-size-coerced] overlay=${options.position.ffmpegNotation}:format=auto, format=yuv420p [media-with-watermark];
        [media-with-watermark] null [media];
    """.takeIf { options.path != null } ?: ""

    /**
     * Description of the filter chain:
     * - Concatenate the intro (along with its silent audio) and the video (possibly watermarked).
     *   `n=2:v=1:a=1` means there are two parts to join and
     *   the result will have one video stream and one audio stream.
     *   Could add `:unsafe=1` to the end of `concat`, just in case,
     *   to prevent errors if dimensions/aspect ratios of intro and video did not match.
     */
    private fun finalFilterChains(hasIntro: Boolean) = when {
        hasIntro -> "[intro][2][media] concat=n=2:v=1:a=1"
        else     -> "[media] null"
    }

    /**
     * Description of each option:
     * - The encoder for the video stream. Run `./ffmpeg -encoders` for more
     * - The encoder for the audio stream. Run `./ffmpeg -encoders` for more
     * - The output constant quality (accepted values are `0..51`):
     *   + `0`: lossless
     *   + `23`: default
     *   + `51`: worst quality
     * - The output frame rate.
     *   Needed because the intro image and the rest of the video should have the same frame rate.
     *   Instead of `-r <number>` could use `-fps_mode vfr`.
     */
    private fun outputOptions(quality: Quality) = listOf(
        "-c:v" to "libx264",
        "-c:a" to "aac",
        "-crf" to qualityString(quality),
        "-r" to "$defaultOutputFrameRate"
    )

    private val WatermarkPosition.ffmpegNotation get() = when (this) {
        WatermarkPosition.TOP_LEFT      -> "0:0"
        WatermarkPosition.TOP_MIDDLE    -> "(W-w)/2:0"
        WatermarkPosition.TOP_RIGHT     -> "W-w:0"
        WatermarkPosition.CENTER_LEFT   -> "0:(H-h)/2"
        WatermarkPosition.CENTER        -> "(W-w)/2:(H-h)/2"
        WatermarkPosition.CENTER_RIGHT  -> "W-w:(H-h)/2"
        WatermarkPosition.BOTTOM_LEFT   -> "0:H-h"
        WatermarkPosition.BOTTOM_MIDDLE -> "(W-w)/2:H-h"
        WatermarkPosition.BOTTOM_RIGHT  -> "W-w:H-h"
    }

    private fun qualityString(quality: Quality) = when (quality) {
        Quality.LOWEST  -> "35"
        Quality.LOW     -> "29"
        Quality.MEDIUM  -> "23"
        Quality.HIGH    -> "17"
        Quality.HIGHEST -> "11"
    }
}
