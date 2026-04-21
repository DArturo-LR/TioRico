package com.example.tiorico.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tiorico.data.repository.AuthRepository
import com.example.tiorico.data.repository.GameRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener

data class ResultadoFinal(
    val estado: String,
    val winnerId: String,
    val loserId: String,
    val roomId: String,
    val money: Int,
    val goal: Int,
    val turn: Int
)

class GameViewModel : ViewModel() {

    private val gameRepo = GameRepository()
    private val authRepo = AuthRepository()

    val userId get() = authRepo.currentUserId
    val userEmail get() = authRepo.currentEmail

    // ── Estado observable ────────────────────────────────────────
    private val _money = MutableLiveData(1000)
    val money: LiveData<Int> = _money

    private val _goal = MutableLiveData(5000)
    val goal: LiveData<Int> = _goal

    private val _turn = MutableLiveData(1)
    val turn: LiveData<Int> = _turn

    private val _esMiTurno = MutableLiveData(false)
    val esMiTurno: LiveData<Boolean> = _esMiTurno

    private val _statusSala = MutableLiveData<String>()
    val statusSala: LiveData<String> = _statusSala

    private val _accion = MutableLiveData<String>()
    val accion: LiveData<String> = _accion

    private val _evento = MutableLiveData<String>()
    val evento: LiveData<String> = _evento

    private val _resultado = MutableLiveData<ResultadoFinal?>()
    val resultado: LiveData<ResultadoFinal?> = _resultado

    // ── Internos ─────────────────────────────────────────────────
    private lateinit var roomId: String
    private var listenerStatus: ValueEventListener? = null
    private var listenerTurno: ValueEventListener? = null
    private var listenerSala: ValueEventListener? = null
    private var resultadoYaMostrado = false

    // ── Inicialización ───────────────────────────────────────────
    fun iniciar(roomId: String) {
        this.roomId = roomId
        iniciarListeners()
    }

    private fun iniciarListeners() {
        listenerStatus = gameRepo.escucharStatus(roomId) { status ->
            _statusSala.postValue(status)
        }

        listenerTurno = gameRepo.escucharTurno(roomId) { turnoActual ->
            _esMiTurno.postValue(turnoActual == userId)
        }

        listenerSala = gameRepo.escucharSala(roomId) { snapshot ->
            procesarSnapshotSala(snapshot)
        }
    }

    private fun procesarSnapshotSala(snapshot: DataSnapshot) {
        if (!snapshot.exists()) return

        val goal = snapshot.child("goal").getValue(Int::class.java) ?: 5000
        _goal.postValue(goal)

        val winnerId = snapshot.child("winnerId").value?.toString() ?: return
        if (winnerId.isEmpty() || resultadoYaMostrado) return

        resultadoYaMostrado = true

        val p1 = snapshot.child("player1Id").value.toString()
        val p2 = snapshot.child("player2Id").value.toString()
        val loserId = if (winnerId == p1) p2 else p1
        val reason = snapshot.child("reason").value.toString()

        val estado = when (reason) {
            "abandoned" -> if (winnerId == userId) "¡Ganaste! (oponente abandonó)" else "Perdiste (abandonaste)"
            "money"     -> if (winnerId == userId) "¡Ganaste! (oponente sin dinero)" else "Perdiste (te quedaste sin dinero)"
            "goal"      -> if (winnerId == userId) "¡Ganaste! (alcanzaste la meta)" else "Perdiste (oponente alcanzó la meta)"
            else        -> "Juego terminado"
        }

        _resultado.postValue(
            ResultadoFinal(estado, winnerId, loserId, roomId, _money.value ?: 0, goal, _turn.value ?: 0)
        )
    }

    // ── Acciones del jugador ─────────────────────────────────────
    fun ahorrar() = ejecutarAccion("Ahorraste +200") { _money.value!! + 200 }

    fun invertir() {
        val resultado = (-200..500).random()
        ejecutarAccion("Invertiste: $resultado") { _money.value!! + resultado }
    }

    fun gastar() = ejecutarAccion("Gastaste -150") { _money.value!! - 150 }

    private fun ejecutarAccion(mensaje: String, calcularNuevoDinero: () -> Int) {
        val nuevoMonto = calcularNuevoDinero()
        _money.postValue(nuevoMonto)
        _turn.postValue((_turn.value ?: 1) + 1)
        _accion.postValue(mensaje)

        val evento = eventoAleatorio(nuevoMonto)
        _evento.postValue(evento.first)

        val montoFinal = nuevoMonto + evento.second
        _money.postValue(montoFinal)

        gameRepo.guardarDinero(roomId, userId ?: return, montoFinal)
        verificarEstado(montoFinal)
        gameRepo.cambiarTurno(roomId, userId ?: return)
    }

    fun abandonar() {
        gameRepo.abandonarJuego(roomId, userId ?: return)
    }

    // ── Lógica de juego ──────────────────────────────────────────
    private fun eventoAleatorio(moneyActual: Int): Pair<String, Int> {
        return if ((1..100).random() <= 30) {
            val evento = listOf(
                "Encontraste dinero en la calle +300" to 300,
                "Te robaron dinero -250" to -250,
                "Ganaste un premio +400" to 400,
                "Pagaste una multa -200" to -200
            ).random()
            evento
        } else {
            "No hubo ningún evento" to 0
        }
    }

    private fun verificarEstado(montoFinal: Int) {
        val uid = userId ?: return
        val goal = _goal.value ?: 5000

        gameRepo.escucharSala(roomId) { snapshot ->
            val p1 = snapshot.child("player1Id").value.toString()
            val p2 = snapshot.child("player2Id").value.toString()
            val oponente = if (uid == p1) p2 else p1

            when {
                montoFinal >= goal ->
                    gameRepo.finalizarJuego(roomId, uid, oponente, "goal")
                montoFinal <= 0 ->
                    gameRepo.finalizarJuego(roomId, oponente, uid, "money")
            }
        }
    }

    fun obtenerEmailJugador(uid: String, onResult: (String) -> Unit) {
        gameRepo.obtenerEmailJugador(roomId, uid, onResult)
    }

    // ── Limpieza ─────────────────────────────────────────────────
    override fun onCleared() {
        super.onCleared()
        listenerStatus?.let { gameRepo.removeListener(roomId, "status", it) }
        listenerTurno?.let { gameRepo.removeListener(roomId, "currentTurn", it) }
        listenerSala?.let { gameRepo.removeRoomListener(roomId, it) }
    }
}
