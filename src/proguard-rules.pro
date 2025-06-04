-keep class ch.qos.logback.** { *; }
-keep class uk.co.caprica.** { *; }
-keep class org.bytedeco.** { *; }
-keep class com.sun.jna.** { *; }
-keep class org.jaudiotagger.tag.** { *; }
# Our own code is less than 1 MB; keep it all
-keep class ir.mahozad.cutcon.** { *; }

# To suppress notes about duplicate META-INF/LICENCE etc.
# See https://github.com/Guardsquare/proguard/issues/465
-dontnote "module-info"
-dontnote "META-INF**"

-dontwarn ch.qos.logback.**
-dontwarn io.github.oshai.kotlinlogging.coroutines.**
-dontwarn com.oracle.svm.core.annotate.**
# See https://github.com/bytedeco/javacv/wiki/Configuring-Proguard-for-JavaCV
-dontwarn org.bytedeco.**
# For the code dealing with VarHandle in media player
-dontwarn java.lang.invoke.**
# For KotlinLogging library version 7.0.7
-dontwarn org.apache.**

# We don't need code obfuscation
-dontobfuscate
#-dontoptimize
#-dontshrink
#-dontpreverify
