package ir.mahozad.cutcon.component

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.assetsPath
import ir.mahozad.cutcon.decodeImage
import ir.mahozad.cutcon.defaultAudioVolume
import ir.mahozad.cutcon.defaultIsAudioMuted
import ir.mahozad.cutcon.detectMimeType
import ir.mahozad.cutcon.model.Clip
import ir.mahozad.cutcon.model.Progress
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.jaudiotagger.audio.AudioFileIO
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.base.State
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.ByteBuffer
import java.nio.file.Path
import javax.swing.SwingUtilities
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import uk.co.caprica.vlcj.factory.MediaPlayerFactory as VlcMediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer as VlcMediaPlayer

interface MediaPlayer {

    val output: Flow<Output>
    val progress: Flow<Progress>
    val isResumed get() = /* For tests: */ flowOf(false)

    sealed interface Output {
        data object SourceNotStarted : Output
        data object SourceHasNoImage : Output
        @JvmInline value class Image(val image: ImageBitmap) : Output
        @JvmInline value class Video(val video: Flow<ImageBitmap?>) : Output
    }

    fun play(url: URL)
    fun seek(value: Float)
    fun pause()
    fun resume()
    fun toggleResume()
    fun mute()
    fun unMute()
    fun setAudioVolume(value: Float)
    fun setSpeed(value: Float)
    fun setClipToLoop(clip: Clip?)
    fun takeScreenshot(saveDirectory: File): Boolean
    fun setFinishListener(listener: (MediaPlayer) -> Unit)
    fun terminate()
}

// See https://github.com/JetBrains/compose-multiplatform/pull/3336
// and https://github.com/caprica/vlcj/issues/1098
class DefaultMediaPlayer : MediaPlayer {

    private val logger = logger(MediaPlayer::class.simpleName ?: "")
    private val vlcPath = (assetsPath / BuildConfig.VLC_DIRECTORY_NAME).absolutePathString()
    // Run vlc -H or vlc --help --advanced or add -H or --help and --advanced options
    // separately below to see all vlc configurations and capabilities in the standard output.
    private val vlcOptions = listOf(
        // Does not have any effect; just in case
        "--video-title=${BuildConfig.APP_NAME} video output",
        // Name of screenshot files (plus date and time)
        "--snapshot-prefix=${BuildConfig.APP_NAME.lowercase()}-",
        // Format of screenshot {png, jpg, tiff}
        "--snapshot-format=png",
        // Makes the process priority high
        "--high-priority",
        // Disables collection of statistics
        "--no-stats",
        // Shows verbose output {0 error and info, 1 warning, 2 debug}
        "--verbose", "1",
        // Sets user interface to none
        "--intf=dummy",
        // Avoids showing a dialog box when user input is required
        "--no-interact",
        // Allows hardware (GPU) decoding when available {any, d3d11va, dxva2, none}
        "--avcodec-hw=any",
        // Greatly improves the startup time of VLC
        "--plugins-cache",
        // Drops frames instead of showing visual (gray) artifacts
        "--no-avcodec-corrupted"
    )

    private var clipToLoop: Clip? = null
    private var finishListener: ((MediaPlayer) -> Unit)? = null
    private val vlcMediaPlayerFactory = initializeVlcMediaPlayerFactory()
    private val videoSurface = SkiaBitmapVideoSurface()
    private val eventListener = EventListener()
    private val vlcMediaPlayer = vlcMediaPlayerFactory
        .mediaPlayers()
        .newEmbeddedMediaPlayer()
        .apply { videoSurface().set(videoSurface) }
        .apply { events().addMediaPlayerEventListener(eventListener) }

