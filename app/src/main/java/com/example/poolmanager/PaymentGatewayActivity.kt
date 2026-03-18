package com.example.poolmanager

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.ArrayList
import java.util.Locale

class PaymentGatewayActivity : AppCompatActivity() {
    private lateinit var tableManager: TableManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_gateway)

        tableManager = TableManager(this)

        val totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        val consumptions = intent.getStringArrayListExtra("CONSUMPTIONS") ?: arrayListOf()
        val timePlayed = intent.getStringExtra("TIME") ?: "00:00:00"
        val tableId = intent.getIntExtra("TABLE_ID", 1)

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        
        val tables = tableManager.getTables()
        val table = tables.find { it.id == tableId }
        val tableName = table?.name ?: "Mesa $tableId"

        // Update UI components
        findViewById<TextView>(R.id.tv_payment_table_name_header).text = tableName
        findViewById<TextView>(R.id.tv_payment_time).text = timePlayed
        findViewById<TextView>(R.id.tv_payment_total_amount).text = format.format(totalAmount)
        
        // Calcular solo el valor del tiempo para el desglose (asumiendo 2000/min)
        val timeParts = timePlayed.split(":")
        if (timeParts.size == 3) {
            val h = timeParts[0].toInt()
            val m = timeParts[1].toInt()
            val totalMin = (h * 60) + m + 1
            val timeValue = totalMin * 2000.0
            findViewById<TextView>(R.id.tv_payment_rate_total).text = format.format(timeValue)
        }

        findViewById<ImageView>(R.id.btn_back_payment)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_confirm_payment)?.setOnClickListener {
            val radioGroup = findViewById<RadioGroup>(R.id.rg_payment_methods)
            val selectedId = radioGroup.checkedRadioButtonId
            
            if (selectedId == -1) {
                Toast.makeText(this, "Por favor, selecciona un método de pago", Toast.LENGTH_SHORT).show()
            } else {
                val radioButton = findViewById<RadioButton>(selectedId)
                val method = radioButton.text.toString()
                
                tableManager.addDailyBilling(totalAmount)
                
                val currentTables = tableManager.getTables()
                val tableToUpdate = currentTables.find { it.id == tableId }
                tableToUpdate?.let {
                    tableManager.incrementTableUsage(it.name)
                    it.status = "Disponible"
                    it.players = 0
                    it.timePlayed = "00:00:00"
                    it.currentTotal = 0.0
                    it.consumptions.clear()
                    it.startTime = 0
                    tableManager.saveTables(currentTables)
                }

                showInvoice(totalAmount, consumptions, timePlayed, method, tableName)
            }
        }
    }

    private fun showInvoice(total: Double, items: ArrayList<String>, time: String, method: String, tableName: String) {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val sb = StringBuilder()
        sb.append("--- FACTURA DE VENTA ---\n\n")
        sb.append("Local: Pool Manager\n")
        sb.append("Mesa: $tableName\n")
        sb.append("Tiempo jugado: $time\n")
        sb.append("Método de pago: $method\n")
        sb.append("--------------------------\n")
        if (items.isEmpty()) {
            sb.append("Sin consumos adicionales\n")
        } else {
            items.forEach { sb.append("$it\n") }
        }
        sb.append("--------------------------\n")
        sb.append("TOTAL A PAGAR: ${format.format(total)}\n\n")
        sb.append("¡Gracias por su visita!")

        val invoiceText = sb.toString()

        AlertDialog.Builder(this)
            .setTitle("Pago Exitoso")
            .setMessage(invoiceText)
            .setPositiveButton("Finalizar") { _, _ ->
                finishPayment()
            }
            .setNeutralButton("Compartir") { _, _ ->
                shareInvoice(invoiceText)
            }
            .setCancelable(false)
            .show()
    }

    private fun shareInvoice(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir factura vía:")
        startActivity(shareIntent)
        finishPayment()
    }

    private fun finishPayment() {
        Toast.makeText(this, "Proceso completado", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
