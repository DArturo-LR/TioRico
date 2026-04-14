package com.example.tiorico.ui.login


import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.google.firebase.auth.FirebaseAuth
import com.example.tiorico.ui.meta.MetaActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                login(email, pass)
            } else {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                register(email, pass)
            } else {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Login correcto", Toast.LENGTH_SHORT).show()

                    // 👉 IR A META
                    startActivity(Intent(this, com.example.tiorico.ui.lobby.LobbyActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun register(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Usuario creado", Toast.LENGTH_SHORT).show()
                    findViewById<EditText>(R.id.etEmail).text.clear()
                    findViewById<EditText>(R.id.etPassword).text.clear()
                } else {
                    Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show()
                }
            }
    }
}