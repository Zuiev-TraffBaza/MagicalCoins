package com.big.win.casino.games.main

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.appsflyer.AppsFlyerLib
import com.big.win.casino.games.BuildConfig
import com.big.win.casino.games.R
import com.big.win.casino.games.databinding.FragmentSplashBinding
import com.big.win.casino.games.util.*
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.onesignal.OneSignal
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


@ExperimentalCoroutinesApi
class SplashFragment : Fragment() {
    companion object{
        const val START_URL = "http://tb-int-site.pp.ua/"
        const val AF_KEY = "iqu4KZGw8eqAEu85xnRnXD"
    }
    private lateinit var binding : FragmentSplashBinding
    private lateinit var sharedPrefs : SharedPreferences
    private lateinit var viewModel : MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPrefs = requireActivity().getSharedPreferences("save", 0)
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel


        binding.imageViewLoader.setImageResource(R.mipmap.ic_launcher)

        startAnimation()


        val referrerJob = mainScope.async(Dispatchers.IO) {
            requireActivity().getInstallReferrer()
        }
        val appsflyerJob = mainScope.async(Dispatchers.IO) {
            requireActivity().getAppsflyer(AF_KEY, requireContext())
        }
        if(isOnline()){
            val advertisingIdJob = mainScope.async(Dispatchers.IO) {
                var adid = sharedPrefs.getString("adid", "")
                if (adid?.isEmpty() == true) {
                    adid = AdvertisingIdClient.getAdvertisingIdInfo(requireContext()).id ?: ""
                    sharedPrefs.edit().putString("adid", adid).apply()
                }
                return@async adid ?: ""
            }

            if (sharedPrefs.getBoolean("user", true)) {
                mainScope.launch(Dispatchers.IO) {
                    awaitAll(advertisingIdJob,  referrerJob, appsflyerJob)
                    launch(Dispatchers.Main){
                        setOnesignal(advertisingIdJob.getCompleted())
                    }
                    if(sharedPrefs.getString("savedUrl", "")?.isNotEmpty() == true){
                        sharedPrefs.getString("visitor_id", "")?.let {
                            OneSignal.setExternalUserId(it)
                        }
                        mainScope.launch(Dispatchers.Main) {
                            findNavController().navigate(R.id.navigation_main)
                        }
                    } else {
                        mainScope.launch(Dispatchers.IO) {

                            val ref = referrerJob.getCompleted()
                            val utmSource = ref.substring(ref.indexOf("=") + 1, ref.indexOf("&"));
                            val utmMedium = ref.substring(ref.lastIndexOf("=") + 1)
                            checkUser(
                                advertisingIdJob.getCompleted(),
                                appsflyerJob.getCompleted(),
                                utmSource,
                                utmMedium
                            )
                        }
                    }
                }
            } else findNavController().navigate(R.id.navigation_game)
        } else findNavController().navigate(R.id.navigation_game)

    }


    private fun checkUser(adid: String, campaign: String, utmSource : String, utmMedium : String){
        val firstJson = JSONObject()

        firstJson.put("bundle_id",  BuildConfig.APPLICATION_ID)
        firstJson.put("appsflyer_device_id", AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext()) ?: "")
        firstJson.put("advertising_id", adid)
        firstJson.put("campaign", campaign)
        firstJson.put("utm_source", utmSource)
        firstJson.put("utm_medium", utmMedium)

        val body: RequestBody = firstJson.toString().toRequestBody("application/json".toMediaType())

        val getDataJob = mainScope.async(Dispatchers.IO) {
            try {
                val buildUrl = "${START_URL}api/user/check/v3/"

                return@async OkHttpCustomClient
                    .getOkHttpClient()
                    .newCall(Request.Builder()
                        .url(buildUrl)
                        .post(body)
                        .build())
                    .execute()
                    .body
                    ?.string() ?: ""
            } catch (e : Exception){
                return@async ""
            }

        }
        mainScope.launch(Dispatchers.Main) {
            awaitAll(getDataJob)
            if(getDataJob.getCompleted().isNotEmpty()) {
                try {
                    val jsonResponse = JSONObject(getDataJob.getCompleted())
                    if (jsonResponse.isNotNull("url")) {
                         val visitorId = jsonResponse.getString("visitor_id")
                        OneSignal.setExternalUserId(visitorId)
                        sharedPrefs.edit().putString("visitor_id", visitorId).apply()
                        viewModel.siteUrl.postValue(Resource.Success(jsonResponse.getString("url")))
                        findNavController().navigate(R.id.navigation_main)
                    } else {
                        if (jsonResponse.getBoolean("is_first_engagement")) {
                            OneSignal.disablePush(true)
                            sharedPrefs.edit().putBoolean("user", false).apply()
                        }
                        launch(Dispatchers.Main){findNavController().navigate(R.id.navigation_game)}
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main){findNavController().navigate(R.id.navigation_game)}

                }
            } else {

                launch(Dispatchers.Main){findNavController().navigate(R.id.navigation_game)}
            }
        }
    }


    private fun setOnesignal(advertisingId: String){
        OneSignal.setAppId("198f953e-811c-4c36-a960-c831b5725eda")
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.setNotificationOpenedHandler {

            val jsonSignal = JSONObject()
            jsonSignal.put("push_id", it.notification.notificationId)
            jsonSignal.put("add_id", advertisingId)
            jsonSignal.put("af_dev", AppsFlyerLib.getInstance().getAppsFlyerUID(requireContext()) ?: "")
            jsonSignal.put("data", it.notification.additionalData.toString())
            jsonSignal.put("bundle", BuildConfig.APPLICATION_ID)
            val body: RequestBody = jsonSignal.toString().toRequestBody("application/json".toMediaType())

            mainScope.launch(Dispatchers.IO) {
                runCatching {
                    OkHttpCustomClient
                        .getOkHttpClient()
                        .newCall(Request.Builder()
                            .url("${START_URL}os/click")
                            .post(body)
                            .build())
                        .execute()
                }
            }
        }

        OneSignal.unsubscribeWhenNotificationsAreDisabled(true)
        OneSignal.initWithContext(requireContext().applicationContext)
        OneSignal.setAppId("198f953e-811c-4c36-a960-c831b5725eda")
    }

    private fun startAnimation() {
        val blinkAnimation = AnimationUtils.loadAnimation(requireActivity().applicationContext,
            R.anim.blink_slow_animation)
        binding.imageViewLoader.startAnimation(blinkAnimation)
    }


    override fun onStart() {
        super.onStart()
        FirebaseAnalytics.getInstance(requireContext()).logFragment(this)
    }

    fun isOnline(): Boolean {
        val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }
}


