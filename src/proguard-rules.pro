-keep class kotlinx.coroutines.** { *; }
# See https://github.com/JetBrains/compose-multiplatform/issues/4391
-keep class androidx.compose.runtime.** { *; }

# Our own code is less than 1 MB; keep it all
-keep class ir.mahozad.cutcon.** { *; }
-keep class org.jaudiotagger.tag.** { *; }
-keep class ch.qos.logback.** { *; }
-keep class uk.co.caprica.** { *; }
-keep class org.bytedeco.** { *; }
-keep class com.sun.jna.** { *; }

# See https://github.com/bytedeco/javacv/wiki/Configuring-Proguard-for-JavaCV
-dontwarn org.bytedeco.**
# For the code dealing with VarHandle in media player
-dontwarn java.lang.invoke.**
-dontwarn org.apache.**
-dontwarn ch.qos.logback.**
-dontwarn kotlinx.datetime.**
# For KotlinLogging library version 7.0.7
-dontwarn com.oracle.svm.core.annotate.**

# Obfuscation breaks coroutines/ktor for some reason
-dontobfuscate
#-dontoptimize
#-dontshrink
#-dontpreverify
