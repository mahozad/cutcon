package ir.mahozad.cutcon.converter

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.model.Format
import kotlinx.coroutines.CoroutineDispatcher

private val logger = logger(name = ConverterFactory::class.simpleName ?: "")

fun interface ConverterFactory {
    fun createFor(format: Format): Converter
}

class DefaultConverterFactory(private val dispatcher: CoroutineDispatcher) : ConverterFactory {
    override fun createFor(format: Format) = when (format) {
        Format.MP3 -> Mp3Converter(dispatcher)
        Format.MP4 -> Mp4Converter(dispatcher)
        Format.RAW -> RawConverter(dispatcher)
    }.also {
        logger.info { "Selected ${it::class.simpleName} for format $format" }
    }
}
