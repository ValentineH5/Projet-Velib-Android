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

        // C'est cette ligne qui lie le code Kotlin au fichier activity_main.xml
        setContentView(R.layout.activity_map)

        // Récupération de la MapView du fichier XML
        mapView = findViewById(R.id.mapView)

        // Configuration de base de la carte
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Centrer la carte sur Paris (Châtelet) au démarrage
        val mapController = mapView.controller
        mapController.setZoom(13.5)
        val parisCenter = GeoPoint(48.8566, 2.3522)
        mapController.setCenter(parisCenter)

        // Liste de test temporaire (Données fictives)
        val sampleStations = listOf(
            Station("1", "Station Châtelet", 48.8584, 2.3475, 14, 6),
            Station("2", "Station Tour Eiffel", 48.8583, 2.2944, 3, 22),
            Station("3", "Station République", 48.8675, 2.3638, 0, 19)
        )

        // Afficher les stations sur la carte
        afficherStations(sampleStations)
    }

    private fun afficherStations(stations: List<Station>) {
        mapView.overlays.clear()

        stations.forEach { station ->
            val stationMarker = Marker(mapView).apply {
                position = GeoPoint(station.latitude, station.longitude)
                title = station.name
                snippet = "Vélos dispo : ${station.bikesAvailable}\nPlaces libres : ${station.locationAvailable}"

                // Action lors du clic sur le marqueur
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

    // Gestion du cycle de vie de la carte
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}