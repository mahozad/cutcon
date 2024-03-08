package ir.mahozad.cutcon

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.*
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.component.*
import ir.mahozad.cutcon.converter.DefaultConverterFactory
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.ui.MainWindow
import ir.mahozad.cutcon.ui.errorWindow
import kotlinx.coroutines.Dispatchers
import java.util.prefs.Preferences
import kotlin.io.path.Path

// See logback.xml file in classpath for logback configs
private val logger = logger(name = "Main")
private var error: Throwable? = null

/*
Link to download vlc:
    https://get.videolan.org/vlc/
    https://download.videolan.org/pub/vlc/
    http://ftp.videolan.org/pub/videolan/vlc
How to build vlc from source: https://wiki.videolan.org/Category:Building/
    How to build for Linux: https://wiki.videolan.org/UnixCompile/
    How to build for Android: https://wiki.videolan.org/AndroidCompile/
List of vlc libraries/plugins: https://wiki.videolan.org/Contrib_Status/






===================================================================





https://github.com/JetBrains/compose-multiplatform/issues/1089
https://github.com/simplex-chat/simplex-chat/pull/3052
https://github.com/simplex-chat/simplex-chat/pull/3120
https://github.com/simplex-chat/simplex-chat/pull/3130
https://github.com/simplex-chat/simplex-chat/pull/3136
https://github.com/simplex-chat/simplex-chat/scripts/desktop/prepare-vlc-linux.sh

Different Linux distributions may have different package management systems.

The Debian and distributions derived from it (like Ubuntu (and its variants like Kubuntu, Xubuntu etc.), Mint, Kali, etc.)
use a packaging format called .deb. The tool to deal with this format is called apt.

The RedHat (RHEL) and distributions derived from it (like CentOS, Fedora, openSUSE, etc.) use a packaging format called .rpm.

Arch linux and manjaro use pacman for package management.

The deb and rpm formats typically do not include all the required dependencies of a program.
Instead, they just include the main program files and the instructions to download/install/use
required libraries for the program.

Now, new formats have been introduced to make it possible to publish a single self-contained installer (ready-to-use package)
(like that of Windows .msi or .exe installers) that can be installed or used in most Linux distributions without additional requirements.
These include Flatpak, AppImage, and Snap formats.

Fortunately, VLC is available in Snap format.
So, we are able to use this self-contained package of VLC (that includes all its libraries) down below.
Also see https://github.com/cmatomic/VLCplayer-AppImage and https://github.com/flathub/org.videolan.VLC

Note that apps can publish different variants (called channels) on the Snap repository.
For example, a stable channel, a beta channel, an old channel etc.
Now, each channel only has the latest version of an app, so there seems
to be no way to download, for example, a previous stable version of VLC.
So, upload vlc snap files to maven repository as a library or keep a backup of them if/when VLC snap gets updated.

Make sure to remove the option "--quiet" and pass the options "--verbose", "2" to vlc (through vlcj MediaPlayerFactory)
to see all errors and warnings from VLC when running the app.

Here are the steps to embed VLC in the app for Linux:

1. Remove the default installed VLC (if any) on Ubuntu:
   https://askubuntu.com/q/572865
   - sudo apt autoremove
   - sudo apt remove vlc-nox
   - sudo apt remove vlc

2. Make sure no VLC is installed:
   which vlc
   whereis vlc

3. Inspect available versions of VLC in SNAP format:
   https://askubuntu.com/q/1268615
   - snap info vlc

4. Download the snap package of VLC (instead of directly installing it)
   it will be downloaded in the current working directory
   - sudo snap download vlc --channel=latest/stable
   (can also install vlc with sudo snap install vlc --channel=latest/stable)

5. Mount the downloaded vlc snap file
   https://askubuntu.com/q/1162798
   - mkdir <mount-folder-name>
   - sudo mount -t squashfs -o ro /path/to/my.snap /path/to/<mount-folder-name>

6. Extract the directory to another folder (also, because it is read-only):
   sudo cp -r vlc-mount/ vlc-mount-copy/

7. Unmount and remove the original mounted folder:
   sudo umount vlc-mount/ && rm -r vlc-mount/

8. Install chrpath tool:
   sudo apt update
   sudo apt install chrpath

9. (Optional) View all .so files that have rpath= or runpath= in them
    find . -name "*.so*" | xargs -n1 chrpath | grep "="

10. Do these in order:
    cd vlc-mount-copy/usr/lib/
    sudo chrpath -r '$ORIGIN' libvlc.so
    cd vlc/
    sudo chrpath -r '$ORIGIN/..' libvlc_pulse.so.0.0.0
    sudo chrpath -r '$ORIGIN/..' libvlc_xcb_events.so.0.0.0
    cd plugins/
    find . -name "*.so*" | sudo xargs -n1 chrpath -r '$ORIGIN/../../..'
    cd ../../../../..
    cp -r vlc-mount-copy/usr/lib/* <project-path>/asset/linux/vlc
    cp -r <project-path>/asset/linux/vlc/x86_64-linux-gnu/* <project-path>/asset/linux/vlc/
    rm -r <project-path>/asset/linux/vlc/ssl/
    rm -r <project-path>/asset/linux/vlc/jvm/
    rm -r <project-path>/asset/linux/vlc/debug/
    rm -r <project-path>/asset/linux/vlc/x86_64-linux-gnu/





========================================================





What is rpath (DT_RPATH) and runpath (DT_RUNPATH)?

See this good explanation:
https://unix.stackexchange.com/q/22926

rpath designates the run-time search path hard-coded in an executable file or library.

The rpath is stored in the executable (it's the DT_RPATH or DT_RUNPATH dynamic attribute).
It can contain absolute paths or relative paths or paths starting with $ORIGIN.
Relative paths are relative to the terminal or process working directory whereas $ORIGIN is the location of the file
(e.g. if the file is in /opt/myapp/bin and its rpath is $ORIGIN/../lib:$ORIGIN/../plugins then the dynamic linker will look in /opt/myapp/lib and /opt/myapp/plugins).
https://stackoverflow.com/q/38058041

rpath was deprecated in favor of runpath.
https://stackoverflow.com/q/7967848

patchelf vs chrpath (for viewing, changing, deleting rpath)
https://stackoverflow.com/q/13769141

using objdump -x libvlc.so or objdump -p libvlc.so | grep NEEDED to inspect an so file
https://en.wikipedia.org/wiki/Rpath
https://en.wikipedia.org/wiki/Ldd_(Unix)

using ldd mylib.so file to inspect references
it is a way to view what/which libraries are bound to your executable
https://stackoverflow.com/q/29422614

ldd vs readelf
https://stackoverflow.com/q/6242761

Hardware video acceleration
    NVIDIA "vdpau" (mesa-vdpau-drivers;libvdpau)
    intel "vaapi"(libva )
    AMD "vaapi" and "vdpau"
See https://wiki.archlinux.org/index.php/Hardware_video_acceleration

Embed libVLC in Android: https://stackoverflow.com/q/39311753




 */
 */
 */

