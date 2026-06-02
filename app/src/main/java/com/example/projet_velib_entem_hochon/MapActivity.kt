package com.example.projet_velib_entem_hochon

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projet_velib_entem_hochon.model.Station
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuration indispensable d'OsmDroid pour le cache de la carte
        val sharedPrefs = getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
        Configuration.getInstance().load(applicationContext, sharedPrefs)

        setContentView(R.layout.activity_map)

        // Récupération de la MapView
        mapView = findViewById(R.id.mapView)

        // Configuration de base de la carte
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Centrer la carte sur Paris (Châtelet) au démarrage
        val mapController = mapView.controller
        mapController.setZoom(13.5)
        val parisCenter = GeoPoint(48.8566, 2.3522)
        mapController.setCenter(parisCenter)

        // 1. Initialisation de Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smovengo.cloud/")
            //.baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        velibApiService = retrofit.create(VelibApiService::class.java)

        // 2. Charger les vraies données depuis l'API
        chargerDonneesVelib()
    }

    private fun chargerDonneesVelib() {
        // Utilisation du lifecycleScope pour lancer la coroutine liée au cycle de vie de l'activité
        lifecycleScope.launch {
            try {
                // Les appels réseau se font en arrière-plan (Dispatchers.IO) pour ne pas bloquer l'interface
                val stationsFinales = withContext(Dispatchers.IO) {

                    // Appels simultanés ou successifs aux deux endpoints
                    val infoResponse = velibApiService.getStationInformation()
                    val statusResponse = velibApiService.getStationStatus()
                    mergeVelibData(infoResponse.data.stations, statusResponse.data.stations)

                    // Transformation du status en Map pour une recherche rapide par ID (O(1))
                   // val statusMap = statusResponse.data.stations.associateBy { it.station_id }

                }

                // 3. Affichage des vraies stations sur la carte (sur le Thread Principal)
                afficherStations(stationsFinales)

//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(this@MapActivity, "Erreur lors de la récupération des données : ${e.message}", Toast.LENGTH_LONG).show()
//            }
            } catch (e: Exception) {
                e.printStackTrace() // Garde ça pour voir dans le Logcat d'Android Studio

                // MODIFIE CETTE LIGNE : Elle affichera la vraie erreur sur ton téléphone
                Toast.makeText(this@MapActivity, "Erreur : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun afficherStations(stations: List<Station>) {
        mapView.overlays.clear()

        stations.forEach { station ->
            val stationMarker = Marker(mapView).apply {
                position = GeoPoint(station.latitude, station.longitude)
                title = station.name
                snippet = "Vélos dispo : ${station.bikesAvailable}\nBornes libres : ${station.locationAvailable}"

                setOnMarkerClickListener { marker, _ ->
                    marker.showInfoWindow()
                    Toast.makeText(this@MapActivity, "Station : ${station.name}", Toast.LENGTH_SHORT).show()
                    true
                }
            }
            mapView.overlays.add(stationMarker)
        }

        mapView.invalidate() // Force la carte à se redessiner
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