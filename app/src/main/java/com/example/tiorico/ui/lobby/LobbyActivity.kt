package com.example.tiorico.ui.lobby

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.game.GameActivity
import com.example.tiorico.ui.meta.MetaActivity

class LobbyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        val btnCrear = findViewById<Button>(R.id.btnCrear)
        val btnUnirse = findViewById<Button>(R.id.btnUnirse)
        val etCode = findViewById<EditText>(R.id.etCode)

        btnCrear.setOnClickListener {
            startActivity(Intent(this, MetaActivity::class.java))
        }

        btnUnirse.setOnClickListener {
            val code = etCode.text.toString()

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("joinCode", code)
            startActivity(intent)
        }
    }
}