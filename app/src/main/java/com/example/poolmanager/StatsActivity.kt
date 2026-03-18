package com.example.poolmanager

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsActivity : AppCompatActivity() {
    private lateinit var tableManager: TableManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        tableManager = TableManager(this)

        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        displayDailyReport()
        displayBillingHistory()
        displayTableUsageStatistics()
    }

    private fun displayDailyReport() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyBilling = tableManager.getDailyBilling(today)
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        findViewById<TextView>(R.id.tv_stats_daily_billing).text = format.format(dailyBilling)
    }

    private fun displayBillingHistory() {
        val container = findViewById<LinearLayout>(R.id.ll_billing_history_container)
        container.removeAllViews()

        val history = tableManager.getAllBillingHistory()
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        if (history.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No hay registros anteriores."
            tvEmpty.setTextColor(getColor(R.color.text_hint))
            container.addView(tvEmpty)
            return
        }

        for (record in history) {
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.HORIZONTAL
            layout.setPadding(0, 16, 0, 16)

            val tvDate = TextView(this)
            tvDate.text = record.first
            tvDate.setTextColor(getColor(R.color.white))
            tvDate.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val tvAmount = TextView(this)
            tvAmount.text = format.format(record.second)
            tvAmount.setTextColor(getColor(R.color.accent_blue))
            tvAmount.gravity = Gravity.END

            layout.addView(tvDate)
            layout.addView(tvAmount)
            container.addView(layout)

            // Línea divisoria
            val divider = View(this)
            divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            divider.setBackgroundColor(getColor(R.color.divider_color))
            container.addView(divider)
        }
    }

    private fun displayTableUsageStatistics() {
        val container = findViewById<LinearLayout>(R.id.ll_usage_container)
        container.removeAllViews()

        val tables = tableManager.getTables()
        for (table in tables) {
            val usageCount = tableManager.getTableUsage(table.name)
            
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.HORIZONTAL
            layout.setPadding(0, 16, 0, 16)

            val tvName = TextView(this)
            tvName.text = table.name
            tvName.setTextColor(getColor(R.color.white))
            tvName.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val tvCount = TextView(this)
            tvCount.text = "$usageCount veces"
            tvCount.setTextColor(getColor(R.color.accent_green))
            tvCount.gravity = Gravity.END

            layout.addView(tvName)
            layout.addView(tvCount)
            container.addView(layout)
        }
    }
}