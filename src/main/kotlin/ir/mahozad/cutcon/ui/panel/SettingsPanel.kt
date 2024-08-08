package ir.mahozad.cutcon.ui.panel

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahozad.cutcon.*
import ir.mahozad.cutcon.localization.Language
import ir.mahozad.cutcon.localization.LanguageEn
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.localization.Messages.Companion.ICO_DSC_SETTINGS_ASPECT_RATIO
import ir.mahozad.cutcon.model.Labeled
import ir.mahozad.cutcon.model.Toggle
import ir.mahozad.cutcon.ui.icon.*
import ir.mahozad.cutcon.ui.widget.RadioGroup

private enum class LanguageEnum : Labeled {
    PERSIAN {
        override val label: (Language) -> String = {
            it.messages.radLblLanguagePersian
        }
    },
    ENGLISH {
        override val label: (Language) -> String = {
            it.messages.radLblLanguageEnglish
        }
    }
}

@Composable
fun SettingsPanel(viewModel: MainViewModel) {
    Column {
        val language = LocalLanguage.current
        val scope = rememberCoroutineScope()
        val theme by viewModel.theme.collectAsState()
        val calendar by viewModel.calendar.collectAsState()
        val aspectRatio by viewModel.aspectRatio.collectAsState()
        val isFinishSoundEnabled by viewModel.isFinishSoundEnabled.collectAsState()
        val isScreenshotSoundEnabled by viewModel.isScreenshotSoundEnabled.collectAsState()
        val isInterlacedFixEnabled by viewModel.isInterlacedFixEnabled.collectAsState()
        // Overrides the font for when the language is not Persian (to show the Persian words with this font)
        Spacer(Modifier.height(8.dp))
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = LanguageFa.fontFamily)
        ) {
            Settings(
                value = if (language is LanguageFa) {
                    LanguageEnum.PERSIAN
                } else {
                    LanguageEnum.ENGLISH
                },
                title = language.messages.txtLblLanguage,
                icon = Icons.Custom.Glob,
                iconDescription = Messages.ICO_DSC_SETTINGS_LANGUAGE,
                onChange = {
                    viewModel.setLanguage(
                        if (it == LanguageEnum.PERSIAN) {
                            LanguageFa
                        } else {
                            LanguageEn
                        }
                    )
                }
            )
        }
        Settings(
            value = theme,
            title = language.messages.txtLblTheme,
            icon = Icons.Custom.Theme,
            iconDescription = Messages.ICO_DSC_SETTINGS_THEME,
            onChange = viewModel::setTheme
        )
        Settings(
            value = calendar,
            title = language.messages.txtLblCalendar,
            icon = Icons.Custom.Date,
            iconDescription = Messages.ICO_DSC_SETTINGS_CALENDAR,
            onChange = viewModel::setCalendar
        )
        Settings(
            value = aspectRatio,
            title = language.messages.txtLblAspectRatio,
            icon = Icons.Custom.AspectRatio,
            iconDescription = ICO_DSC_SETTINGS_ASPECT_RATIO,
            onChange = viewModel::setAspectRatio
        )
        Settings(
            value = if (isInterlacedFixEnabled) Toggle.ENABLED else Toggle.DISABLED,
            title = language.messages.txtLblInterlacedFix,
            icon = Icons.Custom.Interlaced,
            iconDescription = Messages.ICO_DSC_SETTINGS_INTERLACED_FIX,
            onChange = { viewModel.setIsInterlacedFixEnabled(it == Toggle.ENABLED) }
        )
        Settings(
            value = if (isScreenshotSoundEnabled) Toggle.ENABLED else Toggle.DISABLED,
            title = language.messages.txtLblScreenshotSound,
            icon = Icons.Custom.Shutter,
            iconDescription = Messages.ICO_DSC_SETTINGS_SCREENSHOT_SOUND,
            onChange = { viewModel.setIsScreenshotSoundEnabled(it == Toggle.ENABLED) }
        )
        Settings(
            value = if (isFinishSoundEnabled) Toggle.ENABLED else Toggle.DISABLED,
            title = language.messages.txtLblFinishSound,
            icon = Icons.Custom.Notification,
            iconDescription = Messages.ICO_DSC_SETTINGS_FINISH_SOUND,
            onChange = { viewModel.setIsFinishSoundEnabled(it == Toggle.ENABLED) }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = scope::openAppLogFolder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = language.messages.btnLblOpenAppLogFolder, fontSize = defaultFontSize)
        }
    }
}

@Composable
private fun <T> Settings(
    value: T,
    title: String,
    icon: ImageVector,
    iconDescription: String,
    onChange: (T) -> Unit
) where T : Enum<T>, T : Labeled {
    val language = LocalLanguage.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            modifier = Modifier.size(defaultIconSize)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = (defaultFontSize.value - 1).sp,
            modifier = Modifier.width(if (language == LanguageFa) 96.dp else 96.dp)
        )
        RadioGroup(
            value = value,
            isEnabled = true,
            modifier = Modifier.fillMaxWidth(),
            weights = { 1f },
            onChange = onChange
        )
    }
}
