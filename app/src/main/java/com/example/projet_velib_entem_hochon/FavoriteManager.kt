package com.example.projet_velib_entem_hochon

import android.content.Context
import com.example.projet_velib_entem_hochon.model.Station

object FavoriteManager {
    private val favorites = mutableListOf<Station>()
    private var cacheManager: CacheManager? = null

    // Appelée une seule fois au lancement de l'app (ex: dans MainActivity)
    fun initCache(context: Context) {
        if (cacheManager == null) {
            cacheManager = CacheManager(context)

            // On récupère ce qui est enregistré
            val savedFavorites = cacheManager?.getStations()

            favorites.clear()
            // Si on a déjà sauvegardé des favoris auparavant, on les charge
            if (!savedFavorites.isNullOrEmpty()) {
                favorites.addAll(savedFavorites)
            }
            // Si c'est le premier démarrage, savedFavorites est null ou vide,
            // donc 'favorites' reste une liste vide [].
        }
    }

    private fun saveToCache() {
        cacheManager?.saveStations(favorites)
    }

    fun addFavorite(station: Station) {
        if (favorites.none { it.id == station.id }) {
            favorites.add(station)
            saveToCache() // Sauvegarde
        }
    }

    fun removeFavorite(station: Station) {
        favorites.removeAll { it.id == station.id }
        saveToCache() // Sauvegarde après suppression
    }

    fun isFavorite(station: Station): Boolean {
        return favorites.any { it.id == station.id }
    }

    fun getFavorites(): List<Station> {
        return favorites
    }
}