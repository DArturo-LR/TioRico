package com.example.tiorico.ui.game


import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tiorico.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GameActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var roomId: String

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

    private var money = 1000
    private var goal = 5000
    private var turn = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // 🔥 Obtener datos del intent
        val joinCode = intent.getStringExtra("joinCode")

        // 🔥 UI
        tvMoney = findViewById(R.id.tvMoney)
        tvTurn = findViewById(R.id.tvTurn)
        tvGoal = findViewById(R.id.tvGoal)
        tvResult = findViewById(R.id.tvResult)
        tvPlayer = findViewById(R.id.tvPlayer)
        tvEvent = findViewById(R.id.tvEvent)

        btnAhorrar = findViewById(R.id.btnAhorrar)
        btnInvertir = findViewById(R.id.btnInvertir)
        btnGastar = findViewById(R.id.btnGastar)
        btnSalir = findViewById(R.id.btnSalir)

        // 🔥 Mostrar jugador
        val email = FirebaseAuth.getInstance().currentUser?.email
        tvPlayer.text = "Jugador: $email"

        bloquearBotones()

        if (joinCode != null) {
            unirseSala(joinCode)
        } else {
            roomId = intent.getStringExtra("roomId")!!
            iniciarListeners()
        }

        actualizarUI()

        // 🔥 BOTONES
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
            salirDelJuego()
        }
    }

    // ============================
    // 🔥 INICIAR LISTENERS
    // ============================

    private fun iniciarListeners() {
        escucharEstadoSala()
        escucharMeta()
        escucharTurno()
        escucharResultadoFinal()
    }

    // ============================
    // 🔒 BOTONES
    // ============================

    private fun bloquearBotones() {
        btnAhorrar.isEnabled = false
        btnInvertir.isEnabled = false
        btnGastar.isEnabled = false
    }

    private fun activarBotones() {
        btnAhorrar.isEnabled = true
        btnInvertir.isEnabled = true
        btnGastar.isEnabled = true
    }

    // ============================
    // 🔥 ESTADO SALA
    // ============================

    private fun escucharEstadoSala() {
        database.child("gameRooms")
            .child(roomId)
            .child("status")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val status = snapshot.value.toString()

                    when (status) {

                        "waiting" -> {
                            tvTurn.text = "Estado: Esperando jugador..."
                            bloquearBotones()
                        }

                        "started" -> {
                            tvTurn.text = "Estado: Juego iniciado"
                        }

                        "finished" -> {
                            bloquearBotones()
                        }

                        "abandoned" -> {
                            bloquearBotones()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ============================
    // 🔥 META
    // ============================

    private fun escucharMeta() {
        database.child("gameRooms")
            .child(roomId)
            .child("goal")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    goal = snapshot.getValue(Int::class.java) ?: 5000
                    tvGoal.text = "Meta: $goal"
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ============================
    // 🔥 TURNOS
    // ============================

    private fun escucharTurno() {

        database.child("gameRooms")
            .child(roomId)
            .child("currentTurn")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val turnoActual = snapshot.value.toString()

                    if (turnoActual == userId) {
                        tvTurn.text = "🟢 Tu turno"
                        activarBotones()
                    } else {
                        tvTurn.text = "🔴 Turno del oponente"
                        bloquearBotones()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cambiarTurno() {

        val uid = userId ?: return

        database.child("gameRooms")
            .child(roomId)
            .get()
            .addOnSuccessListener { snapshot ->

                val player1 = snapshot.child("player1Id").value.toString()
                val player2 = snapshot.child("player2Id").value.toString()

                val siguienteTurno = if (uid == player1) player2 else player1

                if (siguienteTurno.isNotEmpty()) {
                    database.child("gameRooms")
                        .child(roomId)
                        .child("currentTurn")
                        .setValue(siguienteTurno)
                }
            }
    }

    // ============================
    // 🔥 UNIRSE
    // ============================

    private fun unirseSala(code: String) {

        val uid = userId ?: return
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

        database.child("gameRooms")
            .orderByChild("code")
            .equalTo(code)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        val roomSnapshot = snapshot.children.first()
                        roomId = roomSnapshot.key!!

                        // 🔥 Guardar player2Id
                        database.child("gameRooms")
                            .child(roomId)
                            .child("player2Id")
                            .setValue(uid)

                        // 🔥 GUARDAR TAMBIÉN EN players (ESTO ES LO QUE TE FALTABA)
                        database.child("gameRooms")
                            .child(roomId)
                            .child("players")
                            .child(uid)
                            .setValue(
                                mapOf(
                                    "money" to 1000,
                                    "turn" to false,
                                    "email" to email
                                )
                            )

                        // 🔥 Cambiar estado a iniciado
                        database.child("gameRooms")
                            .child(roomId)
                            .child("status")
                            .setValue("started")

                        // 🔥 Iniciar listeners
                        iniciarListeners()

                    } else {
                        Toast.makeText(this@GameActivity, "Código no existe", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ============================
    // 🎮 JUEGO
    // ============================

    private fun siguienteTurno(mensaje: String) {
        turn++

        val evento = eventoAleatorio()

        // 👉 SOLO ACCIÓN
        tvResult.text = "🎮 Acción: $mensaje"

        // 👉 EVENTO SEPARADO
        tvEvent.text = if (evento.isNotEmpty()) {
            "🎲 Evento: $evento"
        } else {
            "🎲 Evento: No hubo ningún evento"
        }

        actualizarUI()
        guardarEnFirebase()
        verificarEstado()
        cambiarTurno()
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

    // ============================
    // 🏁 RESULTADO
    // ============================

    private fun verificarEstado() {

        val uid = userId ?: return

        database.child("gameRooms")
            .child(roomId)
            .get()
            .addOnSuccessListener { snapshot ->

                val player1 = snapshot.child("player1Id").value.toString()
                val player2 = snapshot.child("player2Id").value.toString()

                val oponente = if (uid == player1) player2 else player1

                // 🟢 GANÓ POR META
                if (money >= goal) {
                    database.child("gameRooms")
                        .child(roomId)
                        .updateChildren(
                            mapOf(
                                "status" to "finished",
                                "winnerId" to uid,
                                "loserId" to oponente,
                                "reason" to "goal"
                            )
                        )
                }

                // 🔴 PERDIÓ POR DINERO
                else if (money <= 0) {
                    database.child("gameRooms")
                        .child(roomId)
                        .updateChildren(
                            mapOf(
                                "status" to "finished",
                                "winnerId" to oponente,
                                "loserId" to uid,
                                "reason" to "money"
                            )
                        )
                }
            }
    }

    private fun irAResultado(estado: String, winnerId: String, loserId: String) {

        val intent = Intent(this, com.example.tiorico.ui.result.ResultActivity::class.java)

        intent.putExtra("money", money)
        intent.putExtra("goal", goal)
        intent.putExtra("turn", turn)
        intent.putExtra("estado", estado)

        // 🔥 nuevos datos
        intent.putExtra("winnerId", winnerId)
        intent.putExtra("loserId", loserId)
        intent.putExtra("roomId", roomId)

        startActivity(intent)
        finish()
    }

    // ============================
    // 🎲 EVENTOS
    // ============================

    private fun eventoAleatorio(): String {
        val probabilidad = (1..100).random()

        return if (probabilidad <= 30) {
            val evento = listOf(
                "Encontraste dinero en la calle +300" to 300,
                "Te robaron dinero -250" to -250,
                "Ganaste un premio +400" to 400,
                "Pagaste una multa -200" to -200
            ).random()

            money += evento.second
            evento.first
        } else {
            ""
        }
    }

    // ============================
    // 🚪 SALIR
    // ============================

    private fun salirDelJuego() {

        val uid = userId ?: return

        database.child("gameRooms")
            .child(roomId)
            .get()
            .addOnSuccessListener { snapshot ->

                val player1 = snapshot.child("player1Id").value.toString()
                val player2 = snapshot.child("player2Id").value.toString()

                val ganador = if (uid == player1) player2 else player1

                database.child("gameRooms")
                    .child(roomId)
                    .updateChildren(
                        mapOf(
                            "status" to "abandoned",
                            "winnerId" to ganador,
                            "loserId" to uid,
                            "reason" to "abandoned"
                        )
                    )
            }

        finish()
    }
    // ============================
    // 🚪 verificar quien gano
    // ============================
    private fun escucharResultadoFinal() {

        database.child("gameRooms")
            .child(roomId)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (!snapshot.exists()) return

                    val winnerId = snapshot.child("winnerId").value?.toString() ?: return
                    val player1 = snapshot.child("player1Id").value.toString()
                    val player2 = snapshot.child("player2Id").value.toString()
                    val status = snapshot.child("status").value.toString()
                    val reason = snapshot.child("reason").value.toString()

                    val loserId = if (winnerId == player1) player2 else player1

                    val estado = when (reason) {

                        "abandoned" -> {
                            if (winnerId == userId) "¡Ganaste! (oponente abandonó)"
                            else "Perdiste (abandonaste)"
                        }

                        "money" -> {
                            if (winnerId == userId) "¡Ganaste! (oponente sin dinero)"
                            else "Perdiste (te quedaste sin dinero)"
                        }

                        "goal" -> {
                            if (winnerId == userId) "¡Ganaste! (alcanzaste la meta)"
                            else "Perdiste (oponente alcanzó la meta)"
                        }

                        else -> "Juego terminado"
                    }

                    irAResultado(estado, winnerId, loserId)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}