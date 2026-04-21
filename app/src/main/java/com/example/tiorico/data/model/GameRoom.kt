package com.example.tiorico.data.model

data class GameRoom(
    val goal: Int = 5000,
    val currentTurn: String = "",
    val code: String = "",
    val status: String = "waiting",
    val player1Id: String = "",
    val player2Id: String = "",
    val winnerId: String = "",
    val loserId: String = "",
    val reason: String = ""
)