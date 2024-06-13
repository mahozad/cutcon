package ir.mahozad.cutcon.localization

import ir.mahozad.cutcon.BuildConfig
import ir.mahozad.cutcon.model.LocalSourceSupportedFileType
import ir.mahozad.cutcon.model.SupportedImageFormat
import ir.mahozad.cutcon.parseMarkdownAsChangelog
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data object MessagesFa : Messages {
    override val changelog by lazy {
        // See the build script where the changelog file was added as an app resource
        val stream = javaClass.getResourceAsStream("/CHANGELOG.md")!!
        parseMarkdownAsChangelog(stream, LanguageFa.tag)
    }

    override val appName = "کات‌کن"
    override val versionPrefix = "ویرایش"
    override val appVersion = "$versionPrefix ${LanguageFa.localizeDigits(BuildConfig.APP_VERSION)}"
    override val error = "خطایی در برنامه رخ داد.\nبرای جزئیات بیشتر، لاگ برنامه را مشاهده کنید."
    override val openLogFolder = "باز کردن پوشه لاگ"
    override val from = "از"
    override val fromFile = "از پرونده"
    override val clipCreationIsAbandonedIfExitTheApp = "در صورت خروج از برنامه، ایجاد کلیپ نیمه‌کاره لغو می‌شود."
    override val areYouSureToExitTheApp = "از برنامه خارج می‌شوید؟"
    override val yes = "بله"
    override val no = "خیر"
    override val qualityNotApplicableToRawFormat = "کیفیت اصلی (کیفیت خروجی خام را نمی‌توان تغییر داد)"
    override val setClipStart = "تعیین شروع کلیپ"
    override val setClipEnd = "تعیین پایان کلیپ"
    override val seek5SecondsBackward = "رفتن به ۵ ثانیه قبل"
    override val seek5SecondsForward = "رفتن به ۵ ثانیه بعد"
    override val seek30SecondsBackward = "رفتن به ۳۰ ثانیه قبل"
    override val seek30SecondsForward = "رفتن به ۳۰ ثانیه بعد"
    override val livePlayback = "پخش زنده"
    override val takeScreenshotAndSaveIn = "گرفتن عکس (S) و ذخیره در:"
    override val resumeMediaPlayback = "ادامه پخش"
    override val pauseMediaPlayback = "توقف پخش"
    override val pinAppWindow = "نمایش روی تمام پنجره‌ها (سنجاق کردن)"
    override val unPinAppWindow = "از سنجاق در آوردن برنامه"
    override val muteMediaAudio = "قطع صدا"
    override val unMuteMediaAudio = "پخش صدا"
    override val restoreLastPlaybackSpeed = "تنظیم سرعت پخش به حالت قبل"
    override val resetPlaybackSpeedToNormal = "بازنشانی سرعت پخش به عادی"
    override val decreasePlaybackSpeed = "کاهش سرعت پخش"
    override val increasePlaybackSpeed = "افزایش سرعت پخش"
    override val switchToFullscreen = "نمایش در حالت تمام‌صفحه"
    override val switchToMiniMode = "نمایش در حالت کوچک"
    override val switchToNormalMode = "نمایش در حالت عادی"
    override val switchToManualDateInput = "تغییر به حالت دستی"
    override val switchToSelectionDateInput = "تغییر به حالت انتخابی"
    override val openSourceFolder = "باز کردن پوشه منبع"
    override val openSaveFolder = "باز کردن بوشه ذخیره"
    override val hideSidePanel = "مخفی کردن پنل کناری"
    override val showSidePanel = "نمایش پنل کناری"
    override val turnOnClipLoop = "روشن کردن تکرار کلیپ"
    override val turnOffClipLoop = "خاموش کردن تکرار کلیپ"
    override val shortcut = "میانبر:"
    override val and = "و"
    override val weekdaySaturday = "شنبه"
    override val weekdaySunday = "یکشنبه"
    override val weekdayMonday = "دوشنبه"
    override val weekdayTuesday ="سه‌شنبه"
    override val weekdayWednesday = "چهارشنبه"
    override val weekdayThursday = "پنجشنبه"
    override val weekdayFriday = "جمعه"
    override val cut = "برش"
    override val copy = "رونوشت"
    override val paste = "چسباندن"
    override val selectAll = "انتخاب همه"
    override val dlgTitSpecifySaveFile = "ذخیره کلیپ به عنوان..."
    override val dlgTitSelectLocalFile = "انتخاب پرونده"
    override val dlgTitSelectIntroImage = "انتخاب عکس شروع"
    override val dlgTitSelectWatermark = "انتخاب واترمارک"
    override val dlgTitSelectAlbumArt = "انتخاب آلبوم‌آرت"
    override val dlgTitExistingFile = "پرونده موجود"
    override val dlgTitMissingFile = "پرونده ناموجود"
    override val dlgTitChangelog = "تغییرات"
    override val second = "ث"
    override val btnLblOk = "تایید"
    override val btnLblOpen = "باز کردن"
    override val btnLblCancel = "لغو"
    override val btnLblSelectSaveFolder = "ذخیره به عنوان..."
    override val btnLblApproveSaveFile = "تایید"
    override val btnLblShowChangelog = "تغییرات"
    override val btnLblApproveSelectedFile = "انتخاب"
    override val btnLblStartConversion = "شروع"
    override val btnLblCancelConversion = "لغو"
    override val btnLblOpenAppLogFolder = "باز کردن پوشه لاگ برنامه"
    override val btnTlpOpenFileChooser = "باز کردن پوشه"
    override val btnTlpCancelFileChooser = "صرف نظر از انتخاب پرونده"
    override val btnTlpCancelFileSave = "صرف نظر از تعیین پرونده"
    override val btnTlpApproveSaveFolder = "انتخاب به عنوان پرونده ذخیره کلیپ"
    override val btnTlpApproveSelectedFile = "انتخاب پرونده"
    override val btnTlpUpFolder = "پوشه بالا"
    override val btnTlpViewMenu = "منوی چینش"
    override val btnTlpNewFolder = "ایجاد پوشه جدید"
    override val txtLblSourceLocal = "منبع"
    override val txtLblToday = "(امروز)"
    override val txtLblYesterday = "(دیروز)"
    override val txtLblPercentSign = "٪"
    override val txtLblSaveIn = ":پوشه"
    override val txtLblLookIn = ":پوشه"
    override val txtLblClipLength = "طول کلیپ:"
    override val txtLblInput = "ورودی"
    override val txtLblOutput = "خروجی"
    override val txtLblQuality = "کیفیت"
    override val txtLblQuality1 = "۱"
    override val txtLblQuality2 = "۲"
    override val txtLblQuality3 = "۳"
    override val txtLblQuality4 = "۴"
    override val txtLblQuality5 = "۵"
    override val txtLblSelectWatermark = "واترمارک"
    override val txtLblSelectIntroImage = "عکس شروع"
    override val txtLblSelectAlbumArt = "آلبوم‌آرت"
    override val txtLblDragFileHere = "می‌توان کشید اینجا"
    override val txtLblLanguage = "زبان (Language):"
    override val txtLblCalendar = "گاه‌شماری:"
    override val txtLblTheme = "پوسته:"
    override val txtLblAspectRatio = "نسبت تصویر:"
    override val txtLblFinishSound = "صدای موفقیت:"
    override val txtLblScreenshotSound = "صدای دوربین:"
    // Other possible labels:
    // رفع لرزش کلیپ | ادغام فیلدها | ادغام دوبه‌دو فیلد | پردازش تصویر | تبدیل فیلد به فریم | ادغام فیلد تصویر | رفع همبافتگی
    override val txtLblInterlacedFix = "ادغام فیلد تصویر:"
    override val txtLblEnabled = "فعال"
    override val txtLblDisabled = "غیرفعال"
    override val txtLblChangelogFeature = "قابلیت‌های جدید"
    override val txtLblChangelogBugFix = "مشکلات رفع شده"
    override val txtLblChangelogUpdate = "بروزرسانی‌ها"
    override val txtLblChangelogRemoval = "حذف شده‌ها"
    override val txtLblChangelogInternal = "تغییرات درونی"
    override val txtLblExistingFile = "پرونده از قبل وجود دارد. جایگزین شود؟"
    override val txtLblMissingFile = "پرونده وجود ندارد"
    override val txtLblFileName = ":نام پرونده"
    override val txtLblFileType = ":نوع پرونده"
    override val txtLblFileTypeAll = "تمام پرونده‌ها"
    override val txtLblFileFormat = ":فرمت پرونده"
    override val txtLblAspectRatioSource = "اصلی"
    override val txtLblAspectRatio16To9 = "۱۶:۹"
    override val txtLblCalendarGregorian = "میلادی"
    override val txtLblCalendarSolarHijri = "خورشیدی" // OR more accurately: هجری خورشیدی
    override val txtLblThemeLight = "روشن"
    override val txtLblThemeDark = "تیره"
    override val radLblFormatMp4 = "MP4"
    override val radLblFormatMp3 = "MP3"
    override val radLblFormatRaw = "خام (اصلی)"
    override val radLblLanguagePersian = "فارسی"
    override val radLblLanguageEnglish = "English"
    override val txtLblClipCreationSuccess = "کلیپ با موفقیت ایجاد شد"
    override val txtLblClipCreationFailure = "ایجاد کلیپ ناموفق بود"
    override val txtDscScreenshotHelp = "روی دکمه زده و نگه دارید تا پوشه باز شود"
    override val txtLblLocalFileSupportedTypesDescription = "\u202Aرسانه (${LocalSourceSupportedFileType.entries.joinToString { it.name }})"
    override val txtLblIntroSupportedTypesDescription = "\u202Aعکس (${SupportedImageFormat.entries.joinToString { it.name }})"
    override val txtLblCoverSupportedTypesDescription = "\u202Aعکس (${SupportedImageFormat.entries.joinToString { it.name }})"
    override val txtLblHasNoDefault = "(بدون پیش‌فرض)"
    override val txtLblHasDefault = "(دارای پیش‌فرض)"
    override val txtLblAlpha = "آلفا:"
    override val txtLblScale = "اندازه:"
    override val txtLblMp3IntroNotSupported = "خروجی MP3 از عکس شروع پشتیبانی نمی‌کند"
    override val txtLblRawIntroNotSupported = "خروجی خام از عکس شروع پشتیبانی نمی‌کند"
    override val txtLblRawCoverNotSupported = "خروجی خام از واترمارک یا آلبوم‌آرت پشتیبانی نمی‌کند"
    override val txtLblAudioFileIntroNotSupported = "ورودی صوتی از عکس شروع پشتیبانی نمی‌کند"
    override val txtLblImageFileIntroNotSupported = "ورودی عکس از عکس شروع پشتیبانی نمی\u200Cکند"
    override val txtLblMiscFileIntroNotSupported = "فرمت ورودی از عکس شروع پشتیبانی نمی‌کند"
    override val txtLblAudioFileWatermarkNotSupported = "ورودی صوتی از واترمارک پشتیبانی نمی‌کند"
    override val txtLblImageFileWatermarkNotSupported = "ورودی عکس از واترمارک پشتیبانی نمی\u200Cکند"
    override val txtLblMiscFileWatermarkNotSupported = "فرمت ورودی از واترمارک پشتیبانی نمی‌کند"
    override val txtLblErrorClipNotSet = "کلیپ مشخص نشده است"
    override val txtLblErrorClipLengthZero = "طول کلیپ صفر است"
    override val txtLblErrorClipLengthNegative = "طول کلیپ منفی است"
    override val txtLblErrorClipStartAfterMediaEnd = "شروع کلیپ از طول رسانه بیشتر است"
    override val txtLblErrorClipFromImageNotSupported = "ورودی عکس از ایجاد کلیپ پشتیبانی نمی‌کند"
    override val txtLblErrorClipFromFormatNotSupported = "فرمت ورودی از ایجاد کلیپ پشتیبانی نمی‌کند"
    override val txtLblErrorClipFileNotSet = "پرونده ذخیره مشخص نشده است"
    override val txtLblReady = "آماده"
    override val txtLblProgressInitializing = "راه‌اندازی..."
    override val txtLblProgressCreating = "ایجاد کلیپ"
    override val txtLblAboutDeveloper = "توسعه‌دهنده: مهدی حسین‌زاده"
    override val txtLblAboutPoweredBy = "با افتخار، قدرت گرفته از نرم‌افزارهای متن‌باز و رایگان"
    override val txtLblAboutKotlinLabel = "Kotlin"
    override val txtLblAboutKotlinText = " از جت‌برینز به عنوان زبان برنامه‌نویسی"
    override val txtLblAboutGradleLabel = "Gradle"
    override val txtLblAboutGradleText = " برای ساخت پروژه و گرفتن خروجی"
    override val txtLblAboutJetpackComposeLabel = "Jetpack Compose"
    override val txtLblAboutJetpackComposeText = " از گوگل برای رابط کاربری"
    override val txtLblAboutComposeMultiplatformLabel = "Compose Multiplatform"
    override val txtLblAboutComposeMultiplatformText = " از جت‌برینز برای رابط کاربری"
    override val txtLblAboutVlcLabel = "libVLC و vlcj"
    override val txtLblAboutVlcText = " برای پخش و تعامل با رسانه"
    override val txtLblAboutFfmpegLabel = "FFmpeg و JavaCV"
    override val txtLblAboutFfmpegText = " برای برش و تبدیل رسانه"
    override val txtLblAboutMaterialDesignLabel = "Material design"
    override val txtLblAboutMaterialDesignText = " و آیکون‌های متریال از گوگل"
    override val txtLblAboutInkscapeLabel = "Inkscape"
    override val txtLblAboutInkscapeText = " برای ایجاد آیکون‌ها و گرافیک برداری"
    override val txtLblAboutIntellijLabel = "IntelliJ IDEA"
    override val txtLblAboutIntellijText = " به عنوان محیط کدنویسی"
    override val txtLblAboutGitLabel = "Git"
    override val txtLblAboutGitText = " برای کنترل نسخه و پیگیری تغییرات"
    override val txtLblAboutGitHubLabel = "GitHub"
    override val txtLblAboutGitHubText = " از مایکروسافت برای میزبانی کد"
    override val txtLblAboutVazirmatnLabel = "Vazirmatn"
    override val txtLblAboutVazirmatnText = " به عنوان قلم برای زبان فارسی"

    override fun totalClipCreationTime(duration: Duration) = "${timeString(duration)} طول کشید"

    private fun timeString(duration: Duration): String {
        fun seconds(value: Long) = if (value == 0L) "" else LanguageFa.localizeDigits("$value ثانیه")
        fun minutes(value: Long) = if (value == 0L) "" else LanguageFa.localizeDigits("$value دقیقه")
        fun hours(value: Long) = if (value == 0L) "" else LanguageFa.localizeDigits("$value ساعت")
        return if (duration < 1.seconds) {
            "کمتر از یک ثانیه"
        } else if (duration < 1.minutes) {
            seconds(duration.inWholeSeconds % 60)
        } else if (duration < 1.hours) {
            buildString {
                append(minutes(duration.inWholeMinutes % 60))
                val seconds = seconds(duration.inWholeSeconds % 60)
                if (seconds != "") {
                    append(" و ")
                    append(seconds)
                }
            }
        } else {
            buildString {
                append(hours(duration.inWholeHours % 60))
                val minutes = minutes(duration.inWholeMinutes % 60)
                if (minutes != "") {
                    append(" و ")
                    append(minutes)
                }
            }
        }
    }
}
