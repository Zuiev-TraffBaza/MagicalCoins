package magic.game.coinss.services

import android.content.Context
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import magic.game.coinss.SDK_TAG
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ConversionListener(
    val onConversionDataSuccessAction: (MutableMap<String, Any>?) -> Unit = {},
    val onConversionDataFailAction: (String?) -> Unit = {},
    val onAppOpenAttributionAction: (MutableMap<String, String>?) -> Unit = {},
    val onAttributionFailureAction: (String?) -> Unit = {},
) : AppsFlyerConversionListener {
    override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
        Log.d(SDK_TAG, "onConversionDataSuccess: $data")
        onConversionDataSuccessAction(data)
    }

    override fun onConversionDataFail(data: String?) {
        Log.d(SDK_TAG, "onConversionDataFail: $data")
        onConversionDataFailAction(data)
    }

    override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
        Log.d(SDK_TAG, "onAppOpenAttribution: $data")
        onAppOpenAttributionAction(data)
    }

    override fun onAttributionFailure(data: String?) {
        Log.d(SDK_TAG, "onAttributionFailure $data")
        onAttributionFailureAction(data)
    }

}

object AppsflyerService {
    suspend fun initAppsflyer(context: Context, afDevKey: String): String = suspendCoroutine {
        if (afDevKey.isEmpty()) {
            Log.e(SDK_TAG, "Appsflyer is not initialized. Appsflyer dev key is Empty", )
            it.resume("")
            return@suspendCoroutine
        }
        AppsFlyerLib.getInstance().apply {
            init(afDevKey, ConversionListener(
                onConversionDataSuccessAction = { data ->
                    val campaign = data?.get("campaign").toString()
                    it.resume(campaign)
                },
                onConversionDataFailAction = { _ ->
                    val campaign = ""
                    it.resume(campaign)
                }
            ), context)
            setDebugLog(true)
            start(context)
        }
        Log.d(SDK_TAG, "Appsflyer Initialized")
    }

    fun getAppsflyerUID(context: Context): String {
        return AppsFlyerLib.getInstance().getAppsFlyerUID(context) ?: ""
    }
}