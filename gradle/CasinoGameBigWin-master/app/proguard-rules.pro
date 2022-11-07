

-optimizationpasses 30
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-mergeinterfacesaggressively
-optimizations !code/simplification/arithmetic
-dontusemixedcaseclassnames
-allowaccessmodification
-useuniqueclassmembernames
-keeppackagenames doNotKeepAThing


-keep public class com.android.installreferrer.** { *; }
-keep class com.appsflyer.** { *; }
    -keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();  }

 -keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
        public static final *** NULL;      }

    -keepnames @com.google.android.gms.common.annotation.KeepName class *
    -keepclassmembernames class * {
        @com.google.android.gms.common.annotation.KeepName *;
    }
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.**{ *; }
-keep interface com.google.android.gms.** { *; }
    -keepnames class * implements android.os.Parcelable {
        public static final ** CREATOR;
    }

    -dontwarn rx.**

    -dontwarn okio.**

    -dontwarn com.squareup.okhttp.**
    -keep class com.squareup.okhttp.** { *; }
    -keep interface com.squareup.okhttp.** { *; }

    -dontwarn retrofit.**
    -dontwarn retrofit.appengine.UrlFetchClient
    -keep class retrofit.** { *; }
    -keepclasseswithmembers class * {
        @retrofit.http.* <methods>;
    }

    -keepattributes Signature
    -keepattributes *Annotation*

-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**

-dontwarn com.badlogic.gdx.**
-dontnote com.badlogic.gdx.**

-keepclassmembers class com.badlogic.gdx.graphics.Cubemap* { *; }
-keepclassmembers class com.badlogic.gdx.graphics.GL* { *; }
-keep class * extends android.support.v4.app.Fragment{}
# for Android backend
-keepclassmembers class com.badlogic.gdx.backends.android.AndroidInput* {
    <init>(com.badlogic.gdx.Application, android.content.Context, java.lang.Object, com.badlogic.gdx.backends.android.AndroidApplicationConfiguration);
}

# for box2d plugin
-keepclassmembers class com.badlogic.gdx.physics.box2d.World {
    boolean contactFilter(long, long);
    void    beginContact(long);
    void    endContact(long);
    void    preSolve(long, long);
    void    postSolve(long, long);
    boolean reportFixture(long);
    float   reportRayFixture(long, float, float, float, float, float);
}

-dontwarn com.onesignal.**

# These 2 methods are called with reflection.
-keep class com.google.android.gms.common.api.GoogleApiClient {
    void connect();
    void disconnect();
}

# Need to keep as these 2 methods are called with reflection from com.onesignal.PushRegistratorFCM
-keep class com.google.firebase.iid.FirebaseInstanceId {
    static com.google.firebase.iid.FirebaseInstanceId getInstance(com.google.firebase.FirebaseApp);
    java.lang.String getToken(java.lang.String, java.lang.String);
}

-keep class com.onesignal.ActivityLifecycleListenerCompat** {*;}

# Observer backcall methods are called with reflection
-keep class com.onesignal.OSSubscriptionState {
    void changed(com.onesignal.OSPermissionState);
}

-keep class com.onesignal.OSPermissionChangedInternalObserver {
    void changed(com.onesignal.OSPermissionState);
}

-keep class com.onesignal.OSSubscriptionChangedInternalObserver {
    void changed(com.onesignal.OSSubscriptionState);
}

-keep class com.onesignal.OSEmailSubscriptionChangedInternalObserver {
    void changed(com.onesignal.OSEmailSubscriptionState);
}

-keep class com.onesignal.OSSMSSubscriptionChangedInternalObserver {
    void changed(com.onesignal.OSSMSSubscriptionState);
}

-keep class ** implements com.onesignal.OSPermissionObserver {
    void onOSPermissionChanged(com.onesignal.OSPermissionStateChanges);
}

-keep class ** implements com.onesignal.OSSubscriptionObserver {
    void onOSSubscriptionChanged(com.onesignal.OSSubscriptionStateChanges);
}

-keep class ** implements com.onesignal.OSEmailSubscriptionObserver {
    void onOSEmailSubscriptionChanged(com.onesignal.OSEmailSubscriptionStateChanges);
}

-keep class ** implements com.onesignal.OSSMSSubscriptionObserver {
    void onOSEmailSubscriptionChanged(com.onesignal.OSSMSSubscriptionStateChanges);
}

-keep class com.onesignal.shortcutbadger.impl.AdwHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.ApexHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.AsusHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.DefaultBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.EverythingMeHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.HuaweiHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.LGHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.NewHtcHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.NovaHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.OPPOHomeBader { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.SamsungHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.SonyHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.VivoHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.XiaomiHomeBadger { <init>(...); }
-keep class com.onesignal.shortcutbadger.impl.ZukHomeBadger { <init>(...); }


-dontwarn com.amazon.**

-dontwarn com.huawei.**

# Proguard ends up removing this class even if it is used in AndroidManifest.xml so force keeping it.
-keep public class com.onesignal.ADMMessageHandler {*;}

-keep public class com.onesignal.ADMMessageHandlerJob {*;}

# OSRemoteNotificationReceivedHandler is an interface designed to be extend then referenced in the
#    app's AndroidManifest.xml as a meta-data tag.
# This doesn't count as a hard reference so this entry is required.
-keep class ** implements com.onesignal.OneSignal$OSRemoteNotificationReceivedHandler {
   void remoteNotificationReceived(android.content.Context, com.onesignal.OSNotificationReceivedEvent);
}

-keep class com.onesignal.JobIntentService$* {*;}

-keep class com.onesignal.OneSignalUnityProxy {*;}

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
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

##---------------End: proguard configuration for Gson  ----------