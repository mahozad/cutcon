package ir.mahozad.cutcon.ui.dialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahozad.cutcon.LocalCalendar
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.defaultIconSize
import ir.mahozad.cutcon.localization.LanguageEn
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.*
import ir.mahozad.cutcon.ui.icon.*
import ir.mahozad.cutcon.ui.theme.AppTheme
import ir.mahozad.cutcon.ui.theme.borderColor

@Preview
@Composable
private fun ChangelogDialogPreviewFa() {
    CompositionLocalProvider(LocalLanguage provides LanguageFa) {
        AppTheme {
            ChangelogDialog(
                changelog = LanguageFa.messages.changelog,
                onCloseRequest = {}
            )
        }
    }
}

@Preview
@Composable
private fun ChangelogDialogPreviewEn() {
    CompositionLocalProvider(LocalLanguage provides LanguageEn) {
        AppTheme {
            ChangelogDialog(
                changelog = LanguageEn.messages.changelog,
                onCloseRequest = {}
            )
        }
    }
}

@Composable
fun ChangelogDialog(changelog: Changelog, onCloseRequest: () -> Unit) {
    AnimatedDialog(
        title = LocalLanguage.current.messages.dlgTitChangelog,
        modifier = Modifier.size(424.dp, 380.dp),
        onDismissRequest = onCloseRequest
    ) {
        Box(modifier = Modifier.padding(bottom = 16.dp, start = 8.dp, end = 8.dp)) {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(
                    key = { changelog.versions[it].name },
                    count = changelog.versions.size,
                    itemContent = { ChangelogVersion(changelog.versions[it]) }
                )
            }
            VerticalScrollbar(
                style = LocalScrollbarStyle.current.copy(minimalHeight = 64.dp),
                adapter = rememberScrollbarAdapter(scrollState = state),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ChangelogVersion(version: ChangelogVersion) {
    val language = LocalLanguage.current
    val calendar = LocalCalendar.current
    val borderColor = borderColor.copy(alpha = 0.2f)
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Divider(color = borderColor, modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(1.dp, borderColor, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = language.messages.versionPrefix, fontSize = 14.sp)
                Spacer(Modifier.width(if (language.messages.versionPrefix.length > 1) 4.dp else 0.dp))
                Text(text = language.localizeDigits(version.name), fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "(${calendar.format(version.date, language)})",
                    fontSize = 14.sp
                )
            }
            Divider(color = borderColor, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            version.categories.forEach {
                ChangelogCategory(it)
            }
        }
    }
}

@Composable
private fun ChangelogCategory(category: ChangelogCategory) {
    val language = LocalLanguage.current
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = when (category.type) {
                    CategoryType.FEATURE -> Icons.Custom.Star
                    CategoryType.BUGFIX -> Icons.Custom.Bug
                    CategoryType.UPDATE -> Icons.Custom.Wrench
                    CategoryType.REMOVAL -> Icons.Custom.Clean
                    CategoryType.INTERNAL -> Icons.Custom.Commit
                },
                modifier = Modifier.size(defaultIconSize),
                contentDescription = Messages.ICO_DSC_CHANGELOG_CATEGORY
            )
            Spacer(Modifier.width(2.dp))
            Text(text = category.type.label(language), fontSize = 14.sp, color = MaterialTheme.colors.primary)
        }
        Spacer(Modifier.height(2.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            category.entries.forEach {
                ChangelogEntry(it)
            }
        }
    }
}

@Composable
private fun ChangelogEntry(entry: ChangelogEntry) {
    Row(
        // Uses top alignment so the bullet is shown properly for multiline entries as well
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Custom.Bullet,
            modifier = Modifier.padding(top = if (LocalLanguage.current is LanguageFa) 5.dp else 4.dp).size(5.dp),
            contentDescription = Messages.ICO_DSC_CHANGELOG_ENTRY
        )
        Spacer(Modifier.width(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            entry.items.forEach {
                Text(text = it, fontSize = (defaultFontSize.value - 1).sp)
            }
        }
    }
}
