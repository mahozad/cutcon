sudo -S apt install chrpath

chrpath -r '$ORIGIN' ./usr/lib/libvlc.so

# Not needed
# find ./usr/lib/vlc/plugins/ -type f -name "*.so*" | xargs -n1 chrpath -r '$ORIGIN/../../..'
