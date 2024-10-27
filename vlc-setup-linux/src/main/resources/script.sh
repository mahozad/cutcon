sudo -S chrpath -r '$ORIGIN' ./usr/lib/libvlc.so

find ./usr/lib/vlc/plugins/ -type f -name "*.so*" | sudo -S xargs -n1 chrpath -r '$ORIGIN/../../..'
