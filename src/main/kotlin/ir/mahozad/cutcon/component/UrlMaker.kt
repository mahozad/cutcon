package ir.mahozad.cutcon.component

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.model.Source
import java.net.URL
import kotlin.io.path.absolutePathString

fun interface UrlMaker {
    fun makeUrl(source: Source): URL
}

object DefaultUrlMaker : UrlMaker {

    private val logger = logger(name = javaClass.simpleName)

    override fun makeUrl(source: Source) = when (source) {
        is Source.Local -> {
            // For playing local files use a URL with "localhost" like this:
            // file://localhost/C:/Users/Name/Downloads/video.mp4
            // Otherwise, the media playback stutters when using a URL like this:
            // file://C:/Users/Name/Downloads/video.mp4
            URL("file://localhost/${source.path.absolutePathString()}")
        }
    }.also {
        logger.info { "Generated url $it" }
    }
}
