package com.annton.mrbincardinfo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface BinListApi {
    @GET("{bin}")
    fun getCardInfo(@Path("bin") bin: String): Call<CardInfo>
}