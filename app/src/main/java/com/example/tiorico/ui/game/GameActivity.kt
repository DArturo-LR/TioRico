package com.example.tiorico.ui.game


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.content.Intent
import com.example.tiorico.ui.login.LoginActivity


class GameActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val roomId = "room1"

    private lateinit var tvMoney: TextView
    private lateinit var tvTurn: TextView
    private lateinit var tvGoal: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvPlayer: TextView
    private lateinit var tvEvent: TextView


    private var money = 1000
    private var goal = 5000
    private var turn = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        goal = intent.getIntExtra("goal", 5000)

        tvMoney = findViewById(R.id.tvMoney)
        tvTurn = findViewById(R.id.tvTurn)
        tvGoal = findViewById(R.id.tvGoal)
        tvResult = findViewById(R.id.tvResult)
        tvPlayer = findViewById(R.id.tvPlayer)
        tvEvent = findViewById(R.id.tvEvent)

        val email = FirebaseAuth.getInstance().currentUser?.email
        tvPlayer.text = "Jugador: $email"

        val btnAhorrar = findViewById<Button>(R.id.btnAhorrar)
        val btnInvertir = findViewById<Button>(R.id.btnInvertir)
        val btnGastar = findViewById<Button>(R.id.btnGastar)
        val btnSalir = findViewById<Button>(R.id.btnSalir)

        actualizarUI()

        btnAhorrar.setOnClickListener {
            money += 200
            siguienteTurno("Ahorraste +200")
        }

        btnInvertir.setOnClickListener {
            val resultado = (-200..500).random()
            money += resultado
            siguienteTurno("Invertiste: $resultado")
        }

        btnGastar.setOnClickListener {
            money -= 150
            siguienteTurno("Gastaste -150")
        }
        btnSalir.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun siguienteTurno(mensaje: String) {
        turn++

        val evento = eventoAleatorio()

        // 👉 Resultado (acción del jugador)
        tvResult.text = mensaje

        // 👉 Evento separado
        if (evento.isNotEmpty()) {
            tvEvent.text = "Evento: $evento"
        } else {
            tvEvent.text = "Evento: No hubo ningún evento"
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

        actualizarUI()
        guardarEnFirebase()
        verificarEstado()
    }

    private fun actualizarUI() {
        tvMoney.text = "Dinero: $money"
        tvTurn.text = "Turno: $turn"
        tvGoal.text = "Meta: $goal"
    }

    private fun guardarEnFirebase() {
        val uid = userId ?: return

        database.child("gameRooms")
            .child(roomId)
            .child("players")
            .child(uid)
            .child("money")
            .setValue(money)
    }

    private fun verificarEstado() {
        if (money >= goal) {
            Toast.makeText(this, "¡Ganaste!", Toast.LENGTH_LONG).show()
            finish()
        } else if (money <= 0) {
            Toast.makeText(this, "Perdiste", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    private fun eventoAleatorio(): String {
        val probabilidad = (1..100).random()

        return if (probabilidad <= 30) { // 30% de probabilidad
            val evento = listOf(
                "Encontraste dinero en la calle +300" to 300,
                "Te robaron dinero -250" to -250,
                "Ganaste un premio +400" to 400,
                "Pagaste una multa -200" to -200
            ).random()

            money += evento.second
            evento.first
        } else {
            "" // no pasa nada
        }
    }
}