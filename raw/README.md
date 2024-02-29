## Splash screen
See the test in UiTest source set to generate the splash screen GIF.

## Demo images
Run the app and play the file available in git branch demo-video downloaded from pexels.com.

With FFmpeg 5.1 gpl:
ffmpeg.exe -f gdigrab -framerate 1 -i title="Cutcon" out.apng
ffmpeg.exe -i out.apng -r 1/1 "$file%03d".png

Select one of the extracted frames
Open it in Inkscape
Add a stroke with width = hairline around the picture
Export the whole document to PNG
Optimize with https://tinypng.com/

## Notification sounds
To play mp3 format audio in Java, see https://stackoverflow.com/tags/javasound/info

camera sound effects downloaded from https://freesound.org

The free *[pristine](notification-pristine.mp3)* sound is licensed under the Creative Commons Attribution license.
- https://notificationsounds.com/message-tones/pristine-609

Some other notification sounds:
1- https://notificationsounds.com/notification-sounds/definite-555
2- https://notificationsounds.com/free-jingles-and-logos/playful-notification
3- https://notificationsounds.com/message-tones/relax-message-tone
4- https://notificationsounds.com/notification-sounds/ringtone-you-would-be-glad-to-know

To convert the sounds to WAV format (supported by Java), use FFmpeg:

```shell
./ffmpeg -i sound.mp3 result.wav
```

## Ico files
.ico sizes for Windows: 16, 24, 32, 48, 256  
See https://learn.microsoft.com/en-us/windows/apps/design/style/iconography/app-icon-construction#:~:text=Apps%20should%20have%2C%20at%20the%20bare%20minimum%3A

If you encounter error with imagemagick, set the result file path to a non-protected directory like *Desktop*
(directories like *Program Files* and its subdirectories etc. are protected).

Create a high-res PNG (for example, 2048x2048) from the SVG and then convert the PNG to .ico with imagemagick
(it automatically creates the embedded 256 size in PNG format and the smaller ones in ICO format):

```shell
./magick logo.png -background transparent -define icon:auto-resize="16,20,24,32,40,48,64,256" C:/Users/Mahdi/Desktop/result.ico
```

OR:

```shell
./magick logo.png -background none -resize 256x256 -density 256x256 C:/Users/Mahdi/Desktop/result.ico
```

To inspect icon sizes:

```shell
./magick identify "C:/Users/Mahdi/Desktop/image.ico"
```

Here are icon sizes of exe files of a few programs
(icons extracted with https://www.nirsoft.net/utils/iconsext.html)
and inspected with imagemagick as described above:

| Application                                                                                                                                                                                  | 16  | 20  | 24  | 32  | 40  | 48  | 60  | 64  | 72  | 80  | 96  | 256 |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|
| <img src="https://github.com/mahozad/mahozad/blob/master/stackoverflow/icons/chrome.ico?raw=true" width="12"/> [Google Chrome 106](https://www.google.com/chrome/)                           | ✓   |     |     | ✓   |     | ✓   |     |     |     |     |     | ✓   |
| <img src="https://github.com/mahozad/mahozad/blob/master/stackoverflow/icons/idea.ico?raw=true" width="12"/> [IntelliJ IDEA 2022.2.3](https://www.jetbrains.com/idea/)                       | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   |     | ✓   |     |     |     | ✓   |
| <img src="https://github.com/mahozad/mahozad/blob/master/stackoverflow/icons/powertoys.ico?raw=true" width="12"/> [MS PowerToys 0.63.0](https://github.com/microsoft/PowerToys)              | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   |     | ✓   |     |     |     | ✓   |
| <img src="https://github.com/mahozad/mahozad/blob/master/stackoverflow/icons/mspaint.ico?raw=true" width="12"/> [MS Paint 11.2208.6.0](https://en.wikipedia.org/wiki/Microsoft_Paint)        | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   |     | ✓   |     |     |     | ✓   |
| <img src="https://github.com/mahozad/mahozad/blob/master/stackoverflow/icons/taskmgr.ico?raw=true" width="12"/> [MS Task Manager 10.0](https://en.wikipedia.org/wiki/Task_Manager_(Windows)) | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   |     | ✓   |     |     |     | ✓   |
| <img src="https://github.com/mahozad/mahozad/blob/master/stackoverflow/icons/msword.ico?raw=true" width="12"/> [MS Word 2021](https://www.microsoft.com/en-ww/microsoft-365/word)            | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   |
| <img src="https://github.com/mahozad/mahozad/blob/master/stackoverflow/icons/vstudio.ico?raw=true" width="12"/> [MS Visual Studio 2022](https://visualstudio.microsoft.com/)                 | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   | ✓   |

Notes:
  1. No app has included the `128` size
  2. The `256` size is `PNG` format; others are `ICO`
  3. `MS` is short for *Microsoft*

Also,
see https://stackoverflow.com/a/74392449
and https://stackoverflow.com/q/3236115
and https://stackoverflow.com/q/11423711
and https://developer.apple.com/design/human-interface-guidelines/foundations/app-icons/
and https://gist.github.com/azam/3b6995a29b9f079282f3
