package com.example.tiorico.ui.waiting

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.game.GameActivity
import com.example.tiorico.viewmodel.GameViewModel

class WaitingRoomActivity : AppCompatActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)

        val tvCode   = findViewById<TextView>(R.id.tvCode)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        val roomId = intent.getStringExtra("roomId")!!
        val code   = intent.getStringExtra("code")!!

        tvCode.text = "Código: $code"

        viewModel.iniciar(roomId)

        viewModel.statusSala.observe(this) { status ->
            when (status) {
                "waiting" -> tvStatus.text = "Esperando jugador..."
                "started" -> {
                    val intent = Intent(this, GameActivity::class.java)
                    intent.putExtra("roomId", roomId)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}