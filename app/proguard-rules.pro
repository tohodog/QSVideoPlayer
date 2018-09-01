# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Develop\Android SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5          # 指定代码的压缩级别
-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontpreverify           # 混淆时是否做预校验
-verbose                # 混淆时是否记录日志
-dontskipnonpubliclibraryclasses
-dontoptimize
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法


#===================Android通用====================

-keepattributes *Annotation*
#避免混淆泛型 如果混淆报错建议关掉
-keepattributes Signature
# 抛出异常时保留代码行号
-keepattributes SourceFile, LineNumberTable
#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}
#默认会忽略xml引用的类的
#-keep public class * extends android.view.View
#-keep public class * extends android.app.Appliction
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
-keep class * extends android.app.Activity {
   public void *(android.view.View);
}
#v4 v7包不混淆 [混淆好像也没事
-dontwarn android.support.**
-keep class android.support.** { *; }


#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#保持 Serializable 不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#===================Android通用===================


#==========videoplayer==========
-dontwarn org.song.videoplayer.**
-keep class org.song.videoplayer.** { *; }

-dontwarn tv.danmaku.ijk.**
-keep class tv.danmaku.ijk.** { *; }

-dontwarn com.google.android.**
-keep class com.google.android.** { *; }
#==========videoplayer==========

