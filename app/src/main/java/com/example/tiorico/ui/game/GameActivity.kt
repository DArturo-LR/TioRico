package com.example.tiorico.ui.game


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class GameActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val roomId = "room1"

    private lateinit var tvMoney: TextView
    private lateinit var tvTurn: TextView
    private lateinit var tvGoal: TextView

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

        val btnAhorrar = findViewById<Button>(R.id.btnAhorrar)
        val btnInvertir = findViewById<Button>(R.id.btnInvertir)
        val btnGastar = findViewById<Button>(R.id.btnGastar)

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
    }

    private fun siguienteTurno(mensaje: String) {
        turn++

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
}