# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\mpost\AppData\Local\Android\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:

-keep class com.eclipsesource.tabris.android.internal.javascript.Console* { *; }
-keep class com.eclipsesource.tabris.android.internal.javascript.V8JavaScriptBridge { *; }
-keep class com.eclipsesource.tabris.android.internal.javascript.JavaScriptApp { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.IActivityResultListener { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.IKeyEventListener { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.IDispatchKeyEventListener { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.IOptionsMenuListener { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.ITouchEvenListener { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.IAndroidWidgetToolkit { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.IRequestPermissionResultListener { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.IStartActivityForResultListener { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.IAppStateListener { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.AppState { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.view.FocusTrackingListener { *; }
-keep class com.eclipsesource.tabris.android.internal.javascript.JavaScriptMessageSender { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.ImageProvider { *; }
-keep class com.eclipsesource.tabris.android.internal.RequestCodePool { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.util.PersistentCookieStore { *; }
-keep class com.eclipsesource.tabris.android.internal.toolkit.util.SerializableHttpCookie { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:

-keepclassmembers class com.eclipsesource.tabris.android.internal.toolkit.view.ScriptResultJsInterface, com.eclipsesource.tabris.android.internal.toolkit.view.TabrisWindowJsInterface { public *; }

-keep class !com.eclipsesource.tabris.android.internal.**,
            !com.facebook.stetho.**,
            !com.squareup.picasso.**,
            !com.squareup.okhttp.**,
            !com.jakewharton.picasso.**,
            !okhttp3.**,
            !okio.**,
            !com.google.common.**
            { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.google.common.**
-dontwarn com.squareup.picasso.**