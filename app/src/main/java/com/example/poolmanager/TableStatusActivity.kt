package com.example.poolmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class TableStatusActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var adapter: TableAdapter
    private lateinit var userRole: String
    
    private lateinit var tvTotal: TextView
    private lateinit var tvAvailable: TextView
    private lateinit var tvOccupied: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_status)

        userRole = intent.getStringExtra("user_role") ?: "empleado"

        tvTotal = findViewById(R.id.tv_total_tables_count)
        tvAvailable = findViewById(R.id.tv_available_tables_count)
        tvOccupied = findViewById(R.id.tv_occupied_tables_count)

        setupRecyclerView()
        setupNavigation()
        listenToTables()

        val fab = findViewById<FloatingActionButton>(R.id.fab_add_table)
        if (userRole == "admin") {
            fab.visibility = View.VISIBLE
            fab.setOnClickListener { showAddTableDialog() }
        }
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rv_tables)
        adapter = TableAdapter(
            emptyList(),
            userRole,
            onActionClick = { mesa -> 
                val intent = Intent(this, TableManagementActivity::class.java)
                intent.putExtra("TABLE_ID", mesa.id)
                intent.putExtra("TABLE_NAME", mesa.nombre)
                startActivity(intent)
            },
            onLongClick = { mesa ->
                if (userRole == "admin") showEditDeleteDialog(mesa)
            }
        )
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.adapter = adapter
    }

    private fun listenToTables() {
        db.collection("mesas").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val tables = snapshot.toObjects(Mesa::class.java)
                // Asignar IDs de los documentos a los objetos
                for (i in tables.indices) {
                    tables[i].id = snapshot.documents[i].id
                }
                
                adapter.updateList(tables)
                updateCounters(tables)
            }
        }
    }

    private fun updateCounters(tables: List<Mesa>) {
        tvTotal.text = tables.size.toString()
        tvAvailable.text = tables.count { it.estado == "disponible" }.toString()
        tvOccupied.text = tables.count { it.estado == "ocupada" }.toString()
    }

    private fun showAddTableDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar Nueva Mesa")
        
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre de la mesa"
        layout.addView(inputNombre)

        val inputPrecio = EditText(this)
        inputPrecio.hint = "Precio por hora"
        inputPrecio.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputPrecio)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nombre = inputNombre.text.toString().trim()
            val precio = inputPrecio.text.toString().toDoubleOrNull() ?: 0.0

            if (nombre.isNotEmpty()) {
                val mesa = hashMapOf(
                    "nombre" to nombre,
                    "estado" to "disponible",
                    "precio" to precio,
                    "fecha_creacion" to com.google.firebase.Timestamp.now()
                )
                db.collection("mesas").add(mesa)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun showEditDeleteDialog(mesa: Mesa) {
        val options = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle("Gestionar ${mesa.nombre}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditDialog(mesa)
                    1 -> db.collection("mesas").document(mesa.id).delete()
                }
            }.show()
    }

    private fun showEditDialog(mesa: Mesa) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Mesa")
        val input = EditText(this)
        input.setText(mesa.nombre)
        builder.setView(input)
        builder.setPositiveButton("Actualizar") { _, _ ->
            val nuevoNombre = input.text.toString().trim()
            if (nuevoNombre.isNotEmpty()) {
                db.collection("mesas").document(mesa.id).update("nombre", nuevoNombre)
            }
        }
        builder.show()
    }

    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_tables
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.putExtra("user_role", userRole)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_tables -> true
                else -> false
            }
        }
    }
}
