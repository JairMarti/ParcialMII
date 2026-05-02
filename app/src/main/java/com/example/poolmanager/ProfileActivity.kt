package com.example.poolmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class ProfileActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage
    private lateinit var userRole: String
    
    private lateinit var ivProfilePic: ImageView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var tvRole: TextView
    private lateinit var tvDisplayName: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnChangePass: MaterialButton

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadImageToFirebase(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userRole = intent.getStringExtra("user_role") ?: "empleado"

        initViews()
        loadUserData()
        setupPermissions()
    }

    private fun initViews() {
        ivProfilePic = findViewById(R.id.iv_profile_pic)
        etName = findViewById(R.id.et_profile_name)
        etEmail = findViewById(R.id.et_profile_email)
        tvRole = findViewById(R.id.tv_display_role)
        tvDisplayName = findViewById(R.id.tv_display_name)
        btnSave = findViewById(R.id.btn_save_profile)
        btnChangePass = findViewById(R.id.btn_change_password)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }
        
        findViewById<View>(R.id.fab_edit_pic).setOnClickListener {
            pickImage.launch("image/*")
        }

        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupPermissions() {
        if (userRole == "admin") {
            etName.isEnabled = true
            btnSave.visibility = View.VISIBLE
            btnChangePass.visibility = View.VISIBLE
            btnChangePass.setOnClickListener { showChangePasswordDialog() }
            btnSave.setOnClickListener { updateProfile() }
        } else {
            etName.isEnabled = false
            btnSave.visibility = View.GONE
            btnChangePass.visibility = View.GONE
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val name = doc.getString("nombre") ?: ""
                val email = doc.getString("email") ?: ""
                val role = doc.getString("role") ?: "empleado"
                val photoUrl = doc.getString("photoUrl")

                etName.setText(name)
                tvDisplayName.text = name
                etEmail.setText(email)
                tvRole.text = role.replaceFirstChar { it.uppercase() }
                
                // Cargar imagen si existe (aquí se usaría Glide o Picasso)
                // Por ahora solo el placeholder
            }
        }
    }

    private fun updateProfile() {
        val uid = auth.currentUser?.uid ?: return
        val newName = etName.text.toString().trim()
        if (newName.isEmpty()) return

        db.collection("usuarios").document(uid).update("nombre", newName)
            .addOnSuccessListener {
                tvDisplayName.text = newName
                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profiles/$uid.jpg")

        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                db.collection("usuarios").document(uid).update("photoUrl", downloadUri.toString())
                Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show()
                // Actualizar UI...
            }
        }
    }

    private fun showChangePasswordDialog() {
        val email = auth.currentUser?.email ?: return
        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            Toast.makeText(this, "Se ha enviado un correo para restablecer tu contraseña", Toast.LENGTH_LONG).show()
        }
    }
}
