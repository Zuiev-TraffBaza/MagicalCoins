package com.big.win.casino.games.main

import android.os.Bundle
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import com.big.win.casino.games.R

import com.big.win.casino.games.util.OkHttpCustomClient
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.*


@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(){
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel(application)
        OkHttpCustomClient.setOkHttpClient(WebSettings.getDefaultUserAgent(this))
        checkForAppUpdates()
        setContentView(R.layout.activity_main)

    }


    private fun checkForAppUpdates(){
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { result ->

            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, this, 0)
            }
        }
    }


    override fun onResume() {
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { result ->
            if (result.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, this, 0)
            }
        }
        super.onResume()
    }

}