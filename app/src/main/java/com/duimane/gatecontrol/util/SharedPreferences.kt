package com.duimane.gatecontrol.util

import android.app.Activity
import android.content.Context
import com.duimane.gatecontrol.model.UserPreferences

class SharedPreferences {

    companion object {

        private const val BASE_URL_SHARED_PREF_KEY = "baseUrl"
        private const val USERNAME_SHARED_PREF_KEY = "username"
        private const val PASSWORD_SHARED_PREF_KEY = "password"
        private const val TOKEN_SHARED_PREF_KEY    = "token"

        fun get(activity: Activity?): UserPreferences? {
            val preferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return null
            val baseUrl = preferences.getString(BASE_URL_SHARED_PREF_KEY, null)
            val username = preferences.getString(USERNAME_SHARED_PREF_KEY, null)
            val password = preferences.getString(PASSWORD_SHARED_PREF_KEY, null)
            val token = preferences.getString(TOKEN_SHARED_PREF_KEY, null)

            return if (baseUrl == null || username == null || password == null || token == null) {
                null
            } else {
                UserPreferences(
                    baseUrl,
                    username,
                    password,
                    token
                )
            }
        }

        fun store(activity: Activity?, prefs: UserPreferences) {
            val preferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
            with(preferences.edit()) {
                putString(BASE_URL_SHARED_PREF_KEY, prefs.baseUrl)
                putString(USERNAME_SHARED_PREF_KEY, prefs.username)
                putString(PASSWORD_SHARED_PREF_KEY, prefs.password)
                putString(TOKEN_SHARED_PREF_KEY, prefs.token)
                commit()
            }
        }

    }

}