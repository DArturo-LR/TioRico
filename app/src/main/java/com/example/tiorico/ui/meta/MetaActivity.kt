package com.example.tiorico.ui.meta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.waiting.WaitingRoomActivity
import com.example.tiorico.viewmodel.LobbyViewModel

class MetaActivity : AppCompatActivity() {

    private val viewModel: LobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meta)

        val etCustomCode = findViewById<EditText>(R.id.etCustomCode)
        val btn3000 = findViewById<Button>(R.id.btn3000)
        val btn5000 = findViewById<Button>(R.id.btn5000)
        val btn10000 = findViewById<Button>(R.id.btn10000)

        fun intentarCrearSala(meta: Int) {
            val code = etCustomCode.text.toString().trim().uppercase()
            if (code.isNotEmpty()) {
                viewModel.crearSala(meta, code)
            } else {
                Toast.makeText(this, "Por favor, ingresa un código para tu sala", Toast.LENGTH_SHORT).show()
            }
        }

        btn3000.setOnClickListener  { intentarCrearSala(3000) }
        btn5000.setOnClickListener  { intentarCrearSala(5000) }
        btn10000.setOnClickListener { intentarCrearSala(10000) }

        viewModel.salaCreada.observe(this) { (roomId, code) ->
            val intent = Intent(this, WaitingRoomActivity::class.java)
            intent.putExtra("roomId", roomId)
            intent.putExtra("code", code)
            startActivity(intent)
            finish()
        }
    }
}
