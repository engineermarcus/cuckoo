package com.cuckoo.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppWhitelistRepository {
    private const val PREFS = "cuckoo_prefs"
    private const val KEY_WHITELIST = "study_app_whitelist"
    private const val KEY_ONBOARDED = "onboarding_complete"
    private val gson = Gson()

    fun getWhitelist(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_WHITELIST, null) ?: return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveWhitelist(context: Context, packages: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WHITELIST, gson.toJson(packages)).apply()
    }

    fun isOnboarded(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDED, false)
    }

    fun setOnboarded(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ONBOARDED, true).apply()
    }
}
