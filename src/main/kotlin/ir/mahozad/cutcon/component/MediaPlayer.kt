package ir.mahozad.cutcon.component

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.assetsPath
import ir.mahozad.cutcon.defaultAudioVolume
import ir.mahozad.cutcon.defaultIsAudioMuted
import ir.mahozad.cutcon.model.Clip
import ir.mahozad.cutcon.model.Progress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.io.File
import java.net.URL
import java.nio.ByteBuffer
import javax.swing.SwingUtilities
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import uk.co.caprica.vlcj.factory.MediaPlayerFactory as VlcMediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer as VlcMediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter as VlcMediaPlayerEventAdapter

interface MediaPlayer {
    val video: Flow<ImageBitmap?>
    val progress: Flow<Progress>

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
    fun setFinishListener(listener: () -> Unit)
    fun terminate()
}

// See https://github.com/JetBrains/compose-multiplatform/pull/3336
// and https://github.com/caprica/vlcj/issues/1098
class DefaultMediaPlayer : MediaPlayer {

    private val logger = logger(MediaPlayer::class.simpleName ?: "")
    private val vlcPath = (assetsPath / BuildConfig.VLC_DIRECTORY_NAME).absolutePathString()
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
        // Allows hardware decoding when available {any, d3d11va, dxva2, none}
        "--avcodec-hw=any",
        // Greatly improves the startup time of VLC
        "--plugins-cache",
        // Drops frames instead of showing visual (gray) artifacts
        "--no-avcodec-corrupted"
    )

    private var vlcMediaPlayerFactory = initializeVlcMediaPlayerFactory()
    private var clipToLoop: Clip? = null
    private var isResumed = true
    private var finishListener: (() -> Unit)? = null
    private val videoSurface = SkiaBitmapVideoSurface()
    private val eventListener = EventListener()
    private var vlcMediaPlayer = vlcMediaPlayerFactory
        .mediaPlayers()
        .newEmbeddedMediaPlayer()
        .apply { videoSurface().set(videoSurface) }
        .apply { events().addMediaPlayerEventListener(eventListener) }

    override val video = videoSurface.bitmap
    override val progress = vlcMediaPlayer.progressFlow()

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
        // Run vlc -H or vlc --help --advanced or add -H or --help and --advanced options
        // separately to vlcOptions below to see all vlc configurations and capabilities
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
            emit(Progress(fraction, length))
            // Higher delay is better which also fixes problem with progress bar;
            // See the progress bar widget code for more information
            delay(250.milliseconds)
        }
    }

    private fun Float.toPercentage() = (this * 100).roundToInt()

    override fun play(url: URL) {
        // Sets null to clear the last frame of previous media
        videoSurface.bitmap.value = null
        isResumed = true
        vlcMediaPlayer.media().play /* OR .start */(url.toString())
    }

    override fun pause() {
        isResumed = false
        vlcMediaPlayer.controls().setPause(true)
    }

    override fun resume() {
        isResumed = true
        vlcMediaPlayer.controls().setPause(false)
    }

    override fun toggleResume() {
        isResumed = !isResumed
        if (isResumed) resume() else pause()
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

    override fun setFinishListener(listener: () -> Unit) {
        finishListener = listener
    }

    private inner class EventListener : VlcMediaPlayerEventAdapter() {
        // Using vlcMediaPlayer.status().length() didn't work
        var mediaLength = 0L

        override fun lengthChanged(vlcMediaPlayer: VlcMediaPlayer, newLength: Long) {
            mediaLength = newLength
        }

        /**
         * Handles media finish.
         *
         * We play the media on finish (so the player is kind of idempotent),
         * unless the [finishListener] callback stops the playback.
         * Using `vlcMediaPlayer.controls().repeat = true` did not work as expected.
         */
        override fun stopped(vlcMediaPlayer: VlcMediaPlayer) {
            // finishListener?.invoke()
            // Restarts the media only if it is longer than 1 seconds;
            // This is mostly for when the file is an image to prevent
            // this callback which is called very often to consume CPU
            // and to prevent logging too many statements in the logger
            if (mediaLength > 1_000) {
                logger.info { "Media finished; starting it over" }
                vlcMediaPlayer.controls().play()
            }
        }

        /**
         * Handles looping the clip if it is set.
         * Note that it seems vlcj updates the progress only every 250 milliseconds or so.
         *
         * For looping the clip, instead of setting start and stop time options in the play method above
         * we set them manually here in a listener because when the loop is set to null
         * the play is called again and thus the media starts over (instead of continuing).
         *
         * For previous implementation of looping the clip that used stop time option in the play method above,
         * checkout the v1.4.0 git tag.
         */
        override fun timeChanged(vlcMediaPlayer: VlcMediaPlayer, newTime: Long) {
            val (start, end) = clipToLoop.takeIf { it != null } ?: return
            val startPosition = start.inWholeMilliseconds / vlcMediaPlayer.status().length().toFloat()
            if (newTime < (start - 500.milliseconds).inWholeMilliseconds) {
                vlcMediaPlayer.controls().setPosition(startPosition)
            } else if (newTime >= end.inWholeMilliseconds) {
                vlcMediaPlayer.controls().setPosition(startPosition)
            }
        }
    }
}

private class SkiaBitmapVideoSurface : VideoSurface(VideoSurfaceAdapters.getVideoSurfaceAdapter()) {

    private lateinit var imageInfo: ImageInfo
    private lateinit var frameBytes: ByteArray
    private val skiaBitmap = Bitmap()
    private val videoSurface = SkiaBitmapVideoSurface()
    val bitmap = MutableStateFlow<ImageBitmap?>(null)

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
                bitmap.value = skiaBitmap.asComposeImageBitmap()
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
