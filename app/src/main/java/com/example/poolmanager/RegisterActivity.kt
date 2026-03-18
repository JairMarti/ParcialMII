package com.example.poolmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        findViewById<Button>(R.id.btn_register)?.setOnClickListener {
            // Aquí iría la lógica de registro
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            finishAffinity() // Cierra todas las actividades anteriores
        }

        findViewById<TextView>(R.id.tv_login_link)?.setOnClickListener {
            finish() // Regresa al login
        }
    }
}