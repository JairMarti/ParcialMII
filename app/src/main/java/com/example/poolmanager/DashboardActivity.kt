package com.example.poolmanager

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class DashboardActivity : AppCompatActivity() {
    private lateinit var userRole: String
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userRole = intent.getStringExtra("user_role") ?: "empleado"
        
        setupUI()
        setupBottomNavigation()
        setupQuickActions()
        listenToTableStats()
    }

    private fun setupUI() {
        val tvWelcome = findViewById<TextView>(R.id.tv_welcome_name)
        if (userRole == "admin") {
            tvWelcome.text = "¡Bienvenido, Admin!"
        } else {
            tvWelcome.text = "¡Bienvenido, Empleado!"
        }
    }

    private fun listenToTableStats() {
        db.collection("mesas").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            
            if (snapshot != null) {
                val tables = snapshot.toObjects(Mesa::class.java)
                findViewById<TextView>(R.id.tv_total_mesas).text = tables.size.toString()
                findViewById<TextView>(R.id.tv_count_available).text = tables.count { it.estado == "disponible" }.toString()
                findViewById<TextView>(R.id.tv_count_occupied).text = tables.count { it.estado == "ocupada" }.toString()
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_tables -> {
                    startActivity(Intent(this, TableStatusActivity::class.java).putExtra("user_role", userRole))
                    true
                }
                R.id.nav_events -> {
                    startActivity(Intent(this, CalendarActivity::class.java).putExtra("user_role", userRole))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).putExtra("user_role", userRole))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupQuickActions() {
        findViewById<LinearLayout>(R.id.action_new_table).setOnClickListener {
            startActivity(Intent(this, TableStatusActivity::class.java).putExtra("user_role", userRole))
        }
        
        findViewById<LinearLayout>(R.id.action_events).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java).putExtra("user_role", userRole))
        }

        findViewById<LinearLayout>(R.id.action_reports).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java).putExtra("user_role", userRole))
        }

        findViewById<LinearLayout>(R.id.action_settings).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).putExtra("user_role", userRole))
        }
    }
}
