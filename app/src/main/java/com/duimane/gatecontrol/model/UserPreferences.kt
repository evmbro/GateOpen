package com.duimane.gatecontrol.model

data class UserPreferences(val baseUrl: String,
                           val username: String,
                           val password: String,
                           val token: String)