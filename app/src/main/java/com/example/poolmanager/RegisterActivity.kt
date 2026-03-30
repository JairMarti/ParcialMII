package com.example.poolmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // 🔥 AGREGA ESTE CAMPO EN TU XML
        val etNombre = findViewById<EditText>(R.id.et_nombre)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvLogin = findViewById<TextView>(R.id.tv_login_link)

        btnRegister.setOnClickListener {

            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔐 1. Crear usuario en Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->

                    val uid = result.user?.uid

                    // 💾 2. Guardar datos en Firestore
                    val usuario = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "uid" to uid
                    )

                    db.collection("usuarios")
                        .document(uid!!) // clave única por usuario
                        .set(usuario)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Usuario guardado correctamente", Toast.LENGTH_SHORT).show()

                            startActivity(Intent(this, DashboardActivity::class.java))
                            finishAffinity()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_LONG).show()
                        }

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }
}
