package com.example.tiorico.ui.waiting

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.game.GameActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WaitingRoomActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var roomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)

        val tvCode = findViewById<TextView>(R.id.tvCode)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        roomId = intent.getStringExtra("roomId")!!
        val code = intent.getStringExtra("code")!!

        tvCode.text = "Código: $code"

        // 🔥 ESCUCHAR ESTADO
        database.child("gameRooms")
            .child(roomId)
            .child("status")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.value.toString()

                    if (status == "waiting") {
                        tvStatus.text = "Esperando jugador..."
                    } else if (status == "started") {
                        irAlJuego()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun irAlJuego() {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("roomId", roomId)
        startActivity(intent)
        finish()
    }
}