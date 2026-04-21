package com.example.tiorico.ui.result

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.lobby.LobbyActivity
import com.example.tiorico.viewmodel.GameViewModel

class ResultActivity : AppCompatActivity() {

    // Reutiliza GameViewModel solo para obtener emails
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tvEstado  = findViewById<TextView>(R.id.tvEstado)
        val tvDinero  = findViewById<TextView>(R.id.tvDineroFinal)
        val tvMeta    = findViewById<TextView>(R.id.tvMetaFinal)
        val tvTurnos  = findViewById<TextView>(R.id.tvTurnosFinal)
        val tvWinner  = findViewById<TextView>(R.id.tvWinner)
        val tvLoser   = findViewById<TextView>(R.id.tvLoser)
        val btnVolver = findViewById<Button>(R.id.btnReiniciar)

        val dinero   = intent.getIntExtra("money", 0)
        val meta     = intent.getIntExtra("goal", 0)
        val turnos   = intent.getIntExtra("turn", 0)
        val estado   = intent.getStringExtra("estado") ?: ""
        val winnerId = intent.getStringExtra("winnerId")!!
        val loserId  = intent.getStringExtra("loserId")!!
        val roomId   = intent.getStringExtra("roomId")!!

        tvEstado.text = estado
        tvDinero.text = "Dinero final: $dinero"
        tvMeta.text   = "Meta: $meta"
        tvTurnos.text = "Turnos: $turnos"

        // El ViewModel necesita el roomId para buscar emails
        viewModel.iniciar(roomId)
        viewModel.obtenerEmailJugador(winnerId) { winnerEmail ->
            tvWinner.text = "🏆 Ganador: $winnerEmail"
        }
        viewModel.obtenerEmailJugador(loserId) { loserEmail ->
            tvLoser.text = "💀 Perdedor: $loserEmail"
        }

        btnVolver.setOnClickListener {
            startActivity(Intent(this, LobbyActivity::class.java))
            finish()
        }
    }
}