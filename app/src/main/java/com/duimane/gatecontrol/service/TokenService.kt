package com.duimane.gatecontrol.service

import android.app.Activity
import com.duimane.gatecontrol.api.GateApiService
import com.duimane.gatecontrol.model.UserPreferences
import com.duimane.gatecontrol.model.response.TokenResponse
import com.duimane.gatecontrol.util.SharedPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TokenService {

    companion object {

        fun fetchAndStore(
            activity: Activity?,
            onComplete: () -> Unit,
            onError: (String) -> Unit
        ) {
            val preferences = SharedPreferences.get(activity) ?: return
            fetchAndStore(
                preferences.baseUrl,
                preferences.username,
                preferences.password,
                activity,
                onComplete,
                onError
            )
        }

        fun fetchAndStore(
            url: String,
            username: String,
            password: String,
            activity: Activity?,
            onComplete: () -> Unit,
            onError: (String) -> Unit
        ) {
            if (GateApiService.instance() == null) {
                GateApiService.configure(url)
            }
            GateApiService.instance()?.getToken(username, password)?.enqueue(
                object: Callback<TokenResponse> {
                    override fun onResponse(
                        call: Call<TokenResponse>,
                        response: Response<TokenResponse>
                    ) {
                        val code = response.code()
                        val token = response.body()?.token
                        if (code == 200 && token is String) {
                            try {
                                GateApiService.configure(url, token)
                                SharedPreferences.store(activity, UserPreferences(
                                    url, username, password, token
                                ))
                                onComplete()
                            } catch (ex: Exception) {
                                onError("Unexpected error occurred. Please try again.")
                            }
                        } else if (code == 401 || code == 400) {
                            onError("Bad request. Please check your credentials and try again.")
                        } else {
                            onError("Unexpected error occurred. Please try again.")
                        }
                    }
                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                        onError("Unexpected error occurred. Please try again.")
                    }
                }
            )

        }

    }

}