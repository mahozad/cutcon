package ir.mahozad.cutcon.converter

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.model.*
import ir.mahozad.cutcon.substringBetween
import ir.mahozad.cutcon.toDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.bytedeco.ffmpeg.ffmpeg
import org.bytedeco.javacpp.Loader
import java.net.URL
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

typealias FFmpegOption = Pair<String, String>

fun interface ProgressListener {
    fun onProgressUpdate(progress: Float)
}

/**
 * See https://stackoverflow.com/q/52182827
 */
abstract class Converter(private val dispatcher: CoroutineDispatcher) {
    // Loads the FFmpeg executable appropriate for the current OS/architecture.
    // See the docs in README -> FFmpeg section and its subsections for more information.
    private val ffmpegPath = Loader.load(ffmpeg::class.java)
    private val millisecondRegex = Regex("""\.\d+""")
    private val logger = logger(name = Converter::class.simpleName ?: "")

    protected abstract fun ffmpegOptions(
        quality: Quality,
        introOptions: IntroOptions,
        coverOptions: CoverOptions,
        flags: ConverterFlags
    ): List<FFmpegOption>

    /**
     * The more functional and better way would be to return a result value
     * (such as [Result] or our own custom sealed class) but then the clients who call this function,
     * would have to handle two kinds of errors in two separate places:
     *  - Handle the exceptions in their catch clause or runCatching.onFailure
     *  - Handle the results denoting errors after calling this or in their runCatching.onSuccess
     *    (which mixes logic with results denoting success)
     *
     * For a quicker cancellation, increase the frequency of FFmpeg progress updates in [createCommand].
     *
     *  @throws [FFmpegProcessStartFailureException] if FFmpeg process failed to start
     *  @throws [FFmpegNonZeroExitCodeException] if FFmpeg return value was non-zero (abnormal)
     *  @throws [CancellationException] if the coroutine executing this function is canceled
     */
    suspend fun convert(
        input: URL,
        clip: Clip,
        intro: IntroOptions,
        cover: CoverOptions,
        quality: Quality,
        flags: ConverterFlags,
        output: Path,
        listener: ProgressListener
    ) = withContext(dispatcher) {
        val ffmpegOptions = ffmpegOptions(quality, intro, cover, flags)
        val ffmpegProcess = ProcessBuilder()
            .createCommand(input, clip, ffmpegOptions, output)
            .also { logger.info { "FFmpeg command:\n  ${it.command().joinToString(separator = "\n  ")}" } }
            .runCatching { start() }
            .onFailure { logger.error(it) { "Starting the FFmpeg process failed" } }
            .onSuccess { logger.info { "Started FFmpeg process with pid=${it.pid()}" } }
            .getOrElse { throw FFmpegProcessStartFailureException(it) }
        ffmpegProcess
            // By default, FFmpeg logs to stderr, so its errorStream is used
            .errorStream
            .reader()
            .useLines { lines ->
                lines
                    .asFlow()
                    .onEach { logger.info { it } }
                    .mapNotNull { parseLineAsProgress(it, clip.duration) }
                    .cancellable() // Could instead use .while { isActive OR ensureActive() } and remove asFlow()
                    .onCompletion {
                        logger.info { "Destroying FFmpeg process (pid=${ffmpegProcess?.pid()}) (alive=${ffmpegProcess?.isAlive})..." }
                        ffmpegProcess?.destroy() // This is also required to prevent FFmpeg leak on cancellation
                        ffmpegProcess?.onExit()?.join()
                        logger.info { "FFmpeg process destroyed successfully" }
                    }
                    .collect(listener::onProgressUpdate)
            }
        if (ffmpegProcess?.exitValue() != 0) {
            throw FFmpegNonZeroExitCodeException(ffmpegProcess.exitValue())
        }
    }

    private fun ProcessBuilder.createCommand(
        input: URL,
        clip: Clip,
        options: List<FFmpegOption>,
        output: Path
    ) = command(
        ffmpegPath,
        "-y", // Overwrites the output file if it exists
        "-nostdin", // Disables FFmpeg input (just in case)
        "-loglevel", "info", // -loglevel option is the same as -v
        "-stats_period", "0.1s", // The period between progress updates
        "-accurate_seek", // Enables accurate seeking in input files when using -ss
        // NOTE: The timestamps may not be exact due to VLC and vlcj progress emission inaccuracy
        //  Refer to MediaPlayer progress emitter function for more information.
        // NOTE: Use timestamps (-ss, -to, -t) before inputs (-i) to avoid unnecessary decoding
        //  For timestamp formats see https://ffmpeg.org/ffmpeg-utils.html#Time-duration
        "-ss", "${(clip.start - /* Makes end result more accurate */ 500.milliseconds).inWholeMilliseconds}ms",
        "-to", "${(clip.end - /* Makes end result more accurate */ 500.milliseconds).inWholeMilliseconds}ms", // Could use -t for duration
        "-i", input.toString().replace("file://localhost/", "file:"), // The main input (could be a file, stream, etc.)
        *options.flatMap(FFmpegOption::toList).toTypedArray(),
        // See https://wiki.multimedia.cx/index.php/FFmpeg_Metadata
        "-metadata", "encoding_tool=${BuildConfig.APP_NAME} v${BuildConfig.APP_VERSION}",
        "$output"
    )

    private fun parseLineAsProgress(line: String, totalDuration: Duration) = line
        .takeIf { "time=" in it }
        ?.substringBetween("time=", " bitrate=")
        ?.replace(millisecondRegex, "")
        ?.toDuration()
        ?.div(totalDuration)
        ?.toFloat()
        ?.coerceIn(0f..1f)
        ?.also { logger.debug { "Parsed FFmpeg progress $it" } }
}
