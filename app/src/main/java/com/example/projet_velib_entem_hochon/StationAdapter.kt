package com.example.projet_velib_entem_hochon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projet_velib_entem_hochon.model.Station

class StationAdapter(
    private val stations: List<Station>,
    private val onItemClick: (Station) -> Unit
) : RecyclerView.Adapter<StationAdapter.StationViewHolder>() {

    // Étape 1 : On crée le ViewHolder qui contient les liaisons XML
    class StationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.station_card_name)
        val statusTextView: TextView = view.findViewById(R.id.station_card_status)
        val bikesTextView: TextView = view.findViewById(R.id.station_card_bikes)
    }

    // Étape 2 : On charge le layout xml de la ligne (station_card_view)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.station_card_view, parent, false)
        return StationViewHolder(view)
    }

    // Étape 3 : On injecte les données de la station actuelle dans les vues textuelles
    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = stations[position]
        holder.nameTextView.text = station.name
        holder.statusTextView.text = "Places libres : ${station.locationAvailable}"
        holder.bikesTextView.text = "${station.bikesAvailable} 🚲"

        // Gestion du clic sur la ligne
        holder.itemView.setOnClickListener { onItemClick(station) }
    }

    override fun getItemCount(): Int = stations.size
}
