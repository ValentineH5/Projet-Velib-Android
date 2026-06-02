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

class FavoritesListActivity : AppCompatActivity() {

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
        val favoriteStations = FavoriteManager.getFavorites()

        // 4. Initialisation du RecyclerView et de son LayoutManager
        recyclerView = findViewById<RecyclerView>(R.id.favorites_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        updateFavoritesList()


    }

    private fun updateFavoritesList() {

        val favoriteStations = FavoriteManager.getFavorites()

        recyclerView.adapter = StationAdapter(favoriteStations) { stationClicked ->
            val intent = Intent(this, StationDetailsActivity::class.java)
            intent.putExtra("STATION_EXTRA", stationClicked)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateFavoritesList()
    }

}
