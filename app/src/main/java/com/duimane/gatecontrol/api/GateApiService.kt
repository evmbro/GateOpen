package com.duimane.gatecontrol.api

import com.duimane.gatecontrol.api.model.ImageResponse
import com.duimane.gatecontrol.api.model.TokenResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface GateApiService {

    @POST("api-token-auth/")
    @FormUrlEncoded
    fun getToken(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<TokenResponse>

    @GET("get-photo/")
    fun getImage(@Header("Authorization") token: String): Call<ImageResponse>

    companion object Factory {

        private var instance: GateApiService? = null

        fun configure(baseUrl: String)  {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            instance = retrofit.create(GateApiService::class.java)
        }

        fun instance(): GateApiService? = instance

    }

}