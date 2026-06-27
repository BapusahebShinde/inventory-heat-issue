# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment
#-keep public class * extends android.support.v4.app.DialogFragment

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn java.lang.management.ThreadMXBean

-ignorewarnings

-keep class * {
    public private *;
}

-keep class * extends java.lang.annotation.Annotation { *; }

-keepclasseswithmembers class * {native <methods>;}