package com.example.projet_velib_entem_hochon
//
//import android.os.Bundle
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.example.projet_velib_entem_hochon.model.Station
//
//class StationDetailsActivity : AppCompatActivity() {
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
//            docksTextView.text = "Places libres : ${station.locationAvailable}"
//        }
//    }
//}


