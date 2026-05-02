package com.example.poolmanager

import com.google.firebase.Timestamp

data class Venta(
    var id: String = "",
    val mesaId: String = "",
    val mesaNombre: String = "",
    val items: List<String> = emptyList(),
    val totalMesa: Double = 0.0,
    val totalConsumo: Double = 0.0,
    val totalVenta: Double = 0.0,
    val tiempoJugado: String = "",
    val fecha: Timestamp = Timestamp.now()
)
