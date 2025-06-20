package ir.mahozad.cutcon

fun String?.toBooleanOrNull(): Boolean? {
    when (this?.trim()?.lowercase()) {
        "false" -> return false
        "true" -> return true
        else -> return null
    }
}

enum class OS { WINDOWS, LINUX, MAC, OTHER }

fun getCurrentOs(): OS {
    val osName = System
        .getProperty("os.name")
        .lowercase()
        .trim()
    return when {
        osName.startsWith("win") -> OS.WINDOWS
        osName.startsWith("mac") -> OS.MAC
        osName == "linux" -> OS.LINUX
        else -> OS.OTHER
    }
}
