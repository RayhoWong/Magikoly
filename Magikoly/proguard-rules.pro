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

-optimizationpasses 5
#-dontskipnonpubliclibraryclasses
#-dontskipnonpubliclibraryclassmembers
#-dontpreverify
-dontwarn
-ignorewarnings
#-dontoptimize
#-verbose
#-keepattributes Exceptions,SourceFile,LineNumberTable

#-dontwarn android.support.**
#-dontwarn com.loopme.**
#-dontwarn com.facebook.ads.**
#-dontwarn google.android.gms.ads.**
#-dontwarn com.google.ads.**
#-dontwarn android.app.usage.**

#-keepattributes *Annotation*
#-keep class * extends java.lang.annotation.Annotation {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}

###### 第三方jar包不混淆
-keepattributes Signature
-keep class com.facebook.** { *;}
-keep class com.google.android.apps.analytics.** { *;}
-keep class com.nineoldandroids.** { *;}
-keep class android.support.v4.** { *;}
-keep class com.google.ads.** { *;}
-keep class com.nostra13.universalimageloader.** { *;}
-keep class com.yandex.** { *;}
-keep class com.zen.** { *;}


-keep class com.android.internal.app.** { *;}
-keep public class * extends com.android.internal.app.AlertActivity

-keep public class mobi.intuitit.android.content.*
-keep public class mobi.intuitit.android.widget.*

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-dontwarn android.webkit.*

-keepclassmembers class * implements java.lang.reflect.InvocationHandler {
	private java.lang.Object *(java.lang.Object, java.lang.reflect.Method, java.lang.Object[]);
}
#虚拟机2.0 end

#广告SDK Begin#
 -keep public class com.monet.** {
  public protected *;
  native <methods>;
 }
  -keep class com.monet.bidder** { *; }
  -keepclasseswithmembernames class com.monet.bidder** { *; }
   -keepclasseswithmembernames public class android.support.v4.content.ContextCompat{ *; }
#广告SDK End#

#loopme sdk
-keep public class com.loopme.* {*;}

#GMS ADS
-assumenosideeffects class com.google.android.gms.ads.internal.ClientApi {
    public static void retainReference(...);
}
-keep class org.apache.** {*;}
-keep class com.google.ads.mediation.admob.AdMobAdapter {*;}
-keep class com.google.ads.mediation.AdUrlAdapter {*;}
-keep public class com.google.android.gms.** {*;}
-keep public class com.google.android.gms.ads.AdView {*;}
-keep public class com.google.android.gms.ads.InterstitialAd {*;}
-keep public class com.google.android.gms.ads.formats.NativeContentAd {*;}
-keep public class com.google.android.gms.ads.formats.NativeAppInstallAd {*;}

#mobilecore sdk
#-keepattributes InnerClasses, EnclosingMethod
#-keep class com.ironsource.mobilcore.**{ *; }

#-keepattributes Exceptions,InnerClasses,...

#Android标注代码不混淆
-keep class android.annotation.** { *; }
#support v4 v7 代码不混淆
-keep class android.support.** { *; }
-keep interface android.support.** { *; }

#第3方sdk混淆配置===BEGIN===========
#google play service sdk
-keep public class com.google.ads.** {*;}
#-keep public class com.google.android.gms.** {*;}

#微服务sdk接入
-keep class com.base.services.**{*;}
-keep class com.base.http.**{*;}

#Firebase==============begin============================
# Realm
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class * { *; }
-dontwarn javax.**
-dontwarn io.realm.**
#
#Firebase==============end==============================

#EventBus配置====begin====================
-keep public class org.greenrobot.eventbus.**{*;}
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
-keep class * extends com.glt.magikoly.event.BaseEvent{*;}
#EventBus配置====end====================

