package com.example.poolmanager

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.NumberFormat
import java.util.Locale

class TableManagementActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private var mesaId: String? = null
    private var currentMesa: Mesa? = null
    
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvTimer: TextView
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    
    private var totalConsumo = 0.0
    private val consumosList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_management)

        mesaId = intent.getStringExtra("TABLE_ID")
        
        tvTotalPrice = findViewById(R.id.tv_total_price)
        tvTimer = findViewById(R.id.tv_timer)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        loadMesaData()
        setupButtons()
    }

    private fun loadMesaData() {
        mesaId?.let { id ->
            db.collection("mesas").document(id).get().addOnSuccessListener { doc ->
                currentMesa = doc.toObject(Mesa::class.java)?.apply { this.id = doc.id }
                findViewById<TextView>(R.id.tv_table_name).text = currentMesa?.nombre ?: "Mesa"
                
                if (currentMesa?.estado == "disponible") {
                    updateMesaStatus("ocupada")
                }
                
                startTimer()
            }
        }
    }

    private fun updateMesaStatus(nuevoEstado: String) {
        mesaId?.let { id ->
            db.collection("mesas").document(id).update("estado", nuevoEstado)
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btn_add_beer).setOnClickListener { addConsumption("Cerveza", 5000.0) }
        findViewById<Button>(R.id.btn_add_water).setOnClickListener { addConsumption("Agua", 3000.0) }
        findViewById<Button>(R.id.btn_add_snack).setOnClickListener { addConsumption("Mecato", 4000.0) }

        findViewById<Button>(R.id.btn_finish_game).setOnClickListener {
            showFinishDialog()
        }
    }

    private fun startTimer() {
        runnable = object : Runnable {
            override fun run() {
                seconds++
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
                
                updatePriceDisplay()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun addConsumption(name: String, price: Double) {
        totalConsumo += price
        consumosList.add(name)
        updatePriceDisplay()
        Toast.makeText(this, "$name añadido", Toast.LENGTH_SHORT).show()
    }

    private fun updatePriceDisplay() {
        val precioPorHora = currentMesa?.precio ?: 2000.0
        val precioTiempo = (seconds.toDouble() / 3600.0) * precioPorHora
        val total = precioTiempo + totalConsumo
        
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        tvTotalPrice.text = format.format(total)
    }

    private fun showFinishDialog() {
        val precioPorHora = currentMesa?.precio ?: 2000.0
        val precioTiempo = (seconds.toDouble() / 3600.0) * precioPorHora
        val total = precioTiempo + totalConsumo

        AlertDialog.Builder(this)
            .setTitle("Finalizar Juego")
            .setMessage("Total a cobrar: ${NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(total)}\n¿Deseas finalizar?")
            .setPositiveButton("Sí, Cobrar y Finalizar") { _, _ ->
                saveVenta(precioTiempo, totalConsumo, total)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveVenta(totalMesa: Double, totalConsumo: Double, totalVenta: Double) {
        val venta = Venta(
            mesaId = mesaId ?: "",
            mesaNombre = currentMesa?.nombre ?: "Desconocida",
            items = consumosList,
            totalMesa = totalMesa,
            totalConsumo = totalConsumo,
            totalVenta = totalVenta,
            tiempoJugado = tvTimer.text.toString()
        )

        db.collection("ventas").add(venta).addOnSuccessListener {
            updateMesaStatus("disponible")
            Toast.makeText(this, "Venta registrada con éxito", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al registrar venta", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::runnable.isInitialized) handler.removeCallbacks(runnable)
    }
}
