package ir.mahozad.cutcon.ui.panel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.LocalCalendar
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.generated.resources.Res
import ir.mahozad.cutcon.generated.resources.compose_logo
import ir.mahozad.cutcon.generated.resources.compose_multiplatform_logo
import ir.mahozad.cutcon.generated.resources.ffmpeg_logo
import ir.mahozad.cutcon.generated.resources.git_logo
import ir.mahozad.cutcon.generated.resources.github_logo
import ir.mahozad.cutcon.generated.resources.gradle_logo
import ir.mahozad.cutcon.generated.resources.inkscape_logo
import ir.mahozad.cutcon.generated.resources.intellij_logo
import ir.mahozad.cutcon.generated.resources.kotlin_logo
import ir.mahozad.cutcon.generated.resources.mahozad
import ir.mahozad.cutcon.generated.resources.material_you_logo
import ir.mahozad.cutcon.generated.resources.open_source
import ir.mahozad.cutcon.generated.resources.vazir_logo
import ir.mahozad.cutcon.generated.resources.vlc_logo
import ir.mahozad.cutcon.localization.LanguageEn
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.ui.theme.AppTheme

private val entryFontSize = (defaultFontSize.value - 1).sp
private val entryIconSize = 18.dp

@Preview
@Composable
private fun AboutPanelPreviewFa() {
    CompositionLocalProvider(LocalLanguage provides LanguageFa) {
        AppTheme {
            AboutPanel()
        }
    }
}

@Preview
@Composable
private fun AboutPanelPreviewEn() {
    CompositionLocalProvider(LocalLanguage provides LanguageEn) {
        AppTheme {
            AboutPanel()
        }
    }
}

@Composable
fun AboutPanel() {
    Column(verticalArrangement = spacedBy(4.dp)) {
        AppGeneralInfo()
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        PoweredByEntries()
    }
}

@Composable
private fun AppGeneralInfo() {
    Spacer(Modifier.height(8.dp))
    LogoNameVersionDate()
    Divider(modifier = Modifier.padding(vertical = 8.dp))
    DeveloperEntry()
}

@Composable
private fun LogoNameVersionDate() {
    val language = LocalLanguage.current
    val calendar = LocalCalendar.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource("logo.svg"),
            contentDescription = Messages.ICO_DSC_LOGO,
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.width(8.dp))
        // Row is needed to be able to use Modifier.alignByBaseLine()
        Row {
            Text(
                text = language.messages.appName,
                fontSize = 16.sp,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = language.messages.versionPrefix,
                fontSize = defaultFontSize,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(Modifier.width(if (language is LanguageFa) 3.dp else 0.dp))
            Text(
                text = language.localizeDigits(BuildConfig.APP_VERSION),
                fontSize = defaultFontSize,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "(${calendar.format(BuildConfig.APP_RELEASE_DATE, language)})",
                fontSize = defaultFontSize,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}

@Composable
private fun DeveloperEntry() {
    val language = LocalLanguage.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(Res.drawable.mahozad),
            contentDescription = Messages.ICO_DSC_MAHOZAD_LOGO,
            modifier = Modifier.size(entryIconSize)
        )
        Spacer(Modifier.width(4.dp))
        val developerString = buildAnnotatedString {
            append("${language.messages.txtLblAboutDeveloper} (")
            withLink(
                LinkAnnotation.Url(
                    url = "https://mahozad.ir",
                    styles = TextLinkStyles(SpanStyle(color = MaterialTheme.colors.primary))
                )
            ) {
                append("https://mahozad.ir")
            }
            append(")")
        }
        Text(
            text = developerString,
            fontSize = entryFontSize
        )
    }
}

