package com.example.tiorico.ui.result

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.google.firebase.database.*

class ResultActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var roomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // 🔥 TextViews
        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        val tvDinero = findViewById<TextView>(R.id.tvDineroFinal)
        val tvMeta = findViewById<TextView>(R.id.tvMetaFinal)
        val tvTurnos = findViewById<TextView>(R.id.tvTurnosFinal)
        val tvWinner = findViewById<TextView>(R.id.tvWinner)
        val tvLoser = findViewById<TextView>(R.id.tvLoser)

        val btnReiniciar = findViewById<Button>(R.id.btnReiniciar)

        // 📥 Datos básicos
        val dinero = intent.getIntExtra("money", 0)
        val meta = intent.getIntExtra("goal", 0)
        val turnos = intent.getIntExtra("turn", 0)
        val estado = intent.getStringExtra("estado") ?: ""

        // 📥 Datos multiplayer
        val winnerId = intent.getStringExtra("winnerId")!!
        val loserId = intent.getStringExtra("loserId")!!
        roomId = intent.getStringExtra("roomId")!!

        // 📺 Mostrar datos básicos
        tvEstado.text = estado
        tvDinero.text = "Dinero final: $dinero"
        tvMeta.text = "Meta: $meta"
        tvTurnos.text = "Turnos: $turnos"

        // 🔥 Obtener correos correctamente
        obtenerCorreo(winnerId) { winnerEmail ->

            tvWinner.text = "🏆 Ganador: $winnerEmail"

            obtenerCorreo(loserId) { loserEmail ->
                tvLoser.text = "💀 Perdedor: $loserEmail"
            }
        }

        // 🔁 Volver al lobby
        btnReiniciar.setOnClickListener {
            val intent = Intent(this, com.example.tiorico.ui.lobby.LobbyActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // ============================
    // 🔥 OBTENER CORREO (CORRECTO)
    // ============================
    private fun obtenerCorreo(uid: String, callback: (String) -> Unit) {

        database.child("gameRooms")
            .child(roomId)
            .child("players")
            .child(uid)
            .child("email")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = snapshot.value?.toString() ?: "Desconocido"
                    callback(email)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}