    /**
     * Instead of [StateFlow], [SharedFlow] with `replay = 2` is used to prevent conflation of emissions and
     * to retain at least 2 last emissions (so that new consumers, or, the single main consumer, gets the startup values).
     *
     * The `onBufferOverflow` should be set to something other than [BufferOverflow.SUSPEND] so that
     * the [MutableSharedFlow.tryEmit] calls won't drop the new value on fast emissions.
     * This caused a bug when going live from a date and time different from now because by clicking the live button,
     * the date and time were updated separately and the [MediaPlayer.play] would be called for each of their new value
     * separately very fast and this sometimes caused the [MutableSharedFlow.tryEmit] to drop the latest output
     * and thus the output remained the last video which by now would be closed and empty.
     */
    override val output = MutableSharedFlow<MediaPlayer.Output>(
        replay = 2, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val progress = vlcMediaPlayer.progressFlow()
    override val isResumed = vlcMediaPlayer.isResumedFlow()

    init {
        /**
         * TODO: Use --no-volume-save option in the factory below
         *  if newer versions of VLC reset the audio muteness as well.
         * VLC has a feature that starts with the last audio volume/muteness set in its previous launch
         * (can explicitly control this by passing --volume-save and --no-volume-save options).
         * The --no-volume-save option does not reset the muteness of audio
         * (meaning, if we mute the audio and restart the app, the app will start with muted sound)
         * so, instead of passing this option, here we set the initial audio volume/muteness manually.
         */
        vlcMediaPlayer.audio().isMute = defaultIsAudioMuted
        vlcMediaPlayer.audio().setVolume(defaultAudioVolume.toPercentage())
    }

    private fun initializeVlcMediaPlayerFactory(): VlcMediaPlayerFactory {
        // See main README -> Embedding VLC DLL files section for more information
        val discovery = NativeDiscovery(object : NativeDiscoveryStrategy {
            override fun discover() = vlcPath
            override fun supported() = true
            override fun onFound(path: String) = true
            override fun onSetPluginPath(path: String) = true
        })
        // The default args are MediaPlayerComponentDefaults.EMBEDDED_MEDIA_PLAYER_ARGS
        // To see how to get the list of all possible VLC options, see comments on [vlcOptions].
        return VlcMediaPlayerFactory(discovery, vlcOptions)
    }

    /**
     * Polls and emits media progress every `n` milliseconds.
     * Note that it seems vlcj updates the progress only every 250 milliseconds or so.
     *
     * Instead of polling, could also have used event listener like below,
     * but it reintroduced bugs when seeking the paused media:
     * ```kotlin
     * DisposableEffect(listener) {
     *     val playerListener = object : MediaPlayerEventAdapter() {
     *         override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
     *             val fraction = mediaPlayer.status().position()
     *             val length = mediaPlayer.status().length().milliseconds
     *             onProgress(Progress(fraction, newTime.milliseconds, length))
     *         }
     *     }
     *     events().addMediaPlayerEventListener(playerListener)
     *     onDispose { events().removeMediaPlayerEventListener(playerListener) }
     * }
     * ```
     *
     * See https://stackoverflow.com/a/43426974
     */
    private fun VlcMediaPlayer.progressFlow() = flow {
        while (true) {
            val fraction = status().position()
            val length = status().length().milliseconds
            // Because when the media is finished the length becomes negative
            if (length >= ZERO) emit(Progress(fraction, length))
            // Higher delay is better which also fixes problem with progress bar;
            // See the progress bar widget code for more information
            delay(250.milliseconds)
        }
    }

    /**
     * Could have instead created a [MutableStateFlow] and update it in the proper event listener callback below.
     */
    private fun VlcMediaPlayer.isResumedFlow() = flow {
        while (true) {
            emit(status().isPlaying)
            delay(50.milliseconds)
        }
    }

    private fun Float.toPercentage() = (this * 100).roundToInt()

    override fun play(url: URL) {
        vlcMediaPlayer.controls().stop()
        videoSurface.stopAndResetVideo()
        output.tryEmit(MediaPlayer.Output.SourceNotStarted)

        val uri = URI("file", url.host, url.path, null)
        val path = Path(url.path.drop(1))
        val mimeType = path.detectMimeType()
        val urlString = uri.toASCIIString() // Encodes the path if needed

        if (mimeType == "image/gif") {
            output.tryEmit(MediaPlayer.Output.Video(videoSurface.imageFlow))
        } else if (mimeType?.startsWith("video") == true) {
            output.tryEmit(MediaPlayer.Output.Video(videoSurface.imageFlow))
        } else if (mimeType?.startsWith("audio") == true) {
            output.tryEmit(generateOutputForAudio(path))
        } else if (mimeType?.startsWith("image") == true) {
            output.tryEmit(generateOutputForImage(path))
        } else {
            output.tryEmit(MediaPlayer.Output.SourceHasNoImage)
        }

        // Uses play() instead of start() because play() is non-blocking (asynchronous)
        vlcMediaPlayer.media().play(urlString)
    }

    private fun generateOutputForImage(path: Path): MediaPlayer.Output {
        return decodeImage(path)
            ?.let(MediaPlayer.Output::Image)
            ?: MediaPlayer.Output.SourceHasNoImage
    }

    private fun generateOutputForAudio(path: Path): MediaPlayer.Output {
        return path
            .toFile()
            .runCatching(AudioFileIO::read)
            .getOrNull()
            ?.tag
            ?.firstArtwork
            ?.binaryData
            ?.inputStream()
            ?.use(::loadImageBitmap)
            ?.let(MediaPlayer.Output::Image)
            ?: MediaPlayer.Output.SourceHasNoImage
    }

    override fun pause() {
        vlcMediaPlayer.controls().setPause(true)
    }

    override fun resume() {
        // Because if the VLC is in stopped state then setting pause or resume will have no effect
        // This sometimes happens in the player so this checking is just in case
        if (vlcMediaPlayer.status().state() == State.STOPPED) {
            vlcMediaPlayer.submit {
                // Should be called in submit to prevent occasional java.lang.Error: Invalid memory access
                vlcMediaPlayer.media().play(vlcMediaPlayer.media().info().mrl())
            }
        } else {
            vlcMediaPlayer.controls().setPause(false)
        }
    }

    override fun toggleResume() {
        if (vlcMediaPlayer.status().isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    override fun setAudioVolume(value: Float) {
        vlcMediaPlayer.audio().setVolume(value.toPercentage())
    }

    override fun setSpeed(value: Float) {
        vlcMediaPlayer.controls().setRate(value)
    }

    override fun mute() {
        vlcMediaPlayer.audio().isMute = true
    }

    override fun unMute() {
        vlcMediaPlayer.audio().isMute = false
    }

    override fun seek(value: Float) {
        vlcMediaPlayer.controls().setPosition(value)
    }

    override fun setClipToLoop(clip: Clip?) {
        clipToLoop = clip
    }

    override fun takeScreenshot(saveDirectory: File): Boolean {
        return vlcMediaPlayer.snapshots().save(saveDirectory)
    }

    override fun terminate() {
        vlcMediaPlayer.events().removeMediaPlayerEventListener(eventListener)
        vlcMediaPlayer.release()
        vlcMediaPlayerFactory.release()
    }

    override fun setFinishListener(listener: (MediaPlayer) -> Unit) {
        finishListener = listener
    }

    private inner class EventListener : MediaPlayerEventAdapter() {

        override fun stopped(vlcMediaPlayer: VlcMediaPlayer) {
            logger.info { "Media finished" }
            // See https://github.com/JetBrains/compose-multiplatform/pull/4048
            vlcMediaPlayer.submit { finishListener?.invoke(this@DefaultMediaPlayer) }
        }

        /**
         * Handles looping the clip if it is set.
         * Note that it seems vlcj updates the progress only every 250 milliseconds or so.
         *
         * For looping the clip, instead of setting start and stop time options in the play method above
         * we set them manually here in a listener because of two reasons:
         *   - The start time option does not work for TV source (issue: https://code.videolan.org/videolan/vlc/-/issues/28227)
         *   - When the loop is set to null the play is called again and thus the media starts over (instead of continuing)
         *
         * For previous implementation of looping the clip that used stop time option in the play method above,
         * checkout the v1.4.0 git tag.
         */
        override fun timeChanged(vlcMediaPlayer: VlcMediaPlayer, newTime: Long) {
            // See https://github.com/JetBrains/compose-multiplatform/pull/4048
            vlcMediaPlayer.submit {
                val (start, end) = clipToLoop ?: return@submit
                val startPosition = start.inWholeMilliseconds / vlcMediaPlayer.status().length().toFloat()
                if (newTime < (start - 500.milliseconds).inWholeMilliseconds) {
                    vlcMediaPlayer.controls().setPosition(startPosition)
                } else if (newTime >= end.inWholeMilliseconds) {
                    vlcMediaPlayer.controls().setPosition(startPosition)
                }
            }
        }
    }
}

private class SkiaBitmapVideoSurface : VideoSurface(VideoSurfaceAdapters.getVideoSurfaceAdapter()) {

    private lateinit var imageInfo: ImageInfo
    private lateinit var frameBytes: ByteArray
    private val skiaBitmap = Bitmap()
    private val videoSurface = SkiaBitmapVideoSurface()

    /**
     * Instead of [StateFlow] or [SharedFlow], a [Channel] is used so that
     * the flow can be indicated finished (closed) for consumers of the flow.
     */
    private var bitmap = Channel<ImageBitmap?>(capacity = Channel.CONFLATED)

    /**
     * Uses [receiveAsFlow] instead of [consumeAsFlow] to prevent rare exceptions.
     */
    val imageFlow get() = bitmap.receiveAsFlow()

    fun stopAndResetVideo() {
        bitmap.close()
        bitmap = Channel(capacity = Channel.CONFLATED)
    }

    override fun attach(mediaPlayer: VlcMediaPlayer) {
        videoSurface.attach(mediaPlayer)
    }

    private inner class SkiaBitmapBufferFormatCallback : BufferFormatCallback {
        private var sourceWidth: Int = 0
        private var sourceHeight: Int = 0

        override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
            this.sourceWidth = sourceWidth
            this.sourceHeight = sourceHeight
            return RV32BufferFormat(sourceWidth, sourceHeight)
        }

        override fun allocatedBuffers(buffers: Array<ByteBuffer>) {
            frameBytes = buffers[0].run { ByteArray(remaining()).also(::get) }
            imageInfo = ImageInfo(
                sourceWidth,
                sourceHeight,
                ColorType.BGRA_8888,
                ColorAlphaType.PREMUL
            )
        }
    }

    private inner class SkiaBitmapRenderCallback : RenderCallback {
        override fun display(
            mediaPlayer: VlcMediaPlayer,
            nativeBuffers: Array<ByteBuffer>,
            bufferFormat: BufferFormat,
        ) {
            SwingUtilities.invokeLater {
                nativeBuffers[0].rewind()
                nativeBuffers[0].get(frameBytes)
                skiaBitmap.installPixels(imageInfo, frameBytes, bufferFormat.width * 4)
                // Takes less than 1 millisecond
                bitmap.trySend(skiaBitmap.asComposeImageBitmap())
            }
        }
    }

    private inner class SkiaBitmapVideoSurface : CallbackVideoSurface(
        SkiaBitmapBufferFormatCallback(),
        SkiaBitmapRenderCallback(),
        true,
        videoSurfaceAdapter
    )
}
