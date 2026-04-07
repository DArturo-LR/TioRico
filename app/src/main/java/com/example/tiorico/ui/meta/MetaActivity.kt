package com.example.tiorico.ui.meta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.game.GameActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MetaActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meta)

        val btn3000 = findViewById<Button>(R.id.btn3000)
        val btn5000 = findViewById<Button>(R.id.btn5000)
        val btn10000 = findViewById<Button>(R.id.btn10000)

        btn3000.setOnClickListener { crearSala(3000) }
        btn5000.setOnClickListener { crearSala(5000) }
        btn10000.setOnClickListener { crearSala(10000) }
    }

    private fun crearSala(meta: Int) {

        val uid = userId ?: return

        val roomId = "room1"

        val room = mapOf(
            "goal" to meta,
            "turnNumber" to 1,
            "currentTurn" to uid
        )

        val player = mapOf(
            "money" to 1000,
            "turn" to true
        )

        database.child("gameRooms").child(roomId).setValue(room)

        database.child("gameRooms")
            .child(roomId)
            .child("players")
            .child(uid)
            .setValue(player)

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("goal", meta)
        startActivity(intent)
    }
}
