package com.duimane.gatecontrol.api

import com.duimane.gatecontrol.model.response.ImageResponse
import com.duimane.gatecontrol.model.response.OpenGateResponse
import com.duimane.gatecontrol.model.response.TokenResponse
import com.duimane.gatecontrol.util.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface GateApiService {

    @POST(TOKEN_ENDPOINT)
    @FormUrlEncoded
    fun getToken(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<TokenResponse>

    @GET(IMAGE_ENDPOINT)
    fun getImage(): Call<ImageResponse>

    @GET(OPEN_ENDPOINT)
    fun openGate(): Call<OpenGateResponse>

    companion object Factory {

        const val TOKEN_ENDPOINT = "api-token-auth/"
        const val IMAGE_ENDPOINT = "get-photo/"
        const val OPEN_ENDPOINT  = "open-ramp/"

        private var instance: GateApiService? = null

        fun configure(baseUrl: String, token: String? = null)  {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    if (token == null || request.url().encodedPath().equals("/${TOKEN_ENDPOINT}", true)) {
                        chain.proceed(request)
                    } else {
                        val authorisedRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Token $token")
                            .build()
                        chain.proceed(authorisedRequest)
                    }
                }
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            instance = retrofit.create(GateApiService::class.java)
        }

        fun instance(): GateApiService? = instance

    }

}