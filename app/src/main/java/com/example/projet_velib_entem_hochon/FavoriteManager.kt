package com.example.projet_velib_entem_hochon

import com.example.projet_velib_entem_hochon.model.Station

object FavoriteManager {
    private val favorites = mutableListOf<Station>()
    init {
        // Stations de test
        val mock = Station.generateMockStations(5)
        favorites.addAll(mock.take(3)) // on en met 3 en favoris
    }

    fun addFavorite(station: Station) {
        if (favorites.none { it.id == station.id }) {
            favorites.add(station)
        }
    }

    fun removeFavorite(station: Station) {
        favorites.removeAll { it.id == station.id }
    }

    fun isFavorite(station: Station): Boolean {
        return favorites.any { it.id == station.id }
    }

    fun getFavorites(): List<Station> {
        return favorites
    }
}