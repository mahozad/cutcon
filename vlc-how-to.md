Links for downloading all the versions of VLC (source or installer):
  - https://get.videolan.org/vlc/
  - https://download.videolan.org/pub/vlc/
  - http://ftp.videolan.org/pub/videolan/vlc
How to build VLC from source:
  - https://wiki.videolan.org/Category:Building/
How to build/embed for Linux:
  - https://wiki.videolan.org/UnixCompile/
How to build/embed for Android:
  - https://wiki.videolan.org/AndroidCompile/
  - https://github.com/masterwok/simple-vlc-player
  - https://github.com/mrmaffen/vlc-android-sdk
  - https://stackoverflow.com/questions/39311753/embed-libvlc-into-my-android-app-is-not-playing-video-only-audio-is-being-playe
List of VLC libraries/plugins:
  - https://wiki.videolan.org/Contrib_Status/
Hardware video acceleration (see https://wiki.archlinux.org/index.php/Hardware_video_acceleration):
  - NVIDIA "vdpau" (mesa-vdpau-drivers;libvdpau)
  - intel "vaapi"(libva )
  - AMD "vaapi" and "vdpau"


---


See
  - https://unix.stackexchange.com/questions/227910/will-my-linux-binary-work-on-all-distros
  - https://stackoverflow.com/questions/78000488/is-there-a-list-of-shared-libraries-available-in-any-linux
  - https://askubuntu.com/questions/350068/where-does-ubuntu-look-for-shared-libraries
  - https://www.tecmint.com/understanding-shared-libraries-in-linux/
  - https://github.com/conan-io/conan/issues/11465#Sharing-binaries-across-different-linux-distros


https://github.com/JetBrains/compose-multiplatform/issues/1089
https://github.com/simplex-chat/simplex-chat/pull/3052
https://github.com/simplex-chat/simplex-chat/pull/3120
https://github.com/simplex-chat/simplex-chat/pull/3130
https://github.com/simplex-chat/simplex-chat/pull/3136
https://github.com/simplex-chat/simplex-chat/scripts/desktop/prepare-vlc-linux.sh


---


## Setup VLC for Linux
VLC provides installers for Windows and macOS but provides just the source code for Linux (and some installers; read below).

### Build VLC from source code
  - Download the VLC release archive (see the links above)
  - extract it: `tar xJf vlc-3.0.20.tar.xz`
  - `cd vlc-3.0.20`
  - `sudo apt install g++ make libtool automake autopoint pkg-config flex bison lua5.2`
  - Enable sources with either of these ways:
    + Open *Software & Updates* app and enable the *Sources* checkbox and click close and click reload
    + In `/etc/apt/sources.list` uncomment lines that start with `deb-src` and then `sudo apt update`
  - `./bootstrap`
  - Link against libraries:
    + To link against dynamic libraries (meaning libraries installed or available on the OS):
      `sudo apt build-dep vlc`
    + To link statically (that is, provide the libraries along with the vlc):
      `sudo apt install subversion yasm cvs cmake ragel`  
      `cd contrib`  
      `mkdir native`  
      `cd native`  
      `../bootstrap`  
      `make`  
  - `./configure` (make sure it executes and ends with no error)
  - `./compile`

### Use VLC packages/installers
Different Linux distributions have different package management systems.

Debian and distributions derived from it (like Ubuntu (and its variants like Kubuntu, Xubuntu etc.), Mint, Kali, etc.)
use a packaging format called **.deb**. The tool to deal with this format is called **apt**.

RedHat (RHEL) and distributions derived from it (like CentOS, Fedora, openSUSE, etc.) use a packaging format called **.rpm**.

Arch linux and manjaro use **pacman** for package management.

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


---


#### What is rpath (DT_RPATH) and runpath (DT_RUNPATH)?
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



















---


List of files after deleting some of them so that VLC still worked in the app: 

IdeaProjects/cutcon/asset/linux/vlc/
├── avahi
│   └── service-types.db
├── dbus-1.0
│   └── dbus-daemon-launch-helper
├── dconf
│   └── dconf-service
├── engines-1.1
│   ├── afalg.so
│   ├── capi.so
│   └── padlock.so
├── gdk-pixbuf-2.0
│   ├── 2.10.0
│   │   └── loaders
│   │       ├── libpixbufloader-ani.so
│   │       ├── libpixbufloader-bmp.so
│   │       ├── libpixbufloader-gif.so
│   │       ├── libpixbufloader-icns.so
│   │       ├── libpixbufloader-ico.so
│   │       ├── libpixbufloader-jpeg.so
│   │       ├── libpixbufloader-png.so
│   │       ├── libpixbufloader-pnm.so
│   │       ├── libpixbufloader-qtif.so
│   │       ├── libpixbufloader-svg.so
│   │       ├── libpixbufloader-tga.so
│   │       ├── libpixbufloader-tiff.so
│   │       ├── libpixbufloader-xbm.so
│   │       └── libpixbufloader-xpm.so
│   └── gdk-pixbuf-query-loaders
├── gio
│   └── modules
│       ├── libdconfsettings.so
│       ├── libgiognomeproxy.so
│       ├── libgiognutls.so
│       └── libgiolibproxy.so
├── glib-2.0
│   ├── gio-querymodules
│   └── glib-compile-schemas
├── glib-networking
│   └── glib-pacrunner
├── gnupg
│   ├── dirmngr_ldap
│   ├── gpg-check-pattern
│   ├── gpg-preset-passphrase
│   ├── gpg-protect-tool
│   └── gpg-wks-client
├── gnupg2
│   ├── gpg-preset-passphrase -> ../gnupg/gpg-preset-passphrase
│   └── gpg-protect-tool -> ../gnupg/gpg-protect-tool
├── jni
│   ├── libatk-wrapper.so -> libatk-wrapper.so.6.0.0
│   ├── libatk-wrapper.so.6 -> libatk-wrapper.so.6.0.0
│   └── libatk-wrapper.so.6.0.0
├── kconf_update_bin
│   └── kde4breeze
├── libaacs.so.0 -> libaacs.so.0.6.0
├── libaacs.so.0.6.0
├── libAppStreamQt.so.0.12.7
├── libAppStreamQt.so.2 -> libAppStreamQt.so.0.12.7
├── libappstream.so.0.12.7
├── libappstream.so.4 -> libappstream.so.0.12.7
├── libasn1.so.8 -> libasn1.so.8.0.0
├── libasn1.so.8.0.0
├── libasound.so.2 -> libasound.so.2.0.0
├── libasound.so.2.0.0
├── libassuan.so.0 -> libassuan.so.0.8.1
├── libassuan.so.0.8.1
├── libasyncns.so.0 -> libasyncns.so.0.3.1
├── libasyncns.so.0.3.1
├── libatk-1.0.so.0 -> libatk-1.0.so.0.22810.1
├── libatk-1.0.so.0.22810.1
├── libatk-bridge-2.0.so.0 -> libatk-bridge-2.0.so.0.0.0
├── libatk-bridge-2.0.so.0.0.0
├── libatspi.so.0 -> libatspi.so.0.0.1
├── libatspi.so.0.0.1
├── libavahi-client.so.3 -> libavahi-client.so.3.2.9
├── libavahi-client.so.3.2.9
├── libavahi-common.so.3 -> libavahi-common.so.3.5.3
├── libavahi-common.so.3.5.3
├── libbreezecommon5.so.5 -> libbreezecommon5.so.5.19.4
├── libbreezecommon5.so.5.19.4
├── libcairo-gobject.so.2 -> libcairo-gobject.so.2.11510.0
├── libcairo-gobject.so.2.11510.0
├── libcairo.so.2 -> libcairo.so.2.11510.0
├── libcairo.so.2.11510.0
├── libcanberra-0.30
│   └── libcanberra-alsa.so
├── libcanberra.so.0 -> libcanberra.so.0.2.5
├── libcanberra.so.0.2.5
├── libcolordprivate.so.2 -> libcolordprivate.so.2.0.5
├── libcolordprivate.so.2.0.5
├── libcolord.so.2 -> libcolord.so.2.0.5
├── libcolord.so.2.0.5
├── libcroco-0.6.so.3 -> libcroco-0.6.so.3.0.1
├── libcroco-0.6.so.3.0.1
├── libcups.so.2
├── libdatrie.so.1 -> libdatrie.so.1.3.3
├── libdatrie.so.1.3.3
├── libdbusmenu-qt5.so.2 -> libdbusmenu-qt5.so.2.6.0
├── libdbusmenu-qt5.so.2.6.0
├── libdconf.so.1 -> libdconf.so.1.0.0
├── libdconf.so.1.0.0
├── libdouble-conversion.so.1 -> libdouble-conversion.so.1.0
├── libdouble-conversion.so.1.0
├── libdrm_amdgpu.so.1 -> libdrm_amdgpu.so.1.0.0
├── libdrm_amdgpu.so.1.0.0
├── libdrm_intel.so.1 -> libdrm_intel.so.1.0.0
├── libdrm_intel.so.1.0.0
├── libdrm_nouveau.so.2 -> libdrm_nouveau.so.2.0.0
├── libdrm_nouveau.so.2.0.0
├── libdrm_radeon.so.1 -> libdrm_radeon.so.1.0.1
├── libdrm_radeon.so.1.0.1
├── libdrm.so.2 -> libdrm.so.2.4.0
├── libdrm.so.2.4.0
├── libedit.so.2 -> libedit.so.2.0.56
├── libedit.so.2.0.56
├── libEGL_mesa.so.0 -> libEGL_mesa.so.0.0.0
├── libEGL_mesa.so.0.0.0
├── libEGL.so.1 -> libEGL.so.1.0.0
├── libEGL.so.1.0.0
├── libelf-0.170.so
├── libelf.so.1 -> libelf-0.170.so
├── libepoxy.so.0 -> libepoxy.so.0.0.0
├── libepoxy.so.0.0.0
├── libevdev.so.2 -> libevdev.so.2.1.20
├── libevdev.so.2.1.20
├── libexec
│   └── kf5
│       ├── kiod5
│       ├── kioexec
│       ├── kio_http_cache_cleaner
│       ├── kioslave5
│       ├── kpac_dhcp_helper
│       └── kpackagehandlers
│           ├── appstreamhandler
│           └── knshandler
├── libexpatw.so.1 -> libexpatw.so.1.6.7
├── libexpatw.so.1.6.7
├── libexslt.so.0 -> libexslt.so.0.8.17
├── libexslt.so.0.8.17
├── libfam.so.0 -> libfam.so.0.0.0
├── libfam.so.0.0.0
├── libffi.so.6 -> libffi.so.6.0.4
├── libffi.so.6.0.4
├── libFLAC.so.8 -> libFLAC.so.8.3.0
├── libFLAC.so.8.3.0
├── libfontconfig.so.1 -> libfontconfig.so.1.10.1
├── libfontconfig.so.1.10.1
├── libfontenc.so.1 -> libfontenc.so.1.0.0
├── libfontenc.so.1.0.0
├── libfreebl3.chk -> nss/libfreebl3.chk
├── libfreebl3.so -> nss/libfreebl3.so
├── libfreeblpriv3.chk -> nss/libfreeblpriv3.chk
├── libfreeblpriv3.so -> nss/libfreeblpriv3.so
├── libfreerdp-cache.so.1.1 -> libfreerdp-cache.so.1.1.0
├── libfreerdp-cache.so.1.1.0
├── libfreerdp-client.so.1.1 -> libfreerdp-client.so.1.1.0
├── libfreerdp-client.so.1.1.0
├── libfreerdp-codec.so.1.1 -> libfreerdp-codec.so.1.1.0
├── libfreerdp-codec.so.1.1.0
├── libfreerdp-common.so.1.1.0 -> libfreerdp-common.so.1.1.0-beta1
├── libfreerdp-common.so.1.1.0-beta1
├── libfreerdp-core.so.1.1 -> libfreerdp-core.so.1.1.0
├── libfreerdp-core.so.1.1.0
├── libfreerdp-crypto.so.1.1 -> libfreerdp-crypto.so.1.1.0
├── libfreerdp-crypto.so.1.1.0
├── libfreerdp-gdi.so.1.1 -> libfreerdp-gdi.so.1.1.0
├── libfreerdp-gdi.so.1.1.0
├── libfreerdp-locale.so.1.1 -> libfreerdp-locale.so.1.1.0
├── libfreerdp-locale.so.1.1.0
├── libfreerdp-primitives.so.1.1 -> libfreerdp-primitives.so.1.1.0
├── libfreerdp-primitives.so.1.1.0
├── libfreerdp-utils.so.1.1 -> libfreerdp-utils.so.1.1.0
├── libfreerdp-utils.so.1.1.0
├── libfreetype.so.6 -> libfreetype.so.6.15.0
├── libfreetype.so.6.15.0
├── libfribidi.so.0 -> libfribidi.so.0.3.6
├── libfribidi.so.0.3.6
├── libgbm.so.1 -> libgbm.so.1.0.0
├── libgbm.so.1.0.0
├── libgdbm_compat.so.4 -> libgdbm_compat.so.4.0.0
├── libgdbm_compat.so.4.0.0
├── libgdbm.so.5 -> libgdbm.so.5.0.0
├── libgdbm.so.5.0.0
├── libgdk-3.so.0 -> libgdk-3.so.0.2200.30
├── libgdk-3.so.0.2200.30
├── libgdk_pixbuf-2.0.so.0 -> libgdk_pixbuf-2.0.so.0.3611.0
├── libgdk_pixbuf-2.0.so.0.3611.0
├── libgdk_pixbuf_xlib-2.0.so.0 -> libgdk_pixbuf_xlib-2.0.so.0.3611.0
├── libgdk_pixbuf_xlib-2.0.so.0.3611.0
├── libgdk-x11-2.0.so.0 -> libgdk-x11-2.0.so.0.2400.32
├── libgdk-x11-2.0.so.0.2400.32
├── libgif.so.7 -> libgif.so.7.0.0
├── libgif.so.7.0.0
├── libglapi.so.0 -> libglapi.so.0.0.0
├── libglapi.so.0.0.0
├── libGLdispatch.so.0 -> libGLdispatch.so.0.0.0
├── libGLdispatch.so.0.0.0
├── libGLESv2.so.2 -> libGLESv2.so.2.0.0
├── libGLESv2.so.2.0.0
├── libglib-2.0.so.0 -> libglib-2.0.so.0.5600.4
├── libglib-2.0.so.0.5600.4
├── libGL.so.1 -> libGL.so.1.0.0
├── libGL.so.1.0.0
├── libGLX_indirect.so.0 -> libGLX_mesa.so.0
├── libGLX_mesa.so.0 -> libGLX_mesa.so.0.0.0
├── libGLX_mesa.so.0.0.0
├── libGLX.so.0 -> libGLX.so.0.0.0
├── libGLX.so.0.0.0
├── libgmodule-2.0.so.0 -> libgmodule-2.0.so.0.5600.4
├── libgmodule-2.0.so.0.5600.4
├── libgmp.so.10 -> libgmp.so.10.3.2
├── libgmp.so.10.3.2
├── libgobject-2.0.so.0 -> libgobject-2.0.so.0.5600.4
├── libgobject-2.0.so.0.5600.4
├── libgpgmepp.so.6 -> libgpgmepp.so.6.8.0
├── libgpgmepp.so.6.8.0
├── libgpgme-pthread.so.11 -> libgpgme.so.11
├── libgpgme.so.11 -> libgpgme.so.11.21.0
├── libgpgme.so.11.21.0
├── libgraphite2.so.2.0.0 -> libgraphite2.so.3
├── libgraphite2.so.3 -> libgraphite2.so.3.0.1
├── libgraphite2.so.3.0.1
├── libgssapi_krb5.so.2 -> libgssapi_krb5.so.2.2
├── libgssapi_krb5.so.2.2
├── libgssapi.so.3 -> libgssapi.so.3.0.0
├── libgssapi.so.3.0.0
├── libgthread-2.0.so.0 -> libgthread-2.0.so.0.5600.4
├── libgthread-2.0.so.0.5600.4
├── libgudev-1.0.so.0 -> libgudev-1.0.so.0.2.0
├── libgudev-1.0.so.0.2.0
├── libharfbuzz.so.0 -> libharfbuzz.so.0.10702.0
├── libharfbuzz.so.0.10702.0
├── libhcrypto.so.4 -> libhcrypto.so.4.1.0
├── libhcrypto.so.4.1.0
├── libheimbase.so.1 -> libheimbase.so.1.0.0
├── libheimbase.so.1.0.0
├── libheimntlm.so.0 -> libheimntlm.so.0.1.0
├── libheimntlm.so.0.1.0
├── libhogweed.so.4 -> libhogweed.so.4.5
├── libhogweed.so.4.5
├── libhx509.so.5 -> libhx509.so.5.0.0
├── libhx509.so.5.0.0
├── libICE.so.6 -> libICE.so.6.3.0
├── libICE.so.6.3.0
├── libicuio.so.60 -> libicuio.so.60.2
├── libicuio.so.60.2
├── libicutest.so.60 -> libicutest.so.60.2
├── libicutest.so.60.2
├── libicutu.so.60 -> libicutu.so.60.2
├── libicutu.so.60.2
├── libidn2.so.0 -> libidn2.so.0.3.3
├── libidn2.so.0.3.3
├── libinput.so.10 -> libinput.so.10.13.0
├── libinput.so.10.13.0
├── libixml.so.2
├── libjackserver.so.0 -> libjackserver.so.0.0.28
├── libjackserver.so.0.0.28
├── libjack.so.0 -> libjack.so.0.0.28
├── libjack.so.0.0.28
├── libjbig.so.0
├── libjpeg.so.8 -> libjpeg.so.8.1.2
├── libjpeg.so.8.1.2
├── libjson-glib-1.0.so.0 -> libjson-glib-1.0.so.0.400.2
├── libjson-glib-1.0.so.0.400.2
├── libk5crypto.so.3 -> libk5crypto.so.3.1
├── libk5crypto.so.3.1
├── libkdecorations2private.so.5.19.4
├── libkdecorations2private.so.7 -> libkdecorations2private.so.5.19.4
├── libkdecorations2.so.5 -> libkdecorations2.so.5.19.4
├── libkdecorations2.so.5.19.4
├── libKF5Archive.so.5 -> libKF5Archive.so.5.72.0
├── libKF5Archive.so.5.72.0
├── libKF5Attica.so.5 -> libKF5Attica.so.5.72.0
├── libKF5Attica.so.5.72.0
├── libKF5AuthCore.so.5 -> libKF5AuthCore.so.5.72.0
├── libKF5AuthCore.so.5.72.0
├── libKF5Auth.so.5 -> libKF5Auth.so.5.72.0
├── libKF5Auth.so.5.72.0
├── libKF5Bookmarks.so.5 -> libKF5Bookmarks.so.5.72.0
├── libKF5Bookmarks.so.5.72.0
├── libKF5Codecs.so.5 -> libKF5Codecs.so.5.72.0
├── libKF5Codecs.so.5.72.0
├── libKF5Completion.so.5 -> libKF5Completion.so.5.72.0
├── libKF5Completion.so.5.72.0
├── libKF5ConfigCore.so.5 -> libKF5ConfigCore.so.5.72.0
├── libKF5ConfigCore.so.5.72.0
├── libKF5ConfigGui.so.5 -> libKF5ConfigGui.so.5.72.0
├── libKF5ConfigGui.so.5.72.0
├── libKF5ConfigWidgets.so.5 -> libKF5ConfigWidgets.so.5.72.0
├── libKF5ConfigWidgets.so.5.72.0
├── libKF5CoreAddons.so.5 -> libKF5CoreAddons.so.5.72.0
├── libKF5CoreAddons.so.5.72.0
├── libKF5Crash.so.5 -> libKF5Crash.so.5.72.0
├── libKF5Crash.so.5.72.0
├── libKF5DBusAddons.so.5 -> libKF5DBusAddons.so.5.72.0
├── libKF5DBusAddons.so.5.72.0
├── libKF5Declarative.so.5 -> libKF5Declarative.so.5.72.0
├── libKF5Declarative.so.5.72.0
├── libKF5DocTools.so.5 -> libKF5DocTools.so.5.72.0
├── libKF5DocTools.so.5.72.0
├── libKF5GlobalAccel.so.5 -> libKF5GlobalAccel.so.5.72.0
├── libKF5GlobalAccel.so.5.72.0
├── libKF5GuiAddons.so.5 -> libKF5GuiAddons.so.5.72.0
├── libKF5GuiAddons.so.5.72.0
├── libKF5I18n.so.5 -> libKF5I18n.so.5.72.0
├── libKF5I18n.so.5.72.0
├── libKF5IconThemes.so.5 -> libKF5IconThemes.so.5.72.0
├── libKF5IconThemes.so.5.72.0
├── libKF5ItemViews.so.5 -> libKF5ItemViews.so.5.72.0
├── libKF5ItemViews.so.5.72.0
├── libKF5JobWidgets.so.5 -> libKF5JobWidgets.so.5.72.0
├── libKF5JobWidgets.so.5.72.0
├── libKF5KCMUtils.so.5 -> libKF5KCMUtils.so.5.72.0
├── libKF5KCMUtils.so.5.72.0
├── libKF5KIOFileWidgets.so.5 -> libKF5KIOFileWidgets.so.5.72.0
├── libKF5KIOFileWidgets.so.5.72.0
├── libKF5KIOGui.so.5 -> libKF5KIOGui.so.5.72.0
├── libKF5KIOGui.so.5.72.0
├── libKF5KIONTLM.so.5 -> libKF5KIONTLM.so.5.72.0
├── libKF5KIONTLM.so.5.72.0
├── libKF5Kirigami2.so.5 -> libKF5Kirigami2.so.5.72.0
├── libKF5Kirigami2.so.5.72.0
├── libKF5NewStuffCore.so.5 -> libKF5NewStuffCore.so.5.72.0
├── libKF5NewStuffCore.so.5.72.0
├── libKF5Notifications.so.5 -> libKF5Notifications.so.5.72.0
├── libKF5Notifications.so.5.72.0
├── libKF5Package.so.5 -> libKF5Package.so.5.72.0
├── libKF5Package.so.5.72.0
├── libKF5QuickAddons.so.5 -> libKF5QuickAddons.so.5.72.0
├── libKF5QuickAddons.so.5.72.0
├── libKF5Service.so.5 -> libKF5Service.so.5.72.0
├── libKF5Service.so.5.72.0
├── libKF5Solid.so.5 -> libKF5Solid.so.5.72.0
├── libKF5Solid.so.5.72.0
├── libKF5SonnetCore.so.5 -> libKF5SonnetCore.so.5.72.0
├── libKF5SonnetCore.so.5.72.0
├── libKF5SonnetUi.so.5 -> libKF5SonnetUi.so.5.72.0
├── libKF5SonnetUi.so.5.72.0
├── libKF5Style.so.5 -> libKF5Style.so.5.72.0
├── libKF5Style.so.5.72.0
├── libKF5TextWidgets.so.5 -> libKF5TextWidgets.so.5.72.0
├── libKF5TextWidgets.so.5.72.0
├── libKF5Wallet.so.5 -> libKF5Wallet.so.5.72.0
├── libKF5Wallet.so.5.72.0
├── libKF5WaylandClient.so.5 -> libKF5WaylandClient.so.5.72.0
├── libKF5WaylandClient.so.5.72.0
├── libKF5WindowSystem.so.5 -> libKF5WindowSystem.so.5.72.0
├── libKF5WindowSystem.so.5.72.0
├── libKF5XmlGui.so.5 -> libKF5XmlGui.so.5.72.0
├── libKF5XmlGui.so.5.72.0
├── libkrb5.so.26 -> libkrb5.so.26.0.0
├── libkrb5.so.26.0.0
├── libkrb5.so.3 -> libkrb5.so.3.3
├── libkrb5.so.3.3
├── libkrb5support.so.0 -> libkrb5support.so.0.1
├── libkrb5support.so.0.1
├── libksba.so.8 -> libksba.so.8.11.6
├── libksba.so.8.11.6
├── libkwalletbackend5.so.5 -> libkwalletbackend5.so.5.72.0
├── libkwalletbackend5.so.5.72.0
├── liblber-2.4.so.2 -> liblber-2.4.so.2.10.8
├── liblber-2.4.so.2.10.8
├── liblcms2.so.2 -> liblcms2.so.2.0.8
├── liblcms2.so.2.0.8
├── libldap-2.4.so.2 -> libldap_r-2.4.so.2
├── libldap_r-2.4.so.2 -> libldap_r-2.4.so.2.10.8
├── libldap_r-2.4.so.2.10.8
├── liblmdb.so.0 -> liblmdb.so.0.0.0
├── liblmdb.so.0.0.0
├── libltdl.so.7 -> libltdl.so.7.3.1
├── libltdl.so.7.3.1
├── liblua5.2-c++.so.0 -> liblua5.2-c++.so.0.0.0
├── liblua5.2-c++.so.0.0.0
├── liblua5.2.so.0 -> liblua5.2.so.0.0.0
├── liblua5.2.so.0.0.0
├── liblz4.so.1
├── libmtdev.so.1 -> libmtdev.so.1.0.0
├── libmtdev.so.1.0.0
├── libmtp.so.9 -> libmtp.so.9.3.0
├── libmtp.so.9.3.0
├── libnettle.so.6 -> libnettle.so.6.5
├── libnettle.so.6.5
├── libnotify.so.4 -> libnotify.so.4.0.0
├── libnotify.so.4.0.0
├── libnpth.so.0 -> libnpth.so.0.1.1
├── libnpth.so.0.1.1
├── libnspr4.so
├── libnssutil3.so
├── libogg.so.0 -> libogg.so.0.8.2
├── libogg.so.0.8.2
├── libpackagekitqt5.so.1 -> libpackagekitqt5.so.1.0.1
├── libpackagekitqt5.so.1.0.1
├── libpango-1.0.so.0 -> libpango-1.0.so.0.4000.14
├── libpango-1.0.so.0.4000.14
├── libpangocairo-1.0.so.0 -> libpangocairo-1.0.so.0.4000.14
├── libpangocairo-1.0.so.0.4000.14
├── libpangoft2-1.0.so.0 -> libpangoft2-1.0.so.0.4000.14
├── libpangoft2-1.0.so.0.4000.14
├── libpciaccess.so.0 -> libpciaccess.so.0.11.1
├── libpciaccess.so.0.11.1
├── libpcre2-16.so.0 -> libpcre2-16.so.0.7.0
├── libpcre2-16.so.0.7.0
├── libpcsclite.so.1 -> libpcsclite.so.1.0.0
├── libpcsclite.so.1.0.0
├── libphonon4qt5.so.4 -> libphonon4qt5.so.4.11.1
├── libphonon4qt5.so.4.11.1
├── libpixman-1.so.0 -> libpixman-1.so.0.34.0
├── libpixman-1.so.0.34.0
├── libplc4.so
├── libplds4.so
├── libpng16.so.16 -> libpng16.so.16.34.0
├── libpng16.so.16.34.0
├── libpolkit-agent-1.so.0 -> libpolkit-agent-1.so.0.0.0
├── libpolkit-agent-1.so.0.0.0
├── libpolkit-gobject-1.so.0 -> libpolkit-gobject-1.so.0.0.0
├── libpolkit-gobject-1.so.0.0.0
├── libpolkit-qt5-agent-1.so.1 -> libpolkit-qt5-agent-1.so.1.113.0
├── libpolkit-qt5-agent-1.so.1.113.0
├── libpolkit-qt5-core-1.so.1 -> libpolkit-qt5-core-1.so.1.113.0
├── libpolkit-qt5-core-1.so.1.113.0
├── libpolkit-qt5-gui-1.so.1 -> libpolkit-qt5-gui-1.so.1.113.0
├── libpolkit-qt5-gui-1.so.1.113.0
├── libproxy.so.1 -> libproxy.so.1.0.0
├── libproxy.so.1.0.0
├── libpulse-mainloop-glib.so.0 -> libpulse-mainloop-glib.so.0.0.5
├── libpulse-mainloop-glib.so.0.0.5
├── libpulse-simple.so.0 -> libpulse-simple.so.0.1.1
├── libpulse-simple.so.0.1.1
├── libpulse.so.0 -> libpulse.so.0.20.2
├── libpulse.so.0.20.2
├── librest-0.7.so.0 -> librest-0.7.so.0.0.0
├── librest-0.7.so.0.0.0
├── libroken.so.18 -> libroken.so.18.1.0
├── libroken.so.18.1.0
├── librsvg-2.so.2 -> librsvg-2.so.2.40.20
├── librsvg-2.so.2.40.20
├── libsasl2.so.2 -> libsasl2.so.2.0.25
├── libsasl2.so.2.0.25
├── libsecret-1.so.0 -> libsecret-1.so.0.0.0
├── libsecret-1.so.0.0.0
├── libsensors.so.4 -> libsensors.so.4.4.0
├── libsensors.so.4.4.0
├── libsmime3.so
├── libSM.so.6 -> libSM.so.6.0.1
├── libSM.so.6.0.1
├── libsndfile.so.1 -> libsndfile.so.1.0.28
├── libsndfile.so.1.0.28
├── libsoup-2.4.so.1 -> libsoup-2.4.so.1.8.0
├── libsoup-2.4.so.1.8.0
├── libsoup-gnome-2.4.so.1 -> libsoup-gnome-2.4.so.1.8.0
├── libsoup-gnome-2.4.so.1.8.0
├── libsqlite3.so.0 -> libsqlite3.so.0.8.6
├── libsqlite3.so.0.8.6
├── libssl3.so
├── libssl.so.1.0.0
├── libssl.so.1.1
├── libstemmer.so.0d -> libstemmer.so.0d.0.0
├── libstemmer.so.0d.0.0
├── libtasn1.so.6 -> libtasn1.so.6.5.5
├── libtasn1.so.6.5.5
├── libtdb.so.1 -> libtdb.so.1.3.15
├── libtdb.so.1.3.15
├── libthai.so.0 -> libthai.so.0.3.0
├── libthai.so.0.3.0
├── libthreadutil.so.6
├── libtiff.so.5 -> libtiff.so.5.3.0
├── libtiff.so.5.3.0
├── libupnp.so.6
├── libva-drm.so.2 -> libva-drm.so.2.100.0
├── libva-drm.so.2.100.0
├── libva.so.2 -> libva.so.2.100.0
├── libva.so.2.100.0
├── libva-x11.so.2 -> libva-x11.so.2.100.0
├── libva-x11.so.2.100.0
├── libvdpau.so.1 -> libvdpau.so.1.0.0
├── libvdpau.so.1.0.0
├── libVkLayer_threading.so
├── libVkLayer_utils.so
├── libvlccore.so -> libvlccore.so.9.0.1
├── libvlccore.so.9 -> libvlccore.so.9.0.1
├── libvlccore.so.9.0.1
├── libvlc.so -> libvlc.so.5.6.1
├── libvlc.so.5 -> libvlc.so.5.6.1
├── libvlc.so.5.6.1
├── libvorbisenc.so.2 -> libvorbisenc.so.2.0.11
├── libvorbisenc.so.2.0.11
├── libvorbisfile.so.3 -> libvorbisfile.so.3.3.7
├── libvorbisfile.so.3.3.7
├── libvorbis.so.0 -> libvorbis.so.0.4.8
├── libvorbis.so.0.4.8
├── libvulkan.so -> libvulkan.so.1
├── libvulkan.so.1 -> libvulkan.so.1.1.70
├── libvulkan.so.1.1.70
├── libwacom.so.2 -> libwacom.so.2.6.1
├── libwacom.so.2.6.1
├── libwayland-client.so.0 -> libwayland-client.so.0.3.0
├── libwayland-client.so.0.3.0
├── libwayland-cursor.so.0 -> libwayland-cursor.so.0.0.0
├── libwayland-cursor.so.0.0.0
├── libwayland-egl.so.1 -> libwayland-egl.so.1.0.0
├── libwayland-egl.so.1.0.0
├── libwayland-server.so.0 -> libwayland-server.so.0.1.0
├── libwayland-server.so.0.1.0
├── libwind.so.0 -> libwind.so.0.0.0
├── libwind.so.0.0.0
├── libwinpr-crt.so.0.1 -> libwinpr-crt.so.0.1.0
├── libwinpr-crt.so.0.1.0
├── libwinpr-dsparse.so.0.1 -> libwinpr-dsparse.so.0.1.0
├── libwinpr-dsparse.so.0.1.0
├── libwinpr-environment.so.0.1 -> libwinpr-environment.so.0.1.0
├── libwinpr-environment.so.0.1.0
├── libwinpr-file.so.0.1 -> libwinpr-file.so.0.1.0
├── libwinpr-file.so.0.1.0
├── libwinpr-handle.so.0.1 -> libwinpr-handle.so.0.1.0
├── libwinpr-handle.so.0.1.0
├── libwinpr-heap.so.0.1 -> libwinpr-heap.so.0.1.0
├── libwinpr-heap.so.0.1.0
├── libwinpr-input.so.0.1 -> libwinpr-input.so.0.1.0
├── libwinpr-input.so.0.1.0
├── libwinpr-interlocked.so.0.1 -> libwinpr-interlocked.so.0.1.0
├── libwinpr-interlocked.so.0.1.0
├── libwinpr-library.so.0.1 -> libwinpr-library.so.0.1.0
├── libwinpr-library.so.0.1.0
├── libwinpr-path.so.0.1 -> libwinpr-path.so.0.1.0
├── libwinpr-path.so.0.1.0
├── libwinpr-pool.so.0.1 -> libwinpr-pool.so.0.1.0
├── libwinpr-pool.so.0.1.0
├── libwinpr-registry.so.0.1 -> libwinpr-registry.so.0.1.0
├── libwinpr-registry.so.0.1.0
├── libwinpr-rpc.so.0.1 -> libwinpr-rpc.so.0.1.0
├── libwinpr-rpc.so.0.1.0
├── libwinpr-sspi.so.0.1 -> libwinpr-sspi.so.0.1.0
├── libwinpr-sspi.so.0.1.0
├── libwinpr-synch.so.0.1 -> libwinpr-synch.so.0.1.0
├── libwinpr-synch.so.0.1.0
├── libwinpr-sysinfo.so.0.1 -> libwinpr-sysinfo.so.0.1.0
├── libwinpr-sysinfo.so.0.1.0
├── libwinpr-thread.so.0.1 -> libwinpr-thread.so.0.1.0
├── libwinpr-thread.so.0.1.0
├── libwinpr-utils.so.0.1 -> libwinpr-utils.so.0.1.0
├── libwinpr-utils.so.0.1.0
├── libX11-xcb.so.1 -> libX11-xcb.so.1.0.0
├── libX11-xcb.so.1.0.0
├── libXau.so.6 -> libXau.so.6.0.0
├── libXau.so.6.0.0
├── libXaw7.so.7 -> libXaw7.so.7.0.0
├── libXaw7.so.7.0.0
├── libXaw.so.7 -> libXaw7.so.7
├── libxcb-composite.so.0 -> libxcb-composite.so.0.0.0
├── libxcb-composite.so.0.0.0
├── libxcb-dri2.so.0 -> libxcb-dri2.so.0.0.0
├── libxcb-dri2.so.0.0.0
├── libxcb-dri3.so.0 -> libxcb-dri3.so.0.0.0
├── libxcb-dri3.so.0.0.0
├── libxcb-glx.so.0 -> libxcb-glx.so.0.0.0
├── libxcb-glx.so.0.0.0
├── libxcb-icccm.so.4 -> libxcb-icccm.so.4.0.0
├── libxcb-icccm.so.4.0.0
├── libxcb-image.so.0 -> libxcb-image.so.0.0.0
├── libxcb-image.so.0.0.0
├── libxcb-keysyms.so.1 -> libxcb-keysyms.so.1.0.0
├── libxcb-keysyms.so.1.0.0
├── libxcb-present.so.0 -> libxcb-present.so.0.0.0
├── libxcb-present.so.0.0.0
├── libxcb-randr.so.0 -> libxcb-randr.so.0.1.0
├── libxcb-randr.so.0.1.0
├── libxcb-render.so.0 -> libxcb-render.so.0.0.0
├── libxcb-render.so.0.0.0
├── libxcb-render-util.so.0 -> libxcb-render-util.so.0.0.0
├── libxcb-render-util.so.0.0.0
├── libxcb-res.so.0 -> libxcb-res.so.0.0.0
├── libxcb-res.so.0.0.0
├── libxcb-shape.so.0 -> libxcb-shape.so.0.0.0
├── libxcb-shape.so.0.0.0
├── libxcb-shm.so.0 -> libxcb-shm.so.0.0.0
├── libxcb-shm.so.0.0.0
├── libxcb.so.1 -> libxcb.so.1.1.0
├── libxcb.so.1.1.0
├── libxcb-sync.so.1 -> libxcb-sync.so.1.0.0
├── libxcb-sync.so.1.0.0
├── libxcb-util.so.1 -> libxcb-util.so.1.0.0
├── libxcb-util.so.1.0.0
├── libxcb-xfixes.so.0 -> libxcb-xfixes.so.0.0.0
├── libxcb-xfixes.so.0.0.0
├── libxcb-xinerama.so.0 -> libxcb-xinerama.so.0.0.0
├── libxcb-xinerama.so.0.0.0
├── libxcb-xinput.so.0 -> libxcb-xinput.so.0.1.0
├── libxcb-xinput.so.0.1.0
├── libxcb-xkb.so.1 -> libxcb-xkb.so.1.0.0
├── libxcb-xkb.so.1.0.0
├── libxcb-xv.so.0 -> libxcb-xv.so.0.0.0
├── libxcb-xv.so.0.0.0
├── libXcomposite.so.1 -> libXcomposite.so.1.0.0
├── libXcomposite.so.1.0.0
├── libXcursor.so.1 -> libXcursor.so.1.0.2
├── libXcursor.so.1.0.2
├── libXdamage.so.1 -> libXdamage.so.1.1.0
├── libXdamage.so.1.1.0
├── libXdmcp.so.6 -> libXdmcp.so.6.0.0
├── libXdmcp.so.6.0.0
├── libXext.so.6 -> libXext.so.6.4.0
├── libXext.so.6.4.0
├── libXfixes.so.3 -> libXfixes.so.3.1.0
├── libXfixes.so.3.1.0
├── libXft.so.2 -> libXft.so.2.3.2
├── libXft.so.2.3.2
├── libXinerama.so.1 -> libXinerama.so.1.0.0
├── libXinerama.so.1.0.0
├── libXi.so.6 -> libXi.so.6.1.0
├── libXi.so.6.1.0
├── libxkbcommon.so.0 -> libxkbcommon.so.0.0.0
├── libxkbcommon.so.0.0.0
├── libxkbcommon-x11.so.0 -> libxkbcommon-x11.so.0.0.0
├── libxkbcommon-x11.so.0.0.0
├── libxkbfile.so.1 -> libxkbfile.so.1.0.2
├── libxkbfile.so.1.0.2
├── libXmu.so.6 -> libXmu.so.6.2.0
├── libXmu.so.6.2.0
├── libXmuu.so.1 -> libXmuu.so.1.0.0
├── libXmuu.so.1.0.0
├── libXpm.so.4 -> libXpm.so.4.11.0
├── libXpm.so.4.11.0
├── libXrandr.so.2 -> libXrandr.so.2.2.0
├── libXrandr.so.2.2.0
├── libXrender.so.1 -> libXrender.so.1.3.0
├── libXrender.so.1.3.0
├── libxshmfence.so.1 -> libxshmfence.so.1.0.0
├── libxshmfence.so.1.0.0
├── libxslt.so.1 -> libxslt.so.1.1.29
├── libxslt.so.1.1.29
├── libXt.so.6 -> libXt.so.6.0.0
├── libXt.so.6.0.0
├── libXtst.so.6 -> libXtst.so.6.1.0
├── libXtst.so.6.1.0
├── libXv.so.1 -> libXv.so.1.0.0
├── libXv.so.1.0.0
├── libXxf86dga.so.1 -> libXxf86dga.so.1.0.0
├── libXxf86dga.so.1.0.0
├── libXxf86vm.so.1 -> libXxf86vm.so.1.0.0
├── libXxf86vm.so.1.0.0
├── libyaml-0.so.2 -> libyaml-0.so.2.0.5
├── libyaml-0.so.2.0.5
├── libzstd.so.1 -> libzstd.so.1.3.3
├── libzstd.so.1.3.3
├── libzvbi-chains.so.0 -> libzvbi-chains.so.0.0.0
├── libzvbi-chains.so.0.0.0
├── libzvbi.so.0 -> libzvbi.so.0.13.2
├── libzvbi.so.0.13.2
├── nss
│   ├── libfreebl3.chk
│   ├── libfreebl3.so
│   ├── libfreeblpriv3.chk
│   ├── libfreeblpriv3.so
│   ├── libnssckbi.so
│   ├── libnssdbm3.chk
│   ├── libnssdbm3.so
│   ├── libsoftokn3.chk
│   └── libsoftokn3.so
├── openssl-1.0.0
│   └── engines
│       ├── lib4758cca.so
│       ├── libaep.so
│       ├── libatalla.so
│       ├── libcapi.so
│       ├── libchil.so
│       ├── libcswift.so
│       ├── libgmp.so
│       ├── libgost.so
│       ├── libnuron.so
│       ├── libpadlock.so
│       ├── libsureware.so
│       └── libubsec.so
├── pkgconfig
│   ├── libvlc.pc
│   ├── vlc-plugin.pc
│   └── vulkan.pc
├── pulseaudio
│   └── libpulsecommon-11.1.so
├── sasl2
│   ├── libsasldb.so -> libsasldb.so.2.0.25
│   ├── libsasldb.so.2 -> libsasldb.so.2.0.25
│   └── libsasldb.so.2.0.25
├── systemd
│   ├── user
│   │   ├── dirmngr.service
│   │   ├── dirmngr.socket
│   │   ├── glib-pacrunner.service
│   │   ├── gpg-agent-browser.socket
│   │   ├── gpg-agent-extra.socket
│   │   ├── gpg-agent.service
│   │   ├── gpg-agent.socket
│   │   ├── gpg-agent-ssh.socket
│   │   └── sockets.target.wants
│   │       ├── dirmngr.socket -> ../dirmngr.socket
│   │       ├── gpg-agent-browser.socket -> ../gpg-agent-browser.socket
│   │       ├── gpg-agent-extra.socket -> ../gpg-agent-extra.socket
│   │       ├── gpg-agent.socket -> ../gpg-agent.socket
│   │       └── gpg-agent-ssh.socket -> ../gpg-agent-ssh.socket
│   └── user-environment-generators
│       └── 90gpg-agent
├── sysusers.d
│   └── dbus.conf
├── tmpfiles.d
│   └── dbus.conf
└── vlc
    ├── libcompat.a
    ├── libvlc_pulse.so -> libvlc_pulse.so.0.0.0
    ├── libvlc_pulse.so.0 -> libvlc_pulse.so.0.0.0
    ├── libvlc_pulse.so.0.0.0
    ├── libvlc_vdpau.so -> libvlc_vdpau.so.0.0.0
    ├── libvlc_vdpau.so.0 -> libvlc_vdpau.so.0.0.0
    ├── libvlc_vdpau.so.0.0.0
    ├── libvlc_xcb_events.so -> libvlc_xcb_events.so.0.0.0
    ├── libvlc_xcb_events.so.0 -> libvlc_xcb_events.so.0.0.0
    ├── libvlc_xcb_events.so.0.0.0
    ├── plugins
    │   ├── access
    │   │   └── libfilesystem_plugin.so
    │   ├── audio_filter
    │   │   ├── libnormvol_plugin.so
    │   │   ├── libscaletempo_pitch_plugin.so
    │   │   └── libscaletempo_plugin.so
    │   ├── audio_output
    │   │   ├── libadummy_plugin.so
    │   │   ├── libafile_plugin.so
    │   │   ├── libalsa_plugin.so
    │   │   ├── libamem_plugin.so
    │   │   ├── libjack_plugin.so
    │   │   └── libpulse_plugin.so
    │   ├── codec
    │   │   └── libavcodec_plugin.so
    │   ├── control
    │   │   ├── libdbus_plugin.so
    │   │   ├── libdummy_plugin.so
    │   │   ├── libgestures_plugin.so
    │   │   ├── libhotkeys_plugin.so
    │   │   ├── libmotion_plugin.so
    │   │   ├── libnetsync_plugin.so
    │   │   ├── liboldrc_plugin.so
    │   │   └── libxcb_hotkeys_plugin.so
    │   ├── demux
    │   │   └── libts_plugin.so
    │   ├── packetizer
    │   │   ├── libpacketizer_mpeg4audio_plugin.so
    │   │   └── libpacketizer_mpeg4video_plugin.so
    │   ├── plugins.dat
    │   ├── stream_filter
    │   │   └── libcache_read_plugin.so
    │   ├── video_chroma
    │   │   └── libswscale_plugin.so
    │   ├── video_filter
    │   │   └── libdeinterlace_plugin.so
    │   └── video_output
    │       └── libvmem_plugin.so
    └── vlc-cache-gen

44 directories, 711 files
