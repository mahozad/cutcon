# Good explanation of rpath/runpath and $ORIGIN:
# https://unix.stackexchange.com/a/22999

# To install libraries/programs using apt or apt-get, we need to use sudo
# and, it in turn, needs the user password which is configured to be read
# from standard input using the -S option and .setStandardInput(System.in)
# See https://stackoverflow.com/q/21659637
sudo -S apt install chrpath

chrpath -r '$ORIGIN' ./libvlc.so

# Optional step
# (removing this step does not seem to affect anything but the
# rpath of the files in plugins/ will be an absolute non-existent path)
find ./vlc/plugins/ -type f -name "*.so*" | xargs -n1 chrpath -r '$ORIGIN/../../..'
