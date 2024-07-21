package ir.mahozad.cutcon.converter

import ir.mahozad.cutcon.model.CoverOptions
import ir.mahozad.cutcon.model.IntroOptions
import ir.mahozad.cutcon.model.Quality
import kotlinx.coroutines.CoroutineDispatcher

class RawConverter(dispatcher: CoroutineDispatcher) : Converter(dispatcher) {
    override fun ffmpegOptions(
        quality: Quality,
        introOptions: IntroOptions,
        coverOptions: CoverOptions,
        flags: ConverterFlags
    ): List<FFmpegOption> = listOf(
        "-c" to "copy" // Preserves the encoding (== original/source encoding)
    )
}
