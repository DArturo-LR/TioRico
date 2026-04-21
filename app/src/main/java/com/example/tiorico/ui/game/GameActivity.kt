package com.example.tiorico.ui.game

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.result.ResultActivity
import com.example.tiorico.viewmodel.GameViewModel

class GameActivity : AppCompatActivity() {

    private val viewModel: GameViewModel by viewModels()

    private lateinit var tvMoney: TextView
    private lateinit var tvTurn: TextView
    private lateinit var tvGoal: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvPlayer: TextView
    private lateinit var tvEvent: TextView
    private lateinit var btnAhorrar: Button
    private lateinit var btnInvertir: Button
    private lateinit var btnGastar: Button
    private lateinit var btnSalir: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvMoney  = findViewById(R.id.tvMoney)
        tvTurn   = findViewById(R.id.tvTurn)
        tvGoal   = findViewById(R.id.tvGoal)
        tvResult = findViewById(R.id.tvResult)
        tvPlayer = findViewById(R.id.tvPlayer)
        tvEvent  = findViewById(R.id.tvEvent)
        btnAhorrar  = findViewById(R.id.btnAhorrar)
        btnInvertir = findViewById(R.id.btnInvertir)
        btnGastar   = findViewById(R.id.btnGastar)
        btnSalir    = findViewById(R.id.btnSalir)

        tvPlayer.text = "Jugador: ${viewModel.userEmail}"

        val roomId = intent.getStringExtra("roomId")!!
        viewModel.iniciar(roomId)

        // Observadores
        viewModel.money.observe(this)      { tvMoney.text = "Dinero: $it" }
        viewModel.goal.observe(this)       { tvGoal.text  = "Meta: $it" }
        viewModel.turn.observe(this)       { tvTurn.text  = "Turno: $it" }
        viewModel.accion.observe(this)     { tvResult.text = "🎮 Acción: $it" }
        viewModel.evento.observe(this)     { tvEvent.text  = "🎲 Evento: $it" }

        viewModel.esMiTurno.observe(this) { esMiTurno ->
            btnAhorrar.isEnabled  = esMiTurno
            btnInvertir.isEnabled = esMiTurno
            btnGastar.isEnabled   = esMiTurno
        }

        viewModel.statusSala.observe(this) { status ->
            when (status) {
                "waiting"   -> tvTurn.text = "Esperando oponente..."
                "abandoned" -> setBotonessEnabled(false)
                "finished"  -> setBotonessEnabled(false)
            }
        }

        viewModel.resultado.observe(this) { resultado ->
            resultado ?: return@observe
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("money",    resultado.money)
                putExtra("goal",     resultado.goal)
                putExtra("turn",     resultado.turn)
                putExtra("estado",   resultado.estado)
                putExtra("winnerId", resultado.winnerId)
                putExtra("loserId",  resultado.loserId)
                putExtra("roomId",   resultado.roomId)
            }
            startActivity(intent)
            finish()
        }

        // Botones
        btnAhorrar.setOnClickListener  { viewModel.ahorrar() }
        btnInvertir.setOnClickListener { viewModel.invertir() }
        btnGastar.setOnClickListener   { viewModel.gastar() }
        btnSalir.setOnClickListener    { viewModel.abandonar(); finish() }
    }

    private fun setBotonessEnabled(enabled: Boolean) {
        btnAhorrar.isEnabled  = enabled
        btnInvertir.isEnabled = enabled
        btnGastar.isEnabled   = enabled
    }
}