package magic.game.coinss.services

import android.content.Context
import android.util.Log
import android.webkit.WebSettings
import com.appsflyer.AppsFlyerLib
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import magic.game.coinss.MainActivity
import magic.game.coinss.SDK_TAG
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object OneSignalService {

    fun initOneSignal(
        context: Context,
        appId: String,
        uuid: String,
        advertisingId: String,
        notificationClickUrl: String
    ) {
        if (appId.isEmpty()) {
            Log.e(SDK_TAG, "OneSignal Not Initialized. Application ID is empty")
        }
        OneSignal.unsubscribeWhenNotificationsAreDisabled(true)
        OneSignal.initWithContext(context)
        OneSignal.setAppId(appId)
        OneSignal.setExternalUserId(uuid)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.setNotificationOpenedHandler {
            onNotificationOpened(
                result = it,
                context = context,
                advertisingId = advertisingId,
                url = notificationClickUrl
            )
        }
        Log.d(SDK_TAG, "OneSignal Initialized")
    }

    private fun onNotificationOpened(
        result: OSNotificationOpenedResult,
        context: Context,
        advertisingId: String,
        url: String,
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d(SDK_TAG, "Result: ${result.notification}")
            val jsonSignal = JSONObject()
            jsonSignal.put("push_id", result.notification.notificationId)
            jsonSignal.put("add_id", advertisingId)
            jsonSignal.put(
                "af_dev",
                AppsFlyerLib.getInstance().getAppsFlyerUID(context) ?: ""
            )
            jsonSignal.put("data", result.notification.additionalData.toString())
            jsonSignal.put("bundle", magic.game.coinss.BuildConfig.APPLICATION_ID)

            Log.d(SDK_TAG, "Json Signal: $jsonSignal")
            val body: RequestBody =
                jsonSignal.toString().toRequestBody("application/json".toMediaType())
          MainActivity.OkHttpCustomClient
                .setOkHttpClient(WebSettings.getDefaultUserAgent(context))
                .newCall(
                    Request.Builder()
                        .url(url)
                        .post(body)
                        .build()
                )
                .execute()
            Log.d(SDK_TAG, "Push was data successfully sent")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(SDK_TAG, "Push was opened, but data not sent. Message: ${e.message}")
        }

    }
}