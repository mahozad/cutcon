package ir.mahozad.cutcon.model

import ir.mahozad.cutcon.localization.Language
import java.time.LocalDate

data class Changelog(val versions: List<ChangelogVersion>) {
    operator fun plus(newVersion: ChangelogVersion) = copy(versions = versions + newVersion)
    fun replaceLast(newVersion: ChangelogVersion) = copy(versions = versions.dropLast(1) + newVersion)
}

data class ChangelogVersion(val name: String, val date: LocalDate, val categories: List<ChangelogCategory>) {
    operator fun plus(newCategory: ChangelogCategory) = copy(categories = categories + newCategory)
}

data class ChangelogCategory(val type: CategoryType, val entries: List<ChangelogEntry>) {
    operator fun plus(newEntry: ChangelogEntry) = copy(entries = entries + newEntry)
}

data class ChangelogEntry(val items: List<String>) {
    operator fun plus(newItem: String) = copy(items = items + newItem)
}

enum class CategoryType(override val label: (Language) -> String) : Labeled {
    FEATURE(label = { it.messages.txtLblChangelogFeature }),
    BUGFIX(label = { it.messages.txtLblChangelogBugFix }),
    UPDATE(label = { it.messages.txtLblChangelogUpdate }),
    REMOVAL(label = { it.messages.txtLblChangelogRemoval }),
    INTERNAL(label = { it.messages.txtLblChangelogInternal })
}
