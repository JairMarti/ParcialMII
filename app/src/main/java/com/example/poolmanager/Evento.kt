package com.example.poolmanager

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Evento(
    var id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: Long = 0L, // Timestamp en milisegundos
    @ServerTimestamp val fecha_creacion: Date? = null
)
