package com.example.projet_velib_entem_hochon

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.projet_velib_entem_hochon.model.Station
import com.example.projet_velib_entem_hochon.model.VelibApiService
import com.example.projet_velib_entem_hochon.model.mergeVelibData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.*


class MapActivity: AppCompatActivity() {

    private lateinit var messageConnexion: TextView
    private var filtreActuel = 0
    private var listeStationsSauvegardee: List<Station> = emptyList()
    private lateinit var mapView: MapView
    private lateinit var velibApiService: VelibApiService
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private var maPosition: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
        Configuration.getInstance().load(applicationContext, sharedPrefs)

        setContentView(R.layout.activity_map)

        FavoriteManager.initCache(this)

        mapView = findViewById(R.id.mapView)
        messageConnexion = findViewById(R.id.messageConnexion)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(13.5)
        val parisCenter = GeoPoint(48.8566, 2.3522)
        mapController.setCenter(parisCenter)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smovengo.cloud/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        velibApiService = retrofit.create(VelibApiService::class.java)

        val btnVelo = findViewById<Button>(R.id.btnFiltreVelo)
        val btnPlace = findViewById<Button>(R.id.btnFiltrePlace)
        val btnFavoris = findViewById<Button>(R.id.btnFiltreFavoris)

        btnVelo.setOnClickListener {
            filtreActuel = if (filtreActuel == 1) 0 else 1
            appliquerFiltreEtAfficher()
        }
        btnPlace.setOnClickListener {
            filtreActuel = if (filtreActuel == 2) 0 else 2
            appliquerFiltreEtAfficher()
        }
        btnFavoris.setOnClickListener {
            filtreActuel = if (filtreActuel == 3) 0 else 3
            appliquerFiltreEtAfficher()
        }
        verifierPermissionsEtGeolocaliser()
    }
    private fun verifierPermissionsEtGeolocaliser() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                maPosition = location
                chargerDonneesVelib()
            } else {
                Toast.makeText(this,
                    "Impossible d'obtenir votre position GPS. Par défaut : Paris Centré.",
                    Toast.LENGTH_LONG).show()
                chargerDonneesVelib()
            }
        }
    }

    private fun chargerDonneesVelib() {
        val cacheManager = CacheManager(this)
        lifecycleScope.launch {
            try {
                val stationsFinales = withContext(Dispatchers.IO) {
                    val infoResponse = velibApiService.getStationInformation()
                    val statusResponse = velibApiService.getStationStatus()
                    mergeVelibData(infoResponse.data.stations,
                        statusResponse.data.stations)
                }
                withContext(Dispatchers.IO) {
                    try {
                        cacheManager.saveStations(stationsFinales)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                listeStationsSauvegardee = stationsFinales
                appliquerFiltreEtAfficher(isOffline = false)

                afficherStations(stationsFinales)

            } catch (e: Exception) {
                e.printStackTrace()
                val cachedStations = withContext(Dispatchers.IO) {
                    cacheManager.getStations()
                }
                if (!cachedStations.isNullOrEmpty()) {
                    listeStationsSauvegardee = cachedStations
                    appliquerFiltreEtAfficher(isOffline = true)
                } else {
                    Toast.makeText(this@MapActivity,
                        "Erreur : ${e.localizedMessage}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun appliquerFiltreEtAfficher(isOffline: Boolean = false) {
        val stationsFiltrees = when (filtreActuel) {
            1 -> listeStationsSauvegardee.filter { (it.bikesAvailable ?: 0) > 0 }
            2 -> listeStationsSauvegardee.filter { (it.locationAvailable ?: 0) > 0 }
            3 -> listeStationsSauvegardee.filter { (FavoriteManager.isFavorite(it)) }
            else -> listeStationsSauvegardee
        }
        afficherStations(stationsFiltrees, isOffline)
    }
    private fun afficherStations(stations: List<Station>, isOffline: Boolean = false) {
        if (isOffline) {
            messageConnexion.visibility = View.VISIBLE
            messageConnexion.text = "Affichage hors-connexion (données non mises à jour)"
        } else {
            messageConnexion.visibility = View.GONE
        }
        mapView.overlays.clear()
        val mapController = mapView.controller
        mapController.setZoom(14.5)
        if (maPosition != null) {
            val userGeoPoint = GeoPoint(maPosition!!.latitude, maPosition!!.longitude)
            mapController.setCenter(userGeoPoint)
            val userMarker = Marker(mapView).apply {
                position = userGeoPoint
                title = "Ma Position"
                val userIcon = ContextCompat.getDrawable(
                    this@MapActivity,
                    org.osmdroid.library.R.drawable.marker_default
                )?.mutate()
                userIcon?.setTint(Color.RED)
                icon = userIcon
            }
            mapView.overlays.add(userMarker)
            stations.forEach { station ->
                station.distance = calculerDistance(
                    maPosition!!.latitude, maPosition!!.longitude,
                    station.latitude, station.longitude
                )
            }

            val stationsTriees = stations.sortedBy { it.distance }
            stationsTriees.forEachIndexed { index, station ->
                val stationMarker = Marker(mapView).apply {
                    position = GeoPoint(station.latitude, station.longitude)
                    title = station.name
                    snippet =
                        "Vélos dispo : ${station.bikesAvailable ?: 0}\nPlaces libres : ${
                            station.locationAvailable ?: 0}\nDistance : ${
                            String.format("%.2f",station.distance)
                        } km"
                    infoWindow = StationInfoWindow(mapView, this@MapActivity, station)
                    if (index < 3) {
                        val bleuIcon = ContextCompat.getDrawable(
                            this@MapActivity,
                            org.osmdroid.library.R.drawable.marker_default
                        )?.mutate()
                        bleuIcon?.setTint(Color.BLUE)
                        icon = bleuIcon
                    } else if (FavoriteManager.isFavorite(station)) {
                        val yellowIcon = ContextCompat.getDrawable(
                            this@MapActivity,
                            org.osmdroid.library.R.drawable.marker_default
                        )?.mutate()
                        yellowIcon?.setTint(Color.YELLOW)
                        icon = yellowIcon
                    } else {
                        //Toutes les autres stations
                    }
                    setOnMarkerClickListener { marker, _ ->
                        marker.showInfoWindow()
                        true
                    }
                }
                mapView.overlays.add(stationMarker)
            }
        } else {
            mapController.setCenter(GeoPoint(48.8566, 2.3522))
            stations.forEach { station ->
                val stationMarker = Marker(mapView).apply {
                    position = GeoPoint(station.latitude, station.longitude)
                    title = station.name
                }
                mapView.overlays.add(stationMarker)
            }

            stations.forEach { station ->
                val stationMarker = Marker(mapView).apply {
                    position = GeoPoint(station.latitude, station.longitude)
                    title = station.name
                    snippet =
                        "Vélos dispo : ${station.bikesAvailable}\nBornes libres : ${station.locationAvailable}"

                    infoWindow = StationInfoWindow(
                        mapView,
                        this@MapActivity,
                        station
                    )

                    setOnMarkerClickListener { marker, _ ->
                        marker.showInfoWindow()
                        true
                    }
                }
                mapView.overlays.add(stationMarker)
            }
            mapView.invalidate()
        }
    }
    // Formule mathématique simplifiée (Haversine) pour calculer la distance entre deux points GPS
    private fun calculerDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Rayon de la Terre en kilomètres
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                verifierPermissionsEtGeolocaliser()
            } else {
                Toast.makeText(
                    this,
                    "Permission refusée. Les stations ne seront pas triées.",
                    Toast.LENGTH_LONG
                ).show()
                chargerDonneesVelib()
            }
        }
    }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
}