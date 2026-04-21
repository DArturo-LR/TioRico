package com.example.tiorico.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tiorico.data.repository.AuthRepository
import com.example.tiorico.data.repository.GameRepository

class LobbyViewModel : ViewModel() {

    private val gameRepo = GameRepository()
    private val authRepo = AuthRepository()

    // Emite (roomId, code) al crear
    private val _salaCreada = MutableLiveData<Pair<String, String>>()
    val salaCreada: LiveData<Pair<String, String>> = _salaCreada

    // Emite roomId al unirse, null si error
    private val _salaUnida = MutableLiveData<String?>()
    val salaUnida: LiveData<String?> = _salaUnida

    fun crearSala(meta: Int) {
        val uid = authRepo.currentUserId ?: return
        val email = authRepo.currentEmail ?: ""
        gameRepo.crearSala(uid, email, meta) { roomId, code ->
            _salaCreada.postValue(Pair(roomId, code))
        }
    }

    fun unirseSala(code: String) {
        val uid = authRepo.currentUserId ?: return
        val email = authRepo.currentEmail ?: ""
        gameRepo.unirsePorCodigo(
            uid, email, code,
            onSuccess = { roomId -> _salaUnida.postValue(roomId) },
            onError = { _salaUnida.postValue(null) }
        )
    }
}
