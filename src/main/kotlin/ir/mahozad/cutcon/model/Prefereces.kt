package ir.mahozad.cutcon.model

import ir.mahozad.cutcon.BuildConfig

object PreferenceKeys {
    // The app name has been added as prefix because other apps with same keys will conflict with these
    const val PREF_THEME = "${BuildConfig.APP_NAME}-theme"
    const val PREF_LANGUAGE = "${BuildConfig.APP_NAME}-language"
    const val PREF_CALENDAR = "${BuildConfig.APP_NAME}-calendar"
    const val PREF_ASPECT_RATIO = "${BuildConfig.APP_NAME}-aspect-ratio"
    const val PREF_FINISH_SOUND = "${BuildConfig.APP_NAME}-finish-sound"
    const val PREF_INTERLACED_FIX = "${BuildConfig.APP_NAME}-interlaced-fix"
    const val PREF_SCREENSHOT_SOUND = "${BuildConfig.APP_NAME}-screenshot-sound"
    const val PREF_LAST_OPEN_DIRECTORY = "${BuildConfig.APP_NAME}-last-open-directory"
    const val PREF_LAST_SAVE_DIRECTORY = "${BuildConfig.APP_NAME}-last-save-directory"
    const val PREF_LAST_SHOWN_CHANGELOG_VERSION = "${BuildConfig.APP_NAME}-last-shown-changelog-version"
}
