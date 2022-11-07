package com.big.win.casino.games.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private var toastMessage : Toast? = null
val mainScope = CoroutineScope(Job() + Dispatchers.IO)


fun FirebaseAnalytics.logFragment(fragment : Fragment){
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, fragment.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, fragment.javaClass.simpleName)
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
}

fun logEvent(eventName: String, applicationContext : Context){

        // log firebase
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, eventName)
        FirebaseAnalytics.getInstance(applicationContext).logEvent(eventName, bundle)


        //log appsflyer
        val eventValues = HashMap<String, Any>()
        eventValues["af_visitor_id"]  = applicationContext.getSharedPreferences("save", 0)?.getString("visitor_id", "no_visitor_id") ?: "no_visitor_id"
        AppsFlyerLib.getInstance().logEvent(applicationContext , eventName, eventValues)
}


suspend fun Activity.getInstallReferrer() =  suspendCoroutine<String> {
        val referrerClient: InstallReferrerClient = InstallReferrerClient.newBuilder(this).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(response: Int) {
                        if(response == InstallReferrerClient.InstallReferrerResponse.OK){
                                val referrer = referrerClient.installReferrer.installReferrer
                                it.resume(referrer)
                        } else {
                                it.resume("")
                        }
                }
                override fun onInstallReferrerServiceDisconnected() = it.resume("")
        })
}
suspend fun Activity.getAppsflyer(afKey: String, context: Context) = suspendCoroutine<String> {

        AppsFlyerLib.getInstance().enableFacebookDeferredApplinks(false)
        AppsFlyerLib.getInstance().init(afKey, object :
                AppsFlyerConversionListener {
                override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>?) {
                        Log.d("testretest", conversionData.toString())

                        val campaign  = conversionData?.get("campaign") ?: ""
                        it.resume(campaign.toString())

                }

                override fun onConversionDataFail(p0: String?) {
                        it.resume("")
                }
                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {}
                override fun onAttributionFailure(p0: String?) {
                }

                //Может быть неверный контекст
        }, this)
        AppsFlyerLib.getInstance().start(context)
}



fun JSONObject.isNotNull(s: String): Boolean = !this.isNull(s)







