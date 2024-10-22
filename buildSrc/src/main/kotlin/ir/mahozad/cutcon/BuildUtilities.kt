package ir.mahozad.cutcon

fun String?.toBooleanOrNull(): Boolean? {
    when (this?.trim()?.lowercase()) {
        "false" -> return false
        "true" -> return true
        else -> return null
    }
}
