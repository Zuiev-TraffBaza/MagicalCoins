package com.big.win.casino.games.main


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.big.win.casino.games.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainViewModel(
    private val mApplication: Application
)  : AndroidViewModel(mApplication) {
    private val appName = mApplication.packageName
    val siteUrl = MutableLiveData<Resource<String>>()


}
