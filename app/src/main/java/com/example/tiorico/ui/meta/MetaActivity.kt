package com.example.tiorico.ui.meta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.waiting.WaitingRoomActivity
import com.example.tiorico.viewmodel.LobbyViewModel

class MetaActivity : AppCompatActivity() {

    // Reutiliza LobbyViewModel — comparte la lógica de crear sala
    private val viewModel: LobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meta)

        findViewById<Button>(R.id.btn3000).setOnClickListener  { viewModel.crearSala(3000) }
        findViewById<Button>(R.id.btn5000).setOnClickListener  { viewModel.crearSala(5000) }
        findViewById<Button>(R.id.btn10000).setOnClickListener { viewModel.crearSala(10000) }

        viewModel.salaCreada.observe(this) { (roomId, code) ->
            val intent = Intent(this, WaitingRoomActivity::class.java)
            intent.putExtra("roomId", roomId)
            intent.putExtra("code", code)
            startActivity(intent)
            finish()
        }
    }
}
