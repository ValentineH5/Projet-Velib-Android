package com.example.projet_velib_entem_hochon.model

import android.os.Parcelable
import androidx.versionedparcelable.VersionedParcelable
import androidx.versionedparcelable.VersionedParcelize

data class Station(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val bikesAvailable: Int,
    val locationAvailable: Int,
    val isFavorite: Boolean = false
) {
    companion object {
        // Une méthode similaire à ton cours pour tester tes listes et ta carte
        fun generateMockStations(size: Int): List<Station> {
            return (1..size).map { i ->
                Station(
                    id = "STATION-$i",
                    name = "Station Velib n°$i",
                    latitude = 48.8566 + (i * 0.001), // Simule des positions autour de Paris
                    longitude = 2.3522 + (i * 0.001),
                    bikesAvailable = (0..20).random(),
                    locationAvailable = (0..15).random()
                )
            }
        }
    }
}

