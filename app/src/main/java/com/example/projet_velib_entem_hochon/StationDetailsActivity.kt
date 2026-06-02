package com.example.projet_velib_entem_hochon
//
//import android.os.Bundle
//import android.widget.TextView
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projet_velib_entem_hochon.model.Station
import android.widget.ImageButton

//import com.example.projet_velib_entem_hochon.model.Station
//
class StationDetailsActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_station_details)
//
//        // Récupération de la station envoyée par la liste (via l'Intent)
//        // On utilise "as? Station" pour dire à Kotlin que c'est un objet Station
//        val station = intent.getSerializableExtra("STATION_EXTRA") as? Station
//
//        // Si la station a bien été reçue, on affiche ses données dans le XML
//        if (station != null) {
//            val nameTextView = findViewById<TextView>(R.id.details_station_name)
//            val bikesTextView = findViewById<TextView>(R.id.details_station_bikes)
//            val docksTextView = findViewById<TextView>(R.id.details_station_docks)
//
//            nameTextView.text = station.name
//            bikesTextView.text = "Vélos disponibles : ${station.bikesAvailable}"
//            docksTextView.text = "Bornes libres : ${station.locationAvailable}"
//        }
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_details)

        // Récupération de la station envoyée par l'Intent
        val station = intent.getSerializableExtra("STATION_EXTRA") as? Station

        // Référence du bouton Favori
        val favoriteButton = findViewById<ImageButton>(R.id.btnFavorite)

        // Si la station a bien été reçue
        if (station != null) {

            val nameTextView = findViewById<TextView>(R.id.details_station_name)
            val bikesTextView = findViewById<TextView>(R.id.details_station_bikes)
            val docksTextView = findViewById<TextView>(R.id.details_station_docks)

            nameTextView.text = station.name
            bikesTextView.text = "Vélos disponibles : ${station.bikesAvailable}"
            docksTextView.text = "Bornes libres : ${station.locationAvailable}"

            // Texte initial du bouton
            if (FavoriteManager.isFavorite(station)) {
                favoriteButton.setImageResource(
                    android.R.drawable.btn_star_big_on
                )
            } else {
                favoriteButton.setImageResource(
                    android.R.drawable.btn_star_big_off
                )
            }

            // Gestion du clic
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


