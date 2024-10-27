cd ./usr/lib/ # == <ROOT_DIR>/usr/lib/
sudo chrpath -r '$ORIGIN' libvlc.so
cd ./vlc/ # == <ROOT_DIR>/usr/lib/vlc/
sudo chrpath -r '$ORIGIN/..' libvlc_pulse.so.0.0.0
sudo chrpath -r '$ORIGIN/..' libvlc_xcb_events.so.0.0.0
cd ./plugins/ # == <ROOT_DIR>/usr/lib/vlc/plugins/
find . -name "*.so*" | sudo xargs -n1 chrpath -r '$ORIGIN/../../..'
cd ../../ # == <ROOT_DIR>/usr/lib/

# cp -r ./* <PROJECT_DIR>/ # from <ROOT_DIR>/usr/lib/ to <PROJECT_DIR>/
# cp -r <PROJECT_DIR>/x86_64-linux-gnu/* <PROJECT_DIR>/
