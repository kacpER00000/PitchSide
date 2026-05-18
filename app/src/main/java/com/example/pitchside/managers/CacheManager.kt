package com.example.pitchside.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit // Upewnij się, że masz ten import!

class CacheManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "pitchside_cache_prefs",
        Context.MODE_PRIVATE
    )
    private val CACHE_EXPIRATION_TIME = 6 * 60 * 60 * 1000L
    fun updateLastLeagueRefresh(leagueCode: String) {
        val currentTime = System.currentTimeMillis()
        prefs.edit {
            putLong("LAST_REFRESH_LEAGUE_$leagueCode", currentTime)
        }
    }
    fun isLeagueCacheExpired(leagueCode: String): Boolean {
        val lastRefresh = prefs.getLong("LAST_REFRESH_LEAGUE_$leagueCode", 0L)
        if (lastRefresh == 0L) return true
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastRefresh) > CACHE_EXPIRATION_TIME
    }
}