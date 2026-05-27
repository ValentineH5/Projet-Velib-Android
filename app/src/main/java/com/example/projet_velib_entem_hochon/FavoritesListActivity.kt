package com.example.projet_velib_entem_hochon

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projet_velib_entem_hochon.model.Station

//class FavoriteListActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_favorites_list)
//
//        // 1. Récupération des données fictives de test
//        val favoriteStations = Station.generateMockStations(15)
//
//        // 2. Initialisation du RecyclerView
//        val recyclerView = findViewById<RecyclerView>(R.id.favorites_recyclerview)
//
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        // 3. Liaison avec l'adaptateur personnalisé
//        recyclerView.adapter = StationAdapter(favoriteStations) { stationClicked ->
//            val intent = Intent(this, StationDetailsActivity::class.java)
//            intent.putExtra("STATION_EXTRA", stationClicked)
//            startActivity(intent)
//        }
//    }
//}

class FavoriteListActivity : AppCompatActivity() {

    // Déclaration du RecyclerView au niveau de la classe (comme ton modèle)
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Activation du EdgeToEdge
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorites_list)

        // 2. Gestion des barres système (Statut / Navigation) pour éviter les chevauchements
        // Remplace R.id.main par l'ID du layout parent dans ton fichier activity_favorites_list.xml (ex: un ConstraintLayout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. Récupération des données fictives de test
        val favoriteStations = Station.generateMockStations(15)

        // 4. Initialisation du RecyclerView et de son LayoutManager
        recyclerView = findViewById<RecyclerView>(R.id.favorites_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // 5. Liaison avec l'adaptateur personnalisé et gestion du clic
        recyclerView.adapter = StationAdapter(favoriteStations) { stationClicked ->
            val intent = Intent(this, StationDetailsActivity::class.java)
            intent.putExtra("STATION_EXTRA", stationClicked)
            startActivity(intent)
        }
    }
}
