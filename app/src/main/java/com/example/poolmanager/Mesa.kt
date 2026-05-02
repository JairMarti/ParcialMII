package com.example.poolmanager

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Mesa(
    var id: String = "",
    val nombre: String = "",
    val estado: String = "disponible", // disponible, ocupada, mantenimiento
    val precio: Double = 0.0,
    @ServerTimestamp val fecha_creacion: Date? = null
)
