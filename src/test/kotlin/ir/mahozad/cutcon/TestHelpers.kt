package ir.mahozad.cutcon

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import io.mockk.spyk
import ir.mahozad.cutcon.component.DateTimeChecker
import ir.mahozad.cutcon.component.MediaPlayer
import ir.mahozad.cutcon.component.UrlMaker
import ir.mahozad.cutcon.converter.ConverterFactory
import ir.mahozad.cutcon.model.Clip
import ir.mahozad.cutcon.model.MediaInfo
import ir.mahozad.cutcon.model.Progress
import ir.mahozad.cutcon.model.Speed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import org.bytedeco.ffmpeg.ffmpeg
import org.bytedeco.javacpp.Loader
import java.io.File
import java.net.URL
import java.nio.file.Path
import java.util.prefs.Preferences
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.toPath
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val testTimeout = 30.seconds

private val resourceAccessor = object {}
private val ffmpegPath = Loader.load(ffmpeg::class.java)

fun constructMainViewModel(
    dispatcher: TestDispatcher,
    converterFactory: ConverterFactory = spyk(),
    dateTimeChecker: DateTimeChecker = spyk(),
    mediaPlayer: MediaPlayer = spyk(),
    settings: Preferences = spyk(),
    urlMaker: UrlMaker = spyk(),
) = MainViewModel(
    dispatcher = dispatcher,
    converterFactory = converterFactory,
    dateTimeChecker = dateTimeChecker,
    mediaPlayer = mediaPlayer,
    settings = settings,
    urlMaker = urlMaker,
    saveFileNameGenerator = spyk()
)

fun constructMediaInfo(
    speed: Speed = defaultSpeed,
    progress: Progress = Progress(fraction = 0f, length = Duration.ZERO),
    isResumed: Boolean = defaultIsResumed,
    clipToLoop: Clip? = defaultClipToLoop,
    audioVolume: Float = defaultAudioVolume,
    isAudioMuted: Boolean = defaultIsAudioMuted
) = MediaInfo(
    url = defaultMediaUrl,
    speed = speed,
    progress = progress,
    isResumed = isResumed,
    clipToLoop = clipToLoop,
    audioVolume = audioVolume,
    isAudioMuted = isAudioMuted
)

class FakeMediaPlayer(private val mediaDuration: Duration) : MediaPlayer {
    override val video = flowOf(null)
    override val progress = MutableStateFlow(Progress(0f, mediaDuration))

    override fun seek(value: Float) {
        progress.value = Progress(value, mediaDuration)
    }

    override fun play(url: URL) {}
    override fun pause() {}
    override fun resume() {}
    override fun toggleResume() {}
    override fun setSpeed(value: Float) {}
    override fun setClipToLoop(clip: Clip?) {}
    override fun setAudioVolume(value: Float) {}
    override fun mute() {}
    override fun unMute() {}
    override fun terminate() {}
    override fun takeScreenshot(saveDirectory: File) = true
    override fun setFinishListener(listener: () -> Unit) {}
}

fun ImageBitmap?.getPixels(): IntArray? {
    if (this == null) return null
    return toAwtImage().data.getPixels(0, 0, width - 2, height, null as IntArray?)
}

fun getResourceAsPath(name: String): Path = resourceAccessor
    .javaClass
    .getResource("/$name")!!
    .toURI()
    .toPath()

/**
 * Could not use `val input = javaClass.getResource("/test.ts")` because,
 * although the created URL is valid, FFmpeg throws error as
 * it does not parse all variations of `file:...` URL syntax correctly.
 *
 * See https://trac.ffmpeg.org/ticket/2702
 * and https://trac.ffmpeg.org/ticket/9157
 */
fun getResourceAsURL(name: String): URL {
    val testResourcesDirectory = Path("src/test/resources").absolutePathString()
    return URL("file:$testResourcesDirectory/$name")
}

fun extractFrame(from: Path, time: String, into: Path) {
    ProcessBuilder()
        .command(
            ffmpegPath,
            "-i", from.toString(),
            "-ss", time,
            "-frames", "1",
            "-filter:v",
            "scale=100:-1",
            into.toString()
        )
        .start()
        ?.errorStream
        ?.reader()
        ?.forEachLine { println("Test output: $it") }
}
