package com.example.tiorico.data.repository

import com.example.tiorico.data.model.GameRoom
import com.google.firebase.database.*

class GameRepository {

    private val db = FirebaseDatabase.getInstance().reference

    // ── Crear sala ──────────────────────────────────────────────
    fun crearSala(
        uid: String,
        email: String,
        meta: Int,
        onCreated: (roomId: String, code: String) -> Unit
    ) {
        val roomId = db.child("gameRooms").push().key!!
        val code = generarCodigo()

        val room = mapOf(
            "goal" to meta,
            "currentTurn" to uid,
            "code" to code,
            "status" to "waiting",
            "player1Id" to uid,
            "player2Id" to ""
        )
        val player = mapOf("money" to 1000, "turn" to true, "email" to email)

        db.child("gameRooms").child(roomId).setValue(room)
        db.child("gameRooms").child(roomId).child("players").child(uid).setValue(player)

        onCreated(roomId, code)
    }

    // ── Unirse por código ────────────────────────────────────────
    fun unirsePorCodigo(
        uid: String,
        email: String,
        code: String,
        onSuccess: (roomId: String) -> Unit,
        onError: () -> Unit
    ) {
        db.child("gameRooms")
            .orderByChild("code").equalTo(code)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) { onError(); return }

                    val roomSnapshot = snapshot.children.first()
                    val roomId = roomSnapshot.key!!

                    db.child("gameRooms").child(roomId).child("player2Id").setValue(uid)
                    db.child("gameRooms").child(roomId).child("players").child(uid)
                        .setValue(mapOf("money" to 1000, "turn" to false, "email" to email))
                    db.child("gameRooms").child(roomId).child("status").setValue("started")

                    onSuccess(roomId)
                }
                override fun onCancelled(error: DatabaseError) { onError() }
            })
    }

    // ── Guardar dinero del jugador ───────────────────────────────
    fun guardarDinero(roomId: String, uid: String, money: Int) {
        db.child("gameRooms").child(roomId)
            .child("players").child(uid).child("money").setValue(money)
    }

    // ── Cambiar turno ────────────────────────────────────────────
    fun cambiarTurno(roomId: String, uid: String, onDone: () -> Unit = {}) {
        db.child("gameRooms").child(roomId).get().addOnSuccessListener { snapshot ->
            val p1 = snapshot.child("player1Id").value.toString()
            val p2 = snapshot.child("player2Id").value.toString()
            val siguiente = if (uid == p1) p2 else p1
            if (siguiente.isNotEmpty()) {
                db.child("gameRooms").child(roomId)
                    .child("currentTurn").setValue(siguiente)
                    .addOnSuccessListener { onDone() }
            }
        }
    }

    // ── Finalizar juego ──────────────────────────────────────────
    fun finalizarJuego(roomId: String, winnerId: String, loserId: String, reason: String) {
        db.child("gameRooms").child(roomId).updateChildren(
            mapOf(
                "status" to "finished",
                "winnerId" to winnerId,
                "loserId" to loserId,
                "reason" to reason
            )
        )
    }

    // ── Abandonar ────────────────────────────────────────────────
    fun abandonarJuego(roomId: String, uid: String) {
        db.child("gameRooms").child(roomId).get().addOnSuccessListener { snapshot ->
            val p1 = snapshot.child("player1Id").value.toString()
            val p2 = snapshot.child("player2Id").value.toString()
            val ganador = if (uid == p1) p2 else p1
            finalizarJuego(roomId, ganador, uid, "abandoned")
        }
    }

    // ── Listeners en tiempo real ─────────────────────────────────
    fun escucharStatus(roomId: String, onChange: (String) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { onChange(s.value.toString()) }
            override fun onCancelled(e: DatabaseError) {}
        }
        db.child("gameRooms").child(roomId).child("status").addValueEventListener(listener)
        return listener
    }

    fun escucharTurno(roomId: String, onChange: (String) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { onChange(s.value.toString()) }
            override fun onCancelled(e: DatabaseError) {}
        }
        db.child("gameRooms").child(roomId).child("currentTurn").addValueEventListener(listener)
        return listener
    }

    fun escucharSala(roomId: String, onChange: (DataSnapshot) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { onChange(s) }
            override fun onCancelled(e: DatabaseError) {}
        }
        db.child("gameRooms").child(roomId).addValueEventListener(listener)
        return listener
    }

    fun removeListener(roomId: String, path: String, listener: ValueEventListener) {
        db.child("gameRooms").child(roomId).child(path).removeEventListener(listener)
    }

    fun removeRoomListener(roomId: String, listener: ValueEventListener) {
        db.child("gameRooms").child(roomId).removeEventListener(listener)
    }

    fun obtenerEmailJugador(
        roomId: String,
        uid: String,
        onResult: (String) -> Unit
    ) {
        db.child("gameRooms").child(roomId)
            .child("players").child(uid).child("email")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    onResult(s.value?.toString() ?: "Desconocido")
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    // ── Helpers ──────────────────────────────────────────────────
    private fun generarCodigo(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}