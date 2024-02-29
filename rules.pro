-keep class kotlinx.coroutines.** { *; }

# Our own code is less than 1 MB; keep it all
-keep class ir.mahozad.cutcon.** { *; }
-keep class org.jaudiotagger.tag.** { *; }
-keep class ch.qos.logback.** { *; }
-keep class uk.co.caprica.** { *; }
-keep class org.bytedeco.** { *; }
-keep class com.sun.jna.** { *; }

# See https://github.com/bytedeco/javacv/wiki/Configuring-Proguard-for-JavaCV
-dontwarn org.bytedeco.**
-dontwarn org.apache.**
-dontwarn ch.qos.logback.**
-dontwarn kotlinx.datetime.**
-dontwarn io.github.oshai.kotlinlogging.coroutines.**

# Obfuscation breaks coroutines/ktor for some reason
-dontobfuscate
# Optimization breaks Compose classes for some reason
-dontoptimize
#-dontshrink
#-dontpreverify
