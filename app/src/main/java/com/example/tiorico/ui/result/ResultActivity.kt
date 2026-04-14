package com.example.tiorico.ui.result

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.meta.MetaActivity

class ResultActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        val tvDinero = findViewById<TextView>(R.id.tvDineroFinal)
        val tvMeta = findViewById<TextView>(R.id.tvMetaFinal)
        val tvTurnos = findViewById<TextView>(R.id.tvTurnosFinal)
        val btnReiniciar = findViewById<Button>(R.id.btnReiniciar)

        // 📥 Recibir datos
        val dinero = intent.getIntExtra("money", 0)
        val meta = intent.getIntExtra("goal", 0)
        val turnos = intent.getIntExtra("turn", 0)
        val estado = intent.getStringExtra("estado")

        // 📺 Mostrar
        tvEstado.text = estado
        tvDinero.text = "Dinero final: $dinero"
        tvMeta.text = "Meta: $meta"
        tvTurnos.text = "Turnos: $turnos"

        // 🔁 Volver a jugar
        btnReiniciar.setOnClickListener {
            val intent = Intent(this, MetaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}