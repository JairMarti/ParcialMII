package com.example.poolmanager

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val ivShowPassword = findViewById<ImageView>(R.id.iv_show_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnGoogle = findViewById<Button>(R.id.btn_google)
        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)
        val tvRegister = findViewById<TextView>(R.id.tv_register)

        // Mostrar/Ocultar contraseña
        ivShowPassword?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword?.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                etPassword?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.setImageResource(android.R.drawable.ic_menu_view)
            }
            etPassword?.setSelection(etPassword.text.length)
        }

        // Botón Login con credenciales admin/admin
        btnLogin?.setOnClickListener {
            val user = etUsername?.text.toString()
            val pass = etPassword?.text.toString()

            if (user == "admin" && pass == "admin") {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Google
        btnGoogle?.setOnClickListener {
            Toast.makeText(this, "Login con Google no disponible", Toast.LENGTH_SHORT).show()
        }

        // Olvidé contraseña
        tvForgotPassword?.setOnClickListener {
            Toast.makeText(this, "Recuperar contraseña próximamente", Toast.LENGTH_SHORT).show()
        }

        // Ir a Registro
        tvRegister?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}