/**
 * Note that it does not catch exceptions thrown outside a [Window].
 * See https://github.com/JetBrains/compose-multiplatform/issues/663
 * and https://github.com/JetBrains/compose-multiplatform/issues/1764
 * and https://github.com/JetBrains/compose-multiplatform/issues/4233
 */
@OptIn(ExperimentalComposeUiApi::class)
private val ApplicationScope.exceptionHandler
    get() = WindowExceptionHandlerFactory {
        WindowExceptionHandler {
            logger.error(it) { "An error occurred in the application" }
            // The app should be terminated so the code will continue after
            // application {} because of its exitProcessOnExit = false
            terminate()
            error = it
        }
    }

val assetsPath = System
    .getProperty("compose.application.resources.dir")
    ?.let(::Path)
    ?.also { logger.debug { "Custom assets path: $it" } }
    ?: error(Messages.ERR_COMPOSE_RES_DIR_NOT_SET)

val viewModel = MainViewModel(
    urlMaker = DefaultUrlMaker,
    settings = Preferences.userRoot(),
    dispatcher = Dispatchers.IO,
    mediaPlayer = DefaultMediaPlayer(),
    dateTimeChecker = DefaultDateTimeChecker(Dispatchers.IO),
    converterFactory = DefaultConverterFactory(Dispatchers.IO),
    saveFileNameGenerator = DefaultSaveFileNameGenerator
)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    logger.info { "Application started" }
    initialize()
    application(exitProcessOnExit = false /* See exceptionHandler */) {
        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides exceptionHandler
        ) {
            MainWindow(onExitRequest = ::terminate)
        }
    }
    error?.let(::errorWindow)
    logger.info { "Application finished" }
}

private fun initialize() {
    viewModel.startUrlMaker()
    viewModel.startDateTimeChecker()
    viewModel.startMediaProgressListener()
}

// This is also called when force closing/halting/killing the app process
private fun ApplicationScope.terminate() {
    logger.info { "Cancelling everything..." }
    viewModel.cancelEverything()
    logger.info { "Exiting the application..." }
    exitApplication()
}
