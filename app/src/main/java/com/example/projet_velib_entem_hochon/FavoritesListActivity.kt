package com.example.projet_velib_entem_hochon

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.projet_velib_entem_hochon.model.Station

class FavoriteListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites_list)

        // 1. Récupération des données fictives de test
        val favoriteStations = Station.generateMockStations(15)

        // 2. Initialisation du RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.favorites_recyclerview)

        // 3. Liaison avec l'adaptateur personnalisé
        recyclerView.adapter = StationAdapter(favoriteStations) { stationClicked ->
            // Clic : Ouvre l'écran de détails en transmettant l'objet Station cliqué
            val intent = Intent(this, StationDetailsActivity::class.java)
            intent.putExtra("STATION_EXTRA", stationClicked)

            startActivity(intent)
        }
    }
}
