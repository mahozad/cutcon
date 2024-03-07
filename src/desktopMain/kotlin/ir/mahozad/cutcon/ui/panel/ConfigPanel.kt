package ir.mahozad.cutcon.ui.panel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.LocalLanguage
import ir.mahozad.cutcon.MainViewModel
import ir.mahozad.cutcon.component.DefaultDateTimeChecker
import ir.mahozad.cutcon.component.DefaultMediaPlayer
import ir.mahozad.cutcon.component.DefaultSaveFileNameGenerator
import ir.mahozad.cutcon.component.DefaultUrlMaker
import ir.mahozad.cutcon.converter.DefaultConverterFactory
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.localization.LanguageEn
import ir.mahozad.cutcon.localization.LanguageFa
import ir.mahozad.cutcon.model.Quality
import ir.mahozad.cutcon.model.Status.*
import ir.mahozad.cutcon.ui.theme.AppTheme
import ir.mahozad.cutcon.ui.widget.*
import kotlinx.coroutines.Dispatchers
import java.util.prefs.Preferences

@Preview
@Composable
private fun ConfigPanelPreviewFa() {
    val fakeWindowScope = object : FrameWindowScope {
        override val window get() = ComposeWindow()
    }
    CompositionLocalProvider(LocalLanguage provides LanguageFa) {
        AppTheme {
            fakeWindowScope.ConfigPanel(
                MainViewModel(
                    dispatcher = Dispatchers.Main,
                    urlMaker = DefaultUrlMaker,
                    mediaPlayer = DefaultMediaPlayer(),
                    dateTimeChecker = DefaultDateTimeChecker(Dispatchers.Main),
                    converterFactory = DefaultConverterFactory(Dispatchers.Main),
                    saveFileNameGenerator = DefaultSaveFileNameGenerator,
                    settings = Preferences.userRoot().node("/${BuildConfig.APP_NAME}/preview")!!
                ).apply { setLanguage(LanguageFa) }
            )
        }
    }
}

@Preview
@Composable
private fun ConfigPanelPreviewEn() {
    val fakeWindowScope = object : FrameWindowScope {
        override val window get() = ComposeWindow()
    }
    CompositionLocalProvider(LocalLanguage provides LanguageEn) {
        AppTheme {
            fakeWindowScope.ConfigPanel(
                MainViewModel(
                    dispatcher = Dispatchers.Main,
                    urlMaker = DefaultUrlMaker,
                    mediaPlayer = DefaultMediaPlayer(),
                    dateTimeChecker = DefaultDateTimeChecker(Dispatchers.Main),
                    converterFactory = DefaultConverterFactory(Dispatchers.Main),
                    saveFileNameGenerator = DefaultSaveFileNameGenerator,
                    settings = Preferences.userRoot().node("/${BuildConfig.APP_NAME}/preview")!!
                ).apply { setLanguage(LanguageEn) }
            )
        }
    }
}

@Composable
fun FrameWindowScope.ConfigPanel(viewModel: MainViewModel) {
    // See https://stackoverflow.com/a/71182514
    val language = LocalLanguage.current
    val source by viewModel.source.collectAsState()
    val status by viewModel.status.collectAsState()
    val format by viewModel.format.collectAsState()
    val quality by viewModel.quality.collectAsState()
    val saveFile by viewModel.saveFile.collectAsState()
    val coverBitmap by viewModel.coverBitmap.collectAsState()
    val coverOptions by viewModel.coverOptions.collectAsState()
    val introBitmap by viewModel.introBitmap.collectAsState()
    val introOptions by viewModel.introOptions.collectAsState()
    val lastOpenDirectory by viewModel.lastOpenDirectory.collectAsState()
    val lastSaveDirectory by viewModel.lastSaveDirectory.collectAsState()
    val isQualityInputApplicable by viewModel.isQualityInputApplicable.collectAsState()
    val isInputEnabled by remember { derivedStateOf { status !is Initializing && status !is InProgress } }
    Column(modifier = Modifier.fillMaxHeight()) {
        Spacer(Modifier.height(8.dp))
        LabeledDivider(label = LocalLanguage.current.messages.txtLblInput)
        Spacer(Modifier.height(8.dp))
        SourceInput(
            source = source,
            lastOpenDirectory = lastOpenDirectory,
            onSetSourceToLocalRequest = viewModel::setSourceToLocal
        )
        Spacer(Modifier.height(16.dp))
        LabeledDivider(label = LocalLanguage.current.messages.txtLblOutput)
        Spacer(Modifier.height(8.dp))
        FormatInput(
            isEnabled = isInputEnabled,
            format = format,
            onChange = viewModel::setFormat
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IntroInput(
                source = source,
                isEnabled = isInputEnabled,
                image = introBitmap,
                targetFormat = format,
                options = introOptions,
                lastOpenDirectory = lastOpenDirectory,
                modifier = Modifier.width(118.dp),
                onFileChange = viewModel::setIntroFile,
                onDurationChange = viewModel::setIntroDuration,
                onBackgroundColorChange = viewModel::setIntroBackgroundColor
            )
            CoverInput(
                source = source,
                isEnabled = isInputEnabled,
                image = coverBitmap,
                targetFormat = format,
                options = coverOptions,
                lastOpenDirectory = lastOpenDirectory,
                modifier = Modifier.weight(1f),
                onFileChange = viewModel::setCoverFile,
                onScaleChange = viewModel::setWaterMarkScale,
                onOpacityChange = viewModel::setWaterMarkOpacity,
                onPositionChange = viewModel::setWatermarkPosition
            )
        }
        Spacer(Modifier.height(16.dp))
        LabeledDivider(label = LocalLanguage.current.messages.txtLblQuality)
        Spacer(Modifier.height(2.dp))
        QualityInput(
            min = Quality.LOWEST.value,
            max = Quality.HIGHEST.value,
            quality = quality,
            isEnabled = isInputEnabled,
            isApplicable = isQualityInputApplicable,
            onChange = viewModel::setQuality
        )
        SaveAsInput(
            isEnabled = isInputEnabled,
            source = source,
            destination = saveFile,
            targetFormat = format,
            lastSaveDirectory = lastSaveDirectory,
            defaultNameProvider = viewModel::generateSaveFileDefaultName,
            onFileSpecified = viewModel::setSaveFile
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = if (status is Ready) viewModel::startProcess else viewModel::cancelProcess,
            enabled = status is Ready || status is InProgress
        ) {
            Text(
                if (status is Initializing || status is InProgress) {
                    language.messages.btnLblCancelConversion
                } else {
                    language.messages.btnLblStartConversion
                }
            )
        }
        Spacer(Modifier.height(7.dp))
        StatusLabel(status)
    }
}

@Composable
private fun LabeledDivider(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(16.dp)
    ) {
        // Divider(modifier = Modifier.weight(1f))
        Text(text = label, fontSize = defaultFontSize)
        Divider(modifier = Modifier.weight(1f))
    }
}
