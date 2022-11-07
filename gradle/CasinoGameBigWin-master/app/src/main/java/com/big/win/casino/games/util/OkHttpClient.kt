package com.big.win.casino.games.util

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object OkHttpCustomClient {
    private lateinit var  customClient : OkHttpClient
    fun setOkHttpClient(userAgent : String): OkHttpClient {
    if(!this::customClient.isInitialized){
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

    fun getOkHttpClient() = customClient
}