package com.example.tiorico.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tiorico.data.repository.AuthRepository

class LoginViewModel : ViewModel() {

    private val authRepo = AuthRepository()

    private val _loginResult = MutableLiveData<Pair<Boolean, String?>>()
    val loginResult: LiveData<Pair<Boolean, String?>> = _loginResult

    private val _registerResult = MutableLiveData<Pair<Boolean, String?>>()
    val registerResult: LiveData<Pair<Boolean, String?>> = _registerResult

    fun login(email: String, password: String) {
        authRepo.login(email, password) { ok, err ->
            _loginResult.postValue(Pair(ok, err))
        }
    }

    fun register(email: String, password: String) {
        authRepo.register(email, password) { ok, err ->
            _registerResult.postValue(Pair(ok, err))
        }
    }
}