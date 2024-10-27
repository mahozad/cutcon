# Good explanation of rpath/runpath and $ORIGIN:
# https://unix.stackexchange.com/a/22999

sudo -S apt install chrpath

chrpath -r '$ORIGIN' ./usr/lib/libvlc.so

# Optional step
# (removing this step does not seem to affect anything but the
# rpath of the files in plugins/ will be an absolute non-existent path)
find ./usr/lib/vlc/plugins/ -type f -name "*.so*" | xargs -n1 chrpath -r '$ORIGIN/../../..'

# Exclude libvlc and libvlccore because compressing them seems to break something
#find ./usr/lib/vlc/plugins/ -name "*.so*" | xargs -n1 ../upx
