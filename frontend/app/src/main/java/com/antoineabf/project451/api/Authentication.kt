package com.antoineabf.project451.api

import android.content.Context
import android.content.SharedPreferences

object Authentication {
    private var token: String? = null
    private var preferences: SharedPreferences? = null

    public fun initialize(context: Context) {
        preferences = context.getSharedPreferences("SETTINGS",
            Context.MODE_PRIVATE)
        token = preferences?.getString("TOKEN", null)
    }

    public fun getToken(): String? {
        return token
    }

    public fun saveToken(token: String) {
        this.token = token
        preferences?.edit()?.putString("TOKEN", token)?.apply()
    }

    public fun clearToken() {
        this.token = null
        preferences?.edit()?.remove("TOKEN")?.apply()
    }

}