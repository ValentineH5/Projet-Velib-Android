package com.example.projet_velib_entem_hochon

import android.content.Context
import com.example.projet_velib_entem_hochon.model.Station

object FavoriteManager {
    private val favorites = mutableListOf<Station>()
    private var cacheManager: CacheManager? = null

    fun initCache(context: Context) {
        if (cacheManager == null) {
            cacheManager = CacheManager(context)

            // MODIFICATION ICI : On utilise la fonction dédiée aux favoris
            val savedFavorites = cacheManager?.getFavorites()

            favorites.clear()
            if (!savedFavorites.isNullOrEmpty()) {
                favorites.addAll(savedFavorites)
            }
        }
    }

    private fun saveToCache() {
        // MODIFICATION ICI : On utilise la fonction dédiée aux favoris
        cacheManager?.saveFavorites(favorites)
    }

    fun addFavorite(station: Station) {
        if (favorites.none { it.id == station.id }) {
            favorites.add(station)
            saveToCache()
        }
    }
    fun removeFavorite(station: Station) {
        favorites.removeAll { it.id == station.id }
        saveToCache()
    }

    fun isFavorite(station: Station): Boolean {
        return favorites.any { it.id == station.id }
    }

    fun getFavorites(): List<Station> {
        return favorites
    }
}