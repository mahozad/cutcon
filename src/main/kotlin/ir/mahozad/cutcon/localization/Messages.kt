package ir.mahozad.cutcon.localization

import ir.mahozad.cutcon.model.Changelog
import kotlin.time.Duration

/**
 * Legend:
 * ICO = ICON
 * LBL = LABEL
 * TIT = TITLE
 * VAL = VALUE
 * DLG = DIALOG
 * BTN = BUTTON
 * TLP = TOOLTIP
 * DRP = DROPDOWN
 * DSC = DESCRIPTION
 * PLC = PLACEHOLDER
 */
sealed interface Messages {
    val changelog: Changelog
    val appName: String
    val versionPrefix: String
    val appVersion: String
    val error: String
    val openLogFolder: String
    val from: String
    val fromFile: String
    val clipCreationIsAbandonedIfExitTheApp: String
    val areYouSureToExitTheApp: String
    val yes: String
    val no: String
    val qualityNotApplicableToRawFormat: String
    val setClipStart: String
    val setClipEnd: String
    val seek5SecondsBackward: String
    val seek5SecondsForward: String
    val seek30SecondsBackward: String
    val seek30SecondsForward: String
    val livePlayback: String
    val takeScreenshotAndSaveIn: String
    val resumeMediaPlayback: String
    val pauseMediaPlayback: String
    val pinAppWindow: String
    val unPinAppWindow: String
    val muteMediaAudio: String
    val unMuteMediaAudio: String
    val restoreLastPlaybackSpeed: String
    val resetPlaybackSpeedToNormal: String
    val decreasePlaybackSpeed: String
    val increasePlaybackSpeed: String
    val switchToFullscreen: String
    val switchToMiniMode: String
    val switchToNormalMode: String
    val switchToManualDateInput: String
    val openSaveFolder: String
    val openSourceFolder: String
    val switchToSelectionDateInput: String
    val hideSidePanel: String
    val showSidePanel: String
    val turnOnClipLoop: String
    val turnOffClipLoop: String
    val shortcut: String
    val and: String
    val weekdaySaturday: String
    val weekdaySunday: String
    val weekdayMonday: String
    val weekdayTuesday: String
    val weekdayWednesday: String
    val weekdayThursday: String
    val weekdayFriday: String
    val cut: String
    val copy: String
    val paste: String
    val selectAll: String
    val dlgTitSpecifySaveFile: String
    val dlgTitSelectLocalFile: String
    val dlgTitSelectIntroImage: String
    val dlgTitSelectWatermark: String
    val dlgTitSelectAlbumArt: String
    val dlgTitExistingFile: String
    val dlgTitMissingFile: String
    val dlgTitChangelog: String
    val second: String
    val btnLblOk: String
    val btnLblOpen: String
    val btnLblCancel: String
    val btnLblSelectSaveFolder: String
    val btnLblApproveSaveFile: String
    val btnLblShowChangelog: String
    val btnLblApproveSelectedFile: String
    val btnLblStartConversion: String
    val btnLblCancelConversion: String
    val btnLblOpenAppLogFolder: String
    val btnTlpOpenFileChooser: String
    val btnTlpCancelFileChooser: String
    val btnTlpCancelFileSave: String
    val btnTlpApproveSaveFolder: String
    val btnTlpApproveSelectedFile: String
    val btnTlpUpFolder: String
    val btnTlpViewMenu: String
    val btnTlpNewFolder: String
    val txtLblSourceLocal: String
    val txtLblToday: String
    val txtLblYesterday: String
    val txtLblPercentSign: String
    val txtLblSaveIn: String
    val txtLblLookIn: String
    val txtLblClipLength: String
    val txtLblInput: String
    val txtLblOutput: String
    val txtLblQuality: String
    val txtLblQuality1: String
    val txtLblQuality2: String
    val txtLblQuality3: String
    val txtLblQuality4: String
    val txtLblQuality5: String
    val txtLblSelectWatermark: String
    val txtLblSelectIntroImage: String
    val txtLblSelectAlbumArt: String
    val txtLblDragFileHere: String
    val txtLblLanguage: String
    val txtLblCalendar: String
    val txtLblTheme: String
    val txtLblAspectRatio: String
    val txtLblFinishSound: String
    val txtLblScreenshotSound: String
    val txtLblInterlacedFix: String
    val txtLblEnabled: String
    val txtLblDisabled: String
    val txtLblChangelogFeature: String
    val txtLblChangelogBugFix: String
    val txtLblChangelogUpdate: String
    val txtLblChangelogRemoval: String
    val txtLblChangelogInternal: String
    val txtLblExistingFile: String
    val txtLblMissingFile: String
    val txtLblFileName: String
    val txtLblFileType: String
    val txtLblFileTypeAll: String
    val txtLblFileFormat: String
    val txtLblAspectRatioSource: String
    val txtLblAspectRatio16To9: String
    val txtLblCalendarGregorian: String
    val txtLblCalendarSolarHijri: String
    val txtLblThemeLight: String
    val txtLblThemeDark: String
    val radLblFormatMp4: String
    val radLblFormatMp3: String
    val radLblFormatRaw: String
    val radLblLanguagePersian: String
    val radLblLanguageEnglish: String
    val txtLblClipCreationSuccess: String
    val txtLblClipCreationFailure: String
    val txtDscScreenshotHelp: String
    val txtLblLocalFileSupportedTypesDescription: String
    val txtLblIntroSupportedTypesDescription: String
    val txtLblCoverSupportedTypesDescription: String
    val txtLblHasNoDefault: String
    val txtLblHasDefault: String
    val txtLblAlpha: String
    val txtLblScale: String
    val txtLblMp3IntroNotSupported: String
    val txtLblRawIntroNotSupported: String
    val txtLblRawCoverNotSupported: String
    val txtLblAudioFileIntroNotSupported: String
    val txtLblImageFileIntroNotSupported: String
    val txtLblMiscFileIntroNotSupported: String
    val txtLblAudioFileWatermarkNotSupported: String
    val txtLblImageFileWatermarkNotSupported: String
    val txtLblMiscFileWatermarkNotSupported: String
    val txtLblErrorClipNotSet: String
    val txtLblErrorClipLengthZero: String
    val txtLblErrorClipLengthNegative: String
    val txtLblErrorClipStartAfterMediaEnd: String
    val txtLblErrorClipFromImageNotSupported: String
    val txtLblErrorClipFromFormatNotSupported: String
    val txtLblErrorClipFileNotSet: String
    val txtLblReady: String
    val txtLblProgressInitializing: String
    val txtLblProgressCreating: String
    val txtLblAboutDeveloper: String
    val txtLblAboutPoweredBy: String
    val txtLblAboutKotlinLabel: String
    val txtLblAboutKotlinText: String
    val txtLblAboutGradleLabel: String
    val txtLblAboutGradleText: String
    val txtLblAboutJetpackComposeLabel: String
    val txtLblAboutJetpackComposeText: String
    val txtLblAboutComposeMultiplatformLabel: String
    val txtLblAboutComposeMultiplatformText: String
    val txtLblAboutVlcLabel: String
    val txtLblAboutVlcText: String
    val txtLblAboutFfmpegLabel: String
    val txtLblAboutFfmpegText: String
    val txtLblAboutMaterialDesignLabel: String
    val txtLblAboutMaterialDesignText: String
    val txtLblAboutInkscapeLabel: String
    val txtLblAboutInkscapeText: String
    val txtLblAboutIntellijLabel: String
    val txtLblAboutIntellijText: String
    val txtLblAboutGitLabel: String
    val txtLblAboutGitText: String
    val txtLblAboutGitHubLabel: String
    val txtLblAboutGitHubText: String
    val txtLblAboutVazirmatnLabel: String
    val txtLblAboutVazirmatnText: String