# Support for Android Advertiser ID.
-keep class com.google.android.gms.common.GooglePlayServicesUtil {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {*;}
# Support for Google Play Services
# http://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
#推送SDK混淆配置===END=============

#Firebase==============begin============================
-keep class com.google.firebase.** { *; }

# keep everything in this package from being renamed only
-keepnames class com.google.firebase.** { *; }
# keep the class and specified members from being removed or renamed
-keep class com.google.firebase.crash.FirebaseCrash { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.google.firebase.crash.FirebaseCrash { *; }

# keep the class and specified members from being renamed only
-keepnames class com.google.firebase.crash.FirebaseCrash { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.google.firebase.crash.FirebaseCrash { *; }
#Firebase==============end============================

-keep class * extends android.support.v4.view.PagerAdapter{*;}

#MobPower配置====begin==================
 -keepattributes Signature
 -keepattributes *Annotation*
 -keep class com.mobpower.ad.appwall.ui.** {*; }
 -keep class com.mobpower.ad.common.ui.** {*; }
 -keep class com.mobpower.ad.video.ui.** {*; }
 -keep class com.mobpower.ad.splash.activity.** {*; }
 -keep class com.power.PowerService {*; }
 -keep class com.power.PowerReceiver {*; }
 -keepclassmembers class * implements android.os.Parcelable {
 public static final ** CREATOR;
 }
#MobPower配置====end====================


#---------------------Glide start----------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#--------------------Glide end---------------------------

#--------------------Volley start-----------------------
-dontwarn com.android.volley.**
-dontwarn com.android.volley.error.**
-keep class com.android.volley.** { *; }
-keep class com.android.volley.toolbox.** { *; }
-keep class com.android.volley.Response$* { *; }
-keep class com.android.volley.Request$* { *; }
-keep class com.android.volley.RequestQueue$* { *; }
-keep class com.android.volley.toolbox.HurlStack$* { *; }
-keep class com.android.volley.toolbox.ImageLoader$* { *; }
-keep interface com.android.volley.** { *; }
#-------------------Volley end-----------------------------

#-------------------OkHttp start-------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
#-------------------OkHttp end----------------------------


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

#crashlytics
-keep class com.crashlytics.android.** { *; }
-keepnames class com.crashlytics.android.** { *; }
-keep class com.crashlytics.android.Crashlytics { *; }
-keepclassmembers class com.crashlytics.android.Crashlytics { *; }
-keepnames class com.crashlytics.android.Crashlytics { *; }
-keepclassmembernames class com.crashlytics.android.Crashlytics { *; }


#第3方sdk混淆配置===BEGIN===========
#facebook sdk
-keep public class com.facebook.ads.** {*;}
#loopme sdk
-keep public class com.loopme.** {*;}
#appmonet SDK混淆
-keep public class com.monet.** {
   public protected *;
   native <methods>;
}
#MoPub SDK混淆
# Explicitly keep any custom event classes in any package.
-keep class * extends com.mopub.nativeads.CustomEventRewardedAd {}

#第3方sdk混淆配置===END===========

##aerserv
-keep class com.aerserv.**{*;}
-keepclassmembers class com.aerserv.** {*;}
#adcolony
-keep class com.adcolony.**{*;}
# Keep ADCNative class members unobfuscated
-keepclassmembers class com.adcolony.sdk.ADCNative** {
 *;
 }
-dontwarn com.aerserv.**
-dontwarn com.adcolony.**

-keepclassmembers class * {
 @android.webkit.JavascriptInterface <methods>;
}


#inmobi===BEGIN===========
-keepattributes SourceFile,LineNumberTable
-keep class com.inmobi.** { *; }
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
     public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
     public *;
}
# skip the Picasso library classes
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
# skip Moat classes
-keep class com.moat.** {*;}
-dontwarn com.moat.**
# For old ads classes
-keep public class com.google.ads.**{
   public *;
}
# For mediation
-keepattributes *Annotation*
# For Google Play services
-keep public class com.google.android.gms.ads.**{
   public *;
}
#inmobi===END===========

#smaato===BEGIN===========
-keep class com.smaato.**{*;}
-dontwarn com.smaato.soma.SomaUnityPlugin*
-dontwarn com.millennialmedia**
-dontwarn com.facebook.**

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
#smaato===END===========

#doubleclick===STRAT===========
-keep class com.doubleclick.**{*;}
-keepclassmembers class com.doubleclick.** {*;}
#doubleclick===END===========

#google play service sdk
-keep public class com.google.ads.** {*;}
-keep public class com.google.android.gms.** {*;}
#facebook sdk
-keep public class com.facebook.ads.** {*;}
#Chartboost sdk
-keep class com.chartboost.** { *; }
#Vungle sdk
-dontwarn com.vungle.**
-keep class com.vungle.** { *; }
-keep class javax.inject.*

#MoPub SDK混淆
#-Keep public classes and methods.
-keepclassmembers class com.mopub.** { public *; }
-keep public class com.mopub.**
-keep public class android.webkit.JavascriptInterface {}
#-Explicitly keep any custom event classes in any package.
-keep class * extends com.mopub.mobileads.CustomEventBanner {}
-keep class * extends com.mopub.mobileads.CustomEventInterstitial {}
-keep class * extends com.mopub.nativeads.CustomEventNative {}
-keep class * extends com.mopub.nativeads.CustomEventRewardedAd {}
#-Keep methods that are accessed via reflection
-keepclassmembers class ** { @com.mopub.common.util.ReflectionTarget *; }
#Unity sdk
-keep class com.unity3d.** { *; }
#Applovin sdk
-keep class com.applovin.** { *; }
#广告sdk新增混淆配置
-keep public class com.mediation.**{*;}

#亚马逊混淆配置===BEGIN===========
-keep class com.amazonaws.**{*;}
-dontwarn com.amazonaws.**
#亚马逊混淆配置===END===========
-keep class com.glt.magikoly.bean.**{*;}

-keep class com.megvii.facepp.sdk.**{*;}
-keepnames class com.megvii.facepp.sdk.**{*;}


-keep public class com.glt.magikoly.apng.** {public *; protected *;}
-keep public class net.ellerton.japng.** { *; }

-keep class com.glt.magikoly.data.DatabaseHelper{
  public void onDowngrade(android.database.sqlite.SQLiteDatabase, int, int);
  public boolean onUpgradeDB*To*(...);
}

-keep class okhttp3.OkHttpClient{*;}

#友盟 start
-keep class com.umeng.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep public class magikoly.magiccamera.R$*{
public static final int *;
}
#友盟 end

#Bugly start
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
#Bugly end

#广告sdk
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.ss.sys.ces.* {*;}
