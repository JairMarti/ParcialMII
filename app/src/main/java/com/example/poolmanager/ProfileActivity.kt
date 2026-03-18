package com.example.poolmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var tableManager: TableManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tableManager = TableManager(this)

        val etName = findViewById<EditText>(R.id.et_profile_name)
        val etEmail = findViewById<EditText>(R.id.et_profile_email)
        val btnSave = findViewById<Button>(R.id.btn_save_profile)
        val btnLogout = findViewById<Button>(R.id.btn_logout)
        val ivBack = findViewById<ImageView>(R.id.iv_back)

        // Cargar datos guardados
        etName.setText(tableManager.getProfileName())
        etEmail.setText(tableManager.getProfileEmail())

        ivBack?.setOnClickListener {
            finish()
        }

        btnSave?.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                tableManager.saveProfile(name, email)
                Toast.makeText(this, "Perfil guardado con éxito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}