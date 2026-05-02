package com.example.poolmanager

data class Producto(
    var id: String = "",
    val nombre: String = "",
    val cantidad: Int = 0,
    val precio: Double = 0.0,
    val categoria: String = "" // Bebida, Snack, etc.
)