    fun totalClipCreationTime(duration: Duration): String

    companion object {
        const val ERR_COMPOSE_RES_DIR_NOT_SET = """
            JVM property 'compose.application.resources.dir'
            which specifies the directory containing application custom assets hasn't been set.
            See https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution#packaging-resources
        """
        const val ICO_DSC_SUCCESS = "Success"
        const val ICO_DSC_FAILURE = "Failure"
        const val ICO_DSC_SOFTWARE_LOGO = "Software logo"
        const val ICO_DSC_MAHOZAD_LOGO = "Mahdi Hosseinzadeh (Mahozad) logo"
        const val ICO_DSC_OPEN_FOLDER = "Open the folder"
        const val ICO_DSC_ENTER_FULLSCREEN = "Enter fullscreen"
        const val ICO_DSC_ENTER_MINI_SCREEN = "Enter mini screen"
        const val ICO_DSC_ENTER_REGULAR_SCREEN = "Enter regular screen"
        const val ICO_DSC_EXIT_FULLSCREEN = "Exit fullscreen"
        const val ICO_DSC_PLAY_FILE = "Start playing the file"
        const val ICO_DSC_PLAY_PAUSE = "Play/Pause"
        const val ICO_DSC_AUDIO_VOLUME = "Audio volume"
        const val ICO_DSC_TOGGLE_ALWAYS_ON_TOP = "Toggle always on top"
        const val ICO_DSC_TOGGLE_CLIP_LOOP = "Toggle clip loop"
        const val ICO_DSC_TOGGLE_SIDE_PANEL = "Toggle side panel"
        const val ICO_DSC_CHANGELOG_CATEGORY = "Changelog category"
        const val ICO_DSC_CHANGELOG_ENTRY = "Changelog entry"
        const val ICO_DSC_TITLE_BAR_ICON = "Title bar icon"
        const val ICO_DSC_MINIMIZE = "Minimize"
        const val ICO_DSC_CLOSE = "Close"
        const val ICO_DSC_PANEL_ABOUT = "About panel"
        const val ICO_DSC_PANEL_CONFIG = "Config panel"
        const val ICO_DSC_PANEL_SETTINGS = "Settings panel"
        const val IMG_DSC_DISPLAY_IMAGE = "Display image"
        const val ICO_DSC_SETTINGS_LANGUAGE = "Language settings"
        const val ICO_DSC_SETTINGS_THEME = "Theme settings"
        const val ICO_DSC_SETTINGS_CALENDAR = "Calendar settings"
        const val ICO_DSC_SETTINGS_ASPECT_RATIO = "Display image aspect ratio settings"
        const val ICO_DSC_SETTINGS_FINISH_SOUND = "Success sound settings"
        const val ICO_DSC_SETTINGS_SCREENSHOT_SOUND = "Screenshot sound settings"
        const val ICO_DSC_SETTINGS_INTERLACED_FIX = "Interlaced settings"
        const val ICO_DSC_OPEN_DROPDOWN = "Open dropdown"
        const val ICO_DSC_RESET_SPEED = "Reset playback speed"
        const val ICO_DSC_DECREASE_SPEED = "Decrease playback speed"
        const val ICO_DSC_INCREASE_SPEED = "Increase playback speed"
        const val ICO_DSC_SET_CLIP_START_NOW = "Set clip start to now"
        const val ICO_DSC_SET_CLIP_END_NOW = "Set clip end to now"
        const val ICO_DSC_REMOVE_INTRO = "Remove intro"
        const val ICO_DSC_REMOVE_COVER = "Remove cover"
        const val ICO_DSC_INTRO_PREVIEW = "Intro preview"
        const val ICO_DSC_COVER_PREVIEW = "Cover preview"
        const val ICO_DSC_ADD_INTRO = "Add intro"
        const val ICO_DSC_ADD_COVER = "Add cover"
        const val ICO_DSC_TAKE_SCREENSHOT = "Take screenshot"
        const val ICO_DSC_LOGO = "App logo"
        const val ICO_DSC_REWIND_5_SECONDS = "Rewind 5 seconds"
        const val ICO_DSC_REWIND_30_SECONDS = "Rewind 30 seconds"
        const val ICO_DSC_FORWARD_5_SECONDS = "Forward 5 seconds"
        const val ICO_DSC_FORWARD_30_SECONDS = "Forward 30 seconds"
    }
}
