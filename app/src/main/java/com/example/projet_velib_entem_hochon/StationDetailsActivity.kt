package com.example.projet_velib_entem_hochon

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projet_velib_entem_hochon.model.Station
import android.widget.ImageButton

class StationDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_details)

        val station = intent.getSerializableExtra("STATION_EXTRA") as? Station

        val favoriteButton = findViewById<ImageButton>(R.id.btnFavorite)

        if (station != null) {

            val nameTextView = findViewById<TextView>(R.id.details_station_name)
            val bikesTextView = findViewById<TextView>(R.id.details_station_bikes)
            val docksTextView = findViewById<TextView>(R.id.details_station_docks)

            nameTextView.text = station.name
            bikesTextView.text = "Vélos disponibles : ${station.bikesAvailable}"
            docksTextView.text = "Bornes libres : ${station.locationAvailable}"

            if (FavoriteManager.isFavorite(station)) {
                favoriteButton.setImageResource(
                    android.R.drawable.btn_star_big_on
                )
            } else {
                favoriteButton.setImageResource(
                    android.R.drawable.btn_star_big_off
                )
            }

            favoriteButton.setOnClickListener {
                if (FavoriteManager.isFavorite(station)) {

                    FavoriteManager.removeFavorite(station)

                    favoriteButton.setImageResource(
                        android.R.drawable.btn_star_big_off
                    )

                } else {

                    FavoriteManager.addFavorite(station)

                    favoriteButton.setImageResource(
                        android.R.drawable.btn_star_big_on
                    )
                }
            }
        }
    }
}


