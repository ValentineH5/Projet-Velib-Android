package com.example.projet_velib_entem_hochon

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesListActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorites_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val favoriteStations = FavoriteManager.getFavorites()

        recyclerView = findViewById<RecyclerView>(R.id.favorites_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
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
