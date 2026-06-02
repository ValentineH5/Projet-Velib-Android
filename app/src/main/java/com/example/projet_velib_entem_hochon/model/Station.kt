package com.example.projet_velib_entem_hochon.model

import android.os.Parcelable
import androidx.versionedparcelable.VersionedParcelable
import androidx.versionedparcelable.VersionedParcelize
import com.google.gson.annotations.SerializedName
//import kotlinx.parcelize.Parcelize
import java.io.Serializable


import retrofit2.http.GET

// ==========================================
// 1. LES MODÈLES DE DONNÉES (API)
// ==========================================

// Modèles pour station_information.json
data class StationInformationResponse(val data: StationInfoData)
data class StationInfoData(val stations: List<StationInfo>)
data class StationInfo(
    val station_id: Long,
    val name: String,
    @SerializedName("lat") val latitude: Double,  // L'API utilise "lat"
    @SerializedName("lon") val longitude: Double,
    val capacity: Int
)

// Modèles pour station_status.json
data class StationStatusResponse(val data: StationStatusData)
data class StationStatusData(val stations: List<StationStatus>)
data class StationStatus(
    val station_id: Long,
    val num_bikes_available: Int,
    val num_docks_available: Int,
    val is_renting: Int
)

// Modèle final fusionné pour votre carte et votre logique
data class Station(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("num_bikes_available") val bikesAvailable: Int,
    @SerializedName("num_docks_available") val locationAvailable: Int,
    val isFavorite: Boolean = false
) : Serializable{

    companion object{
        fun generateMockStations(size: Int): List<Station> {
            return (1..size).map { i ->
                Station(
                    id = i.toLong(),
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

// ==========================================
// 2. L'INTERFACE RETROFIT
// ==========================================

interface VelibApiService {
    @GET("opendata/Velib_Metropole/station_information.json")
    suspend fun getStationInformation(): StationInformationResponse

    @GET("opendata/Velib_Metropole/station_status.json")
    suspend fun getStationStatus(): StationStatusResponse
}

// ==========================================
// 3. LA FONCTION DE FUSION (Optionnelle ici)
// ==========================================

fun mergeVelibData(infoList: List<StationInfo>, statusList: List<StationStatus>): List<Station> {
    val statusMap = statusList.associateBy { it.station_id }
    return infoList.mapNotNull { info ->
        val status = statusMap[info.station_id]
        if (status != null) {
            Station(
                id = info.station_id,
                name = info.name,
                latitude = info.latitude,
                longitude = info.longitude,
                bikesAvailable = status.num_bikes_available,
                locationAvailable = status.num_docks_available
            )
        } else {
            null
        }
    }
}