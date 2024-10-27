sudo -S chrpath -r '$ORIGIN' ./usr/lib/libvlc.so

sudo -S chrpath -r '$ORIGIN/..' ./usr/lib/vlc/libvlc_pulse.so.0.0.0
sudo -S chrpath -r '$ORIGIN/..' ./usr/lib/vlc/libvlc_xcb_events.so.0.0.0

find ./usr/lib/vlc/plugins/ -type f -name "*.so*" | sudo -S xargs -n1 chrpath -r '$ORIGIN/../../..'

cp -r ./usr/lib/x86_64-linux-gnu/* ./usr/lib/
rm -r ./usr/lib/x86_64-linux-gnu/
