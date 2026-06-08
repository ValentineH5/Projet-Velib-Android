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
    private var filtreActuel = 0 // 0: Tous, 1: Vélos dispo, 2: Places dispo
    private var listeStationsSauvegardee: List<Station> = emptyList() // Pour garder les données en mémoire
    private lateinit var mapView: MapView
    private lateinit var velibApiService: VelibApiService
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private var maPosition: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuration indispensable d'OsmDroid pour le cache de la carte
        val sharedPrefs = getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
        Configuration.getInstance().load(applicationContext, sharedPrefs)

        setContentView(R.layout.activity_map)

        FavoriteManager.initCache(this)

        // Récupération de la MapView
        mapView = findViewById(R.id.mapView)
        messageConnexion = findViewById(R.id.messageConnexion)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        velibApiService = retrofit.create(VelibApiService::class.java)

        val btnVelo = findViewById<Button>(R.id.btnFiltreVelo)
        val btnPlace = findViewById<Button>(R.id.btnFiltrePlace)
        val btnFavoris = findViewById<Button>(R.id.btnFiltreFavoris)

        btnVelo.setOnClickListener {
            // Si on clique alors qu'il est déjà actif, on l'éteint (retour à 0), sinon on l'active (1)
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
        // 2. Vérifier les permissions et demander la géolocalisation de l'utilisateur
        verifierPermissionsEtGeolocaliser()
    }
    private fun verifierPermissionsEtGeolocaliser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        // Obtenir la position actuelle
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                maPosition = location
                chargerDonneesVelib()
            } else {
                Toast.makeText(this, "Impossible d'obtenir votre position GPS. Par défaut : Paris Centré.", Toast.LENGTH_LONG).show()
                // Fallback si le GPS est éteint : on charge quand même mais sans le tri parfait
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
                    mergeVelibData(infoResponse.data.stations, statusResponse.data.stations)
                }
                withContext(Dispatchers.IO) {
                    try {
                        cacheManager.saveStations(stationsFinales)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // MODIFICATION : On sauvegarde la liste fraîche et on applique le filtre
                listeStationsSauvegardee = stationsFinales
                appliquerFiltreEtAfficher(isOffline = false)

                // 3. Affichage des vraies stations sur la carte (sur le Thread Principal)
                afficherStations(stationsFinales)

//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(this@MapActivity, "Erreur lors de la récupération des données : ${e.message}", Toast.LENGTH_LONG).show()
//            }
            } catch (e: Exception) {
                e.printStackTrace()
                val cachedStations = withContext(Dispatchers.IO) {
                    cacheManager.getStations()
                }
                if (!cachedStations.isNullOrEmpty()) {
                    // MODIFICATION : On sauvegarde la liste du cache et on applique le filtre
                    listeStationsSauvegardee = cachedStations
                    appliquerFiltreEtAfficher(isOffline = true)
                } else {
                    Toast.makeText(this@MapActivity, "Erreur : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun appliquerFiltreEtAfficher(isOffline: Boolean = false) {
        // On filtre la liste globale en fonction de la valeur de 'filtreActuel'
        val stationsFiltrees = when (filtreActuel) {
            1 -> listeStationsSauvegardee.filter { (it.bikesAvailable ?: 0) > 0 }      // Que les vélos dispo
            2 -> listeStationsSauvegardee.filter { (it.locationAvailable ?: 0) > 0 }   // Que les places dispo
            3 -> listeStationsSauvegardee.filter { (FavoriteManager.isFavorite(it)) }    //Que les stations favorites
            else -> listeStationsSauvegardee                                           // Tout afficher
        }
        // On envoie le résultat filtré
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
        //Gérer le point ROUGE de l'utilisateur
        if (maPosition != null) {
            val userGeoPoint = GeoPoint(maPosition!!.latitude, maPosition!!.longitude)
            mapController.setCenter(userGeoPoint)
            val userMarker = Marker(mapView).apply {
                position = userGeoPoint
                title = "Ma Position"
                val userIcon = ContextCompat.getDrawable(this@MapActivity, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                userIcon?.setTint(Color.RED)
                icon = userIcon
            }
            mapView.overlays.add(userMarker)
            // Calculer la distance
            stations.forEach { station ->
                station.distance = calculerDistance(
                    maPosition!!.latitude, maPosition!!.longitude,
                    station.latitude, station.longitude
                )
            }
            // Trier les stations
            val stationsTriees = stations.sortedBy { it.distance }
            stationsTriees.forEachIndexed { index, station ->
                val stationMarker = Marker(mapView).apply {
                    position = GeoPoint(station.latitude, station.longitude)
                    title = station.name
                    snippet =
                        "Vélos dispo : ${station.bikesAvailable ?: 0}\nPlaces libres : ${station.locationAvailable ?: 0}\nDistance : ${
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
                        //TOUTES LES AUTRES
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

                    // Action lors du clic sur le marqueur
                    setOnMarkerClickListener { marker, _ ->
                        marker.showInfoWindow()
                        //Toast.makeText(this@MapActivity, "Station : ${station.name}", Toast.LENGTH_SHORT).show()
                        true
                    }
                }
                mapView.overlays.add(stationMarker)
            }

            mapView.invalidate() // Force la carte à se redessiner
        }
    }
    // Formule mathématique simplifiée (Haversine) pour calculer la distance entre deux points GPS
    private fun calculerDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Rayon de la Terre en kilomètres
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                verifierPermissionsEtGeolocaliser()
            } else {
                Toast.makeText(this, "Permission refusée. Les stations ne seront pas triées.", Toast.LENGTH_LONG).show()
                chargerDonneesVelib()
            }
        }
    }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
}