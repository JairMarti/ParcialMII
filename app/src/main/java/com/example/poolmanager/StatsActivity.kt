package com.example.poolmanager

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class StatsActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var llVentasContainer: LinearLayout
    private lateinit var tvTotalGeneral: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        llVentasContainer = findViewById(R.id.ll_ventas_container)
        tvTotalGeneral = findViewById(R.id.tv_total_general)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        loadDailySales()
    }

    private fun loadDailySales() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.time

        db.collection("ventas")
            .whereGreaterThanOrEqualTo("fecha", startOfDay)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val ventas = snapshot.toObjects(Venta::class.java)
                    displayVentas(ventas)
                }
            }
    }

    private fun displayVentas(ventas: List<Venta>) {
        llVentasContainer.removeAllViews()
        var totalGeneral = 0.0
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        if (ventas.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No hay ventas registradas hoy."
            tvEmpty.setTextColor(android.graphics.Color.GRAY)
            tvEmpty.textAlignment = View.TEXT_ALIGNMENT_CENTER
            llVentasContainer.addView(tvEmpty)
            tvTotalGeneral.text = format.format(0.0)
            return
        }

        for (venta in ventas) {
            totalGeneral += venta.totalVenta

            val card = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                setCardBackgroundColor(android.graphics.Color.parseColor("#1A1A2E"))
                radius = 16f
                elevation = 4f
            }

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            // Nombre Mesa y Total
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            val tvMesa = TextView(this).apply {
                text = venta.mesaNombre
                setTextColor(android.graphics.Color.WHITE)
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvTotalVenta = TextView(this).apply {
                text = format.format(venta.totalVenta)
                setTextColor(android.graphics.Color.parseColor("#BB86FC"))
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            header.addView(tvMesa)
            header.addView(tvTotalVenta)

            // Detalle de Items
            val tvDetalle = TextView(this).apply {
                val itemsStr = if (venta.items.isNotEmpty()) venta.items.joinToString(", ") else "Sin consumos"
                text = "Items: $itemsStr\nTiempo: ${venta.tiempoJugado}"
                setTextColor(android.graphics.Color.parseColor("#BBBBBB"))
                textSize = 12f
                setPadding(0, 8, 0, 0)
            }

            // Desglose
            val tvDesglose = TextView(this).apply {
                text = "Mesa: ${format.format(venta.totalMesa)} | Consumo: ${format.format(venta.totalConsumo)}"
                setTextColor(android.graphics.Color.parseColor("#BBBBBB"))
                textSize = 10f
                setPadding(0, 4, 0, 0)
            }

            layout.addView(header)
            layout.addView(tvDetalle)
            layout.addView(tvDesglose)
            card.addView(layout)
            llVentasContainer.addView(card)
        }

        tvTotalGeneral.text = format.format(totalGeneral)
    }
}
