package com.example.tiorico.ui.lobby

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.game.GameActivity
import com.example.tiorico.ui.meta.MetaActivity
import com.example.tiorico.viewmodel.LobbyViewModel

class LobbyActivity : AppCompatActivity() {

    private val viewModel: LobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        val btnCrearContainer = findViewById<View>(R.id.btnCrearContainer)
        val btnUnirse = findViewById<Button>(R.id.btnUnirse)
        val etCode = findViewById<EditText>(R.id.etCode)

        // Usamos el contenedor interno que tiene el efecto de clic (ripple)
        btnCrearContainer.setOnClickListener {
            startActivity(Intent(this, MetaActivity::class.java))
        }

        btnUnirse.setOnClickListener {
            val code = etCode.text.toString().trim()
            if (code.isNotEmpty()) {
                viewModel.unirseSala(code)
            } else {
                Toast.makeText(this, "Ingresa un código", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.salaUnida.observe(this) { roomId ->
            if (roomId != null) {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("roomId", roomId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Código no existe", Toast.LENGTH_SHORT).show()
            }
        }
    }
}