package com.example.projet_velib_entem_hochon

import android.content.Context
import com.example.projet_velib_entem_hochon.model.Station
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
class CacheManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        "velib_cache",
        Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveStations(stations: List<Station>) {
        val stationsJson = gson.toJson(stations)
        sharedPreferences.edit()
            .putString("all_stations_cache", stationsJson)
            .apply()
    }

    fun getStations(): List<Station>? {
        val stationsJson = sharedPreferences.getString("all_stations_cache", null)
        if (stationsJson != null) {
            val type = object : TypeToken<List<Station>>() {}.type
            return gson.fromJson(stationsJson, type)
        }
        return null
    }

    fun saveFavorites(favorites: List<Station>) {
        val favoritesJson = gson.toJson(favorites)
        sharedPreferences.edit()
            .putString("favorites_stations_cache", favoritesJson)
            .apply()
    }

    fun getFavorites(): List<Station>? {
        val favoritesJson = sharedPreferences.getString("favorites_stations_cache", null)
        if (favoritesJson != null) {
            val type = object : TypeToken<List<Station>>() {}.type
            return gson.fromJson(favoritesJson, type)
        }
        return null
    }
}