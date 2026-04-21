package com.example.tiorico.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.example.tiorico.ui.lobby.LobbyActivity
import com.example.tiorico.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail    = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin   = findViewById<Button>(R.id.btnLogin)
        val btnRegister= findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass  = etPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) viewModel.login(email, pass)
            else Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val pass  = etPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) viewModel.register(email, pass)
            else Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
        }

        viewModel.loginResult.observe(this) { (ok, err) ->
            if (ok) {
                startActivity(Intent(this, LobbyActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, err ?: "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.registerResult.observe(this) { (ok, err) ->
            if (ok) {
                Toast.makeText(this, "Usuario creado", Toast.LENGTH_SHORT).show()
                etEmail.text.clear(); etPassword.text.clear()
            } else {
                Toast.makeText(this, err ?: "Error al registrar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}