@Composable
private fun PoweredByEntries() {
    val language = LocalLanguage.current
    AboutEntry(icon = Res.drawable.open_source, normalText = language.messages.txtLblAboutPoweredBy)
    AboutEntry(
        icon = Res.drawable.kotlin_logo,
        link = "https://kotlinlang.org",
        linkText = language.messages.txtLblAboutKotlinLabel,
        normalText = language.messages.txtLblAboutKotlinText
    )
    AboutEntry(
        icon = Res.drawable.gradle_logo,
        link = "https://gradle.org",
        linkText = language.messages.txtLblAboutGradleLabel,
        normalText = language.messages.txtLblAboutGradleText
    )
    AboutEntry(
        icon = Res.drawable.compose_logo,
        link = "https://developer.android.com/jetpack/compose",
        linkText = language.messages.txtLblAboutJetpackComposeLabel,
        normalText = language.messages.txtLblAboutJetpackComposeText
    )
    AboutEntry(
        icon = Res.drawable.compose_multiplatform_logo,
        link = "https://jetbrains.com/lp/compose-multiplatform",
        linkText = language.messages.txtLblAboutComposeMultiplatformLabel,
        normalText = language.messages.txtLblAboutComposeMultiplatformText
    )
    AboutEntry(
        icon = Res.drawable.vlc_logo,
        link = "https://videolan.org/vlc/libvlc.html",
        linkText = language.messages.txtLblAboutVlcLabel,
        normalText = language.messages.txtLblAboutVlcText
    )
    AboutEntry(
        icon = Res.drawable.ffmpeg_logo,
        link = "https://github.com/bytedeco/javacv",
        linkText = language.messages.txtLblAboutFfmpegLabel,
        normalText = language.messages.txtLblAboutFfmpegText
    )
    AboutEntry(
        icon = Res.drawable.material_you_logo,
        link = "https://m3.material.io",
        linkText = language.messages.txtLblAboutMaterialDesignLabel,
        normalText = language.messages.txtLblAboutMaterialDesignText
    )
    AboutEntry(
        icon = Res.drawable.inkscape_logo,
        link = "https://inkscape.org",
        linkText = language.messages.txtLblAboutInkscapeLabel,
        normalText = language.messages.txtLblAboutInkscapeText
    )
    AboutEntry(
        icon = Res.drawable.intellij_logo,
        link = "https://www.jetbrains.com/idea",
        linkText = language.messages.txtLblAboutIntellijLabel,
        normalText = language.messages.txtLblAboutIntellijText
    )
    AboutEntry(
        icon = Res.drawable.git_logo,
        link = "https://git-scm.com",
        linkText = language.messages.txtLblAboutGitLabel,
        normalText = language.messages.txtLblAboutGitText
    )
    AboutEntry(
        icon = Res.drawable.github_logo,
        link = "https://github.com",
        linkText = language.messages.txtLblAboutGitHubLabel,
        normalText = language.messages.txtLblAboutGitHubText
    )
    AboutEntry(
        icon = Res.drawable.vazir_logo,
        link = "https://github.com/rastikerdar/vazirmatn",
        linkText = language.messages.txtLblAboutVazirmatnLabel,
        normalText = language.messages.txtLblAboutVazirmatnText
    )
}

@Composable
private fun AboutEntry(
    icon: DrawableResource,
    link: String? = null,
    linkText: String? = null,
    normalText: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(20.dp)
    ) {
        Image(
            painter = painterResource(icon),
            modifier = Modifier.size(entryIconSize),
            contentDescription = Messages.ICO_DSC_SOFTWARE_LOGO
        )
        Spacer(Modifier.width(4.dp))
        val primaryColor = MaterialTheme.colors.primary
        val annotatedString = buildAnnotatedString {
            if (link != null) {
                withLink(
                    LinkAnnotation.Url(
                        url = link,
                        styles = TextLinkStyles(SpanStyle(color = primaryColor))
                    )
                ) {
                    append(linkText)
                }
            }
            append(normalText)
        }
        Text(
            text = annotatedString,
            fontSize = entryFontSize
        )
    }
}
