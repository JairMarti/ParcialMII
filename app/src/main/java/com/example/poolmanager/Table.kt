package com.example.poolmanager

data class Table(
    val id: Int,
    var name: String,
    var status: String, // "Disponible", "Ocupada", "Mantenimiento"
    var lastGame: String = "No registrado",
    var players: Int = 0,
    var timePlayed: String = "00:00:00",
    var startTime: Long = 0,
    var currentTotal: Double = 0.0,
    var consumptions: MutableList<String> = mutableListOf()
)
