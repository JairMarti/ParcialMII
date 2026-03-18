package com.example.poolmanager

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class TableManagementActivity : AppCompatActivity() {
    private lateinit var tableManager: TableManager
    private var currentTable: Table? = null
    
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvTimer: TextView
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    
    private var tableId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_management)

        tableManager = TableManager(this)
        tableId = intent.getIntExtra("TABLE_ID", 1)
        
        loadTableData()

        tvTotalPrice = findViewById(R.id.tv_total_price)
        tvTimer = findViewById(R.id.tv_timer)
        
        updatePriceDisplay()
        startTimer()

        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_add_beer)?.setOnClickListener {
            addConsumption("Cerveza", 5000.0)
        }

        findViewById<Button>(R.id.btn_add_water)?.setOnClickListener {
            addConsumption("Agua", 3000.0)
        }

        findViewById<Button>(R.id.btn_add_snack)?.setOnClickListener {
            addConsumption("Mecato", 4000.0)
        }

        findViewById<Button>(R.id.btn_finish_game)?.setOnClickListener {
            val intent = Intent(this, PaymentGatewayActivity::class.java)
            intent.putExtra("TOTAL_AMOUNT", currentTable?.currentTotal ?: 0.0)
            intent.putExtra("CONSUMPTIONS", ArrayList(currentTable?.consumptions ?: mutableListOf()))
            intent.putExtra("TIME", tvTimer.text.toString())
            intent.putExtra("TABLE_ID", tableId)
            startActivity(intent)
        }
    }

    private fun loadTableData() {
        val tables = tableManager.getTables()
        currentTable = tables.find { it.id == tableId }
        
        currentTable?.let { table ->
            findViewById<TextView>(R.id.tv_table_name)?.text = table.name
            
            if (table.status != "Ocupada") {
                table.status = "Ocupada"
                table.startTime = System.currentTimeMillis()
                table.currentTotal = 2000.0 // Cobro inicial por los primeros 30 min
                table.consumptions = mutableListOf("Tiempo de juego (Bloque 30 min): $2.000")
                tableManager.saveTables(tables)
            }
            
            val now = System.currentTimeMillis()
            seconds = ((now - table.startTime) / 1000).toInt()
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
                
                // Cobro de 2000 COP cada 30 minutos (1800 segundos)
                if (seconds > 0 && seconds % 1800 == 0) {
                    addTimeCharge()
                }
                
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun addTimeCharge() {
        currentTable?.let { table ->
            table.currentTotal += 2000.0
            saveCurrentState()
            updatePriceDisplay()
            Toast.makeText(this, "Nuevo bloque de 30 min cobrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addConsumption(name: String, price: Double) {
        currentTable?.let { table ->
            table.currentTotal += price
            val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
            table.consumptions.add("$name: ${format.format(price)}")
            
            saveCurrentState()
            updatePriceDisplay()
            Toast.makeText(this, "$name añadido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCurrentState() {
        val tables = tableManager.getTables()
        val index = tables.indexOfFirst { it.id == tableId }
        if (index != -1 && currentTable != null) {
            tables[index] = currentTable!!
            tableManager.saveTables(tables)
        }
    }

    private fun updatePriceDisplay() {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        tvTotalPrice.text = format.format(currentTable?.currentTotal ?: 0.0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}