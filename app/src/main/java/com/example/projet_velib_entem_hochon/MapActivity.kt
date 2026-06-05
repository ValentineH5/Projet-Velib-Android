package com.example.projet_velib_entem_hochon

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projet_velib_entem_hochon.model.Station
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.widget.TextView
import com.example.projet_velib_entem_hochon.model.VelibApiService
import com.example.projet_velib_entem_hochon.model.mergeVelibData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class MapActivity : AppCompatActivity() {
    private lateinit var messageConnexion: TextView
    private lateinit var mapView: MapView
    private lateinit var velibApiService: VelibApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configuration indispensable d'OsmDroid pour le cache de la carte
        val sharedPrefs = getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
        Configuration.getInstance().load(applicationContext, sharedPrefs)
        setContentView(R.layout.activity_map)
        mapView = findViewById(R.id.mapView)
        messageConnexion = findViewById(R.id.messageConnexion)
        // Configuration de base de la carte
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        // Centrer la carte
        val mapController = mapView.controller
        mapController.setZoom(13.5)
        val parisCenter = GeoPoint(48.8566, 2.3522)
        mapController.setCenter(parisCenter)
        // 1. Initialisation de Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smovengo.cloud/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        velibApiService = retrofit.create(VelibApiService::class.java)
        // 2. Charger les vraies données depuis l'API
        chargerDonneesVelib()
    }
private fun chargerDonneesVelib() {
    val cacheManager = CacheManager(this)
    lifecycleScope.launch {
        try {
            val stationsFinales = withContext(Dispatchers.IO) {
                val infoResponse = velibApiService.getStationInformation()
                val statusResponse = velibApiService.getStationStatus()
                mergeVelibData(infoResponse.data.stations, statusResponse.data.stations)
            }

            withContext(Dispatchers.IO) {
                try {
                    cacheManager.saveStations(stationsFinales)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            afficherStations(stationsFinales, isOffline = false)

        } catch (e: Exception) {
            e.printStackTrace()

            val cachedStations = withContext(Dispatchers.IO) {
                cacheManager.getStations()
            }

            if (!cachedStations.isNullOrEmpty()) {
                afficherStations(cachedStations, isOffline = true)
            } else {
                // Pas de réseau ET pas de cache
                Toast.makeText(this@MapActivity, "Erreur : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
        }
    }
    private fun afficherStations(stations: List<Station>, isOffline: Boolean = false) {
        if (isOffline) {
            messageConnexion.visibility = View.VISIBLE
            messageConnexion.text = "Affichage hors-connexion (données non mises à jour)"
            Toast.makeText(this, "Mode hors-ligne : données locales chargées", Toast.LENGTH_SHORT).show()
        } else {
            messageConnexion.visibility = View.GONE
        }
        mapView.overlays.clear()
        // 3. Ajout des marqueurs pour chaque station
        stations.forEach { station ->
            val stationMarker = Marker(mapView).apply {
                position = GeoPoint(station.latitude, station.longitude)
                title = station.name
                snippet = "Vélos dispo : ${station.bikesAvailable ?: 0}\nPlaces libres : ${station.locationAvailable ?: 0}"
                setOnMarkerClickListener { marker, _ ->
                    marker.showInfoWindow()
                    Toast.makeText(this@MapActivity, "Station : ${station.name}", Toast.LENGTH_SHORT).show()
                    true
                }
            }
            mapView.overlays.add(stationMarker)
        }
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}