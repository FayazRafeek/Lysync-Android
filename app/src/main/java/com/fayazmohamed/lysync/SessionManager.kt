package com.fayazmohamed.lysync

import android.content.Context
import android.content.SharedPreferences

class SessionManager (context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    companion object {
        const val USER_TOKEN = "user_token"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun saveFcmToken(token: String) {
        val editor = prefs.edit()
        editor.putString("FCM_TOKEN", token)
        editor.apply()
    }

    fun saveDeviceId(id: String) {
        val editor = prefs.edit()
        editor.putString("DEVICE_ID", id)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, "")
    }

    fun fetchFcmToken(): String? {
        return prefs.getString("FCM_TOKEN", "null")
    }

    fun fetchDeviceId(): String? {
        return prefs.getString("DEVICE_ID", "null")
    }
}