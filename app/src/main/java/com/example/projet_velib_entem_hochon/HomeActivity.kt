package com.example.projet_velib_entem_hochon

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        val mapButton = findViewById<Button>(R.id.button_map)
        val favoritesButton = findViewById<Button>(R.id.button_favoris)

        mapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        favoritesButton.setOnClickListener {
            val intent = Intent(this, FavoritesListActivity::class.java)
            startActivity(intent)
        }
    }
}