package com.example.projet_velib_entem_hochon

import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import com.example.projet_velib_entem_hochon.model.Station
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class StationInfoWindow(
    mapView: MapView,
    private val context: Context,
    private val station: Station
) : MarkerInfoWindow(R.layout.info_window_station, mapView) {

    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return

        // 1. Récupération et remplissage des textes
        val titleView = mView.findViewById<TextView>(R.id.info_title)
        val snippetView = mView.findViewById<TextView>(R.id.info_snippet)
        titleView?.text = marker.title
        snippetView?.text = marker.snippet

        // 2. Récupération du bouton et gestion du clic
        val detailsButton = mView.findViewById<Button>(R.id.btn_info_details)
        detailsButton?.setOnClickListener {
            val intent = Intent(context, StationDetailsActivity::class.java).apply {
                putExtra("STATION_EXTRA", station)
                // Important pour ouvrir l'activité de manière fluide depuis l'InfoWindow
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            close() // Ferme la bulle d'info sur la carte
        }
    }

    override fun onClose() {
        // Nettoyage si nécessaire à la fermeture
    }
}