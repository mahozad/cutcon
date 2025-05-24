package ir.mahozad.cutcon.component

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.toTwoDigit
import java.nio.file.Path
import java.time.LocalTime
import java.time.chrono.ChronoLocalDate
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

fun interface SaveFileNameGenerator {
    fun generate(
        directory: Path,
        date: ChronoLocalDate,
        time: LocalTime
    ): String
}

object DefaultSaveFileNameGenerator : SaveFileNameGenerator {

    private val logger = logger(name = javaClass.simpleName)

    override fun generate(
        directory: Path,
        date: ChronoLocalDate,
        time: LocalTime
    ): String {
        val name = "${date}_${time.hour.toTwoDigit()}-00"
        val maxNumber = maxExistingFileNumber(directory, name)
        val result = "${name}_${maxNumber + 1}"
        logger.info { "Generated save file name $result" }
        return result
    }

    private fun maxExistingFileNumber(
        directory: Path,
        name: String
    ) = directory
        .takeIf(Path::exists)
        ?.listDirectoryEntries(glob = "$name*")
        ?.map(Path::nameWithoutExtension)
        ?.map { it.substringAfter("${name}_") }
        ?.mapNotNull(String::toIntOrNull)
        ?.maxOrNull()
        ?: 0
}
