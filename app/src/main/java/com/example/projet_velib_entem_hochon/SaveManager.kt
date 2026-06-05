package com.example.projet_velib_entem_hochon

import android.content.Context
import com.example.projet_velib_entem_hochon.model.Station
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CacheManager(context: Context) {
    // SharedPreferences permet de stocker des données sur le téléphone
    private val sharedPreferences = context.getSharedPreferences("velib_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    // 1. Sauvegarder TOUTES les stations d'un coup
    fun saveStations(stations: List<Station>) {
        // On transforme TOUTE la liste d'objets Station en un seul texte (JSON)
        val stationsJson = gson.toJson(stations)

        // On l'enregistre sous une clé unique globale
        sharedPreferences.edit()
            .putString("all_stations_cache", stationsJson)
            .apply()
    }

    // 2. Récupérer TOUTES les stations sauvegardées
    fun getStations(): List<Station>? {
        val stationsJson = sharedPreferences.getString("all_stations_cache", null)

        if (stationsJson != null) {
            // Attention : Pour du JSON contenant une Liste d'objets, Gson a besoin d'un "TypeToken"
            // pour savoir qu'il doit recréer une List<Station> et non un objet simple.
            val type = object : TypeToken<List<Station>>() {}.type
            return gson.fromJson(stationsJson, type)
        }

        return null // Si le cache est vide
    }
}