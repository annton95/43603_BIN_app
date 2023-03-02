package com.annton.mrbincardinfo

import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BinListApiClient {
    private val retrofit: Retrofit

    init {
        retrofit = Retrofit.Builder()
            .baseUrl("https://lookup.binlist.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getCardInfo(bin: String, callback: Callback<CardInfo>) {
        val api = retrofit.create(BinListApi::class.java)
        val call = api.getCardInfo(bin)

        call.enqueue(callback)
    }
}