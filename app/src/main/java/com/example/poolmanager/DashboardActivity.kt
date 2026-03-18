package com.example.poolmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    private lateinit var tableManager: TableManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tableManager = TableManager(this)

        setupBottomNavigation()
        setupQuickActions()
    }

    override fun onResume() {
        super.onResume()
        updateSummary()
        updateActiveTables()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = R.id.nav_home
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_tables -> {
                    startActivity(Intent(this, TableStatusActivity::class.java))
                    true
                }
                R.id.nav_sales, R.id.nav_stats -> {
                    startActivity(Intent(this, StatsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun updateSummary() {
        val tables = tableManager.getTables()
        val available = tables.count { it.status == "Disponible" }
        val occupied = tables.count { it.status == "Ocupada" }

        findViewById<TextView>(R.id.tv_count_available)?.text = available.toString()
        findViewById<TextView>(R.id.tv_count_occupied)?.text = occupied.toString()
    }

    private fun setupQuickActions() {
        findViewById<LinearLayout>(R.id.action_new_table)?.setOnClickListener {
            startActivity(Intent(this, TableStatusActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.action_history)?.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.action_reports)?.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.action_settings)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<TextView>(R.id.text_view_all)?.setOnClickListener {
            startActivity(Intent(this, TableStatusActivity::class.java))
        }
    }

    private fun updateActiveTables() {
        val tables = tableManager.getTables()
        val activeTables = tables.filter { it.status == "Ocupada" }
        
        // Let's hide the static cards and show dynamic ones if needed, 
        // but for now let's just make the existing ones work with real data
        val card1 = findViewById<CardView>(R.id.card_active_table_1)
        val card2 = findViewById<CardView>(R.id.card_active_table_2)

        if (activeTables.isNotEmpty()) {
            card1?.visibility = View.VISIBLE
            card1?.setOnClickListener {
                val intent = Intent(this, TableManagementActivity::class.java)
                intent.putExtra("TABLE_ID", activeTables[0].id)
                intent.putExtra("TABLE_NAME", activeTables[0].name)
                startActivity(intent)
            }
        } else {
            card1?.visibility = View.GONE
        }

        if (activeTables.size > 1) {
            card2?.visibility = View.VISIBLE
            card2?.setOnClickListener {
                val intent = Intent(this, TableManagementActivity::class.java)
                intent.putExtra("TABLE_ID", activeTables[1].id)
                intent.putExtra("TABLE_NAME", activeTables[1].name)
                startActivity(intent)
            }
        } else {
            card2?.visibility = View.GONE
        }
    }
}
