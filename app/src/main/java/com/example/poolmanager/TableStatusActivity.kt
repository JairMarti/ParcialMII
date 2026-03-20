package com.example.poolmanager

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TableStatusActivity : AppCompatActivity() {
    private lateinit var tablesList: MutableList<Table>
    private lateinit var tableManager: TableManager
    private lateinit var container: GridLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvAvailable: TextView
    private var currentFilter: String = "Todas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_status)

        tableManager = TableManager(this)
        tablesList = tableManager.getTables()
        
        container = findViewById(R.id.gl_tables_container)
        tvTotal = findViewById(R.id.tv_total_tables_count)
        tvAvailable = findViewById(R.id.tv_available_tables_count)

        setupNavigation()
        setupFilters()
        refreshTablesUI()

        findViewById<FloatingActionButton>(R.id.fab_add_table)?.setOnClickListener {
            showAddTableDialog()
        }
    }

    private fun setupFilters() {
        val chipGroup = findViewById<ChipGroup>(R.id.chip_group_filters)
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = when (checkedId) {
                R.id.chip_filter_available -> "Disponible"
                R.id.chip_filter_occupied -> "Ocupada"
                R.id.chip_filter_reserved -> "Reservada"
                else -> "Todas"
            }
            refreshTablesUI()
        }
    }

    private fun refreshTablesUI() {
        container.removeAllViews()
        var availableCount = 0
        
        val filteredList = if (currentFilter == "Todas") {
            tablesList
        } else {
            tablesList.filter { it.status == currentFilter }
        }

        availableCount = tablesList.count { it.status == "Disponible" }
        
        for (table in filteredList) {
            val card = layoutInflater.inflate(R.layout.item_table_card, container, false)
            
            // Set layout params for GridLayout
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            card.layoutParams = params

            val tvName = card.findViewById<TextView>(R.id.tv_item_table_name)
            val tvStatus = card.findViewById<TextView>(R.id.tv_item_table_status)
            val btnAction = card.findViewById<Button>(R.id.btn_item_table_action)
            val ivTable = card.findViewById<ImageView>(R.id.iv_item_table_image)
            
            tvName.text = table.name
            tvStatus.text = table.status
            
            // Set the pool table image
            // Note: Make sure the image is named img_pool_table in drawable folder
            val resId = resources.getIdentifier("img_pool_table", "drawable", packageName)
            if (resId != 0) {
                ivTable.setImageResource(resId)
            }

            when (table.status) {
                "Ocupada" -> {
                    tvStatus.setTextColor(Color.parseColor("#FF5252")) // Red/Orange for occupied
                    tvStatus.setBackgroundResource(R.drawable.bg_pill_white_alpha)
                    tvStatus.text = "Ocupada"
                    btnAction.text = "En Uso"
                    btnAction.setBackgroundColor(Color.parseColor("#333333"))
                    btnAction.setTextColor(Color.WHITE)
                    btnAction.setOnClickListener { openManagement(table) }
                }
                "Reservada" -> {
                    tvStatus.setTextColor(Color.parseColor("#2196F3"))
                    tvStatus.text = "Reservada"
                    btnAction.text = "Activar"
                    btnAction.setBackgroundColor(Color.parseColor("#2196F3"))
                    btnAction.setOnClickListener { activateReservedTable(table) }
                }
                else -> { // Disponible
                    tvStatus.setTextColor(Color.parseColor("#00C853"))
                    tvStatus.text = "Disponible"
                    btnAction.text = "Reservar Mesa"
                    btnAction.setBackgroundColor(Color.parseColor("#00C853"))
                    btnAction.setOnClickListener { showOpenOrReserveDialog(table) }
                }
            }

            card.setOnClickListener {
                if (table.status == "Ocupada") {
                    openManagement(table)
                } else if (table.status == "Reservada") {
                    activateReservedTable(table)
                } else {
                    showOpenOrReserveDialog(table)
                }
            }

            card.setOnLongClickListener {
                showEditDeleteDialog(table)
                true
            }

            container.addView(card)
        }
        
        tvTotal.text = tablesList.size.toString()
        tvAvailable.text = availableCount.toString()
    }

    private fun showOpenOrReserveDialog(table: Table) {
        val options = arrayOf("Abrir Mesa (Iniciar Juego)", "Reservar Mesa")
        AlertDialog.Builder(this)
            .setTitle("Opciones para ${table.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openManagement(table)
                    1 -> reserveTable(table)
                }
            }
            .show()
    }

    private fun reserveTable(table: Table) {
        table.status = "Reservada"
        tableManager.saveTables(tablesList)
        refreshTablesUI()
        Toast.makeText(this, "${table.name} ha sido reservada", Toast.LENGTH_SHORT).show()
    }

    private fun activateReservedTable(table: Table) {
        AlertDialog.Builder(this)
            .setTitle("Activar Mesa")
            .setMessage("¿Deseas iniciar el juego en la ${table.name}?")
            .setPositiveButton("Sí, Iniciar") { _, _ ->
                openManagement(table)
            }
            .setNegativeButton("Cancelar Reserva") { _, _ ->
                table.status = "Disponible"
                tableManager.saveTables(tablesList)
                refreshTablesUI()
            }
            .show()
    }

    private fun openManagement(table: Table) {
        val intent = Intent(this, TableManagementActivity::class.java)
        intent.putExtra("TABLE_ID", table.id)
        startActivity(intent)
    }

    private fun showAddTableDialog() {
        val input = EditText(this)
        input.hint = "Nombre de la mesa (Ej: Mesa 5)"
        AlertDialog.Builder(this)
            .setTitle("Nueva Mesa")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val name = input.text.toString()
                if (name.isNotEmpty()) {
                    val newId = if (tablesList.isEmpty()) 1 else tablesList.maxOf { it.id } + 1
                    tablesList.add(Table(newId, name, "Disponible"))
                    tableManager.saveTables(tablesList)
                    refreshTablesUI()
                }
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun showEditDeleteDialog(table: Table) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Gestionar ${table.name}")
        val options = arrayOf("Editar Nombre", "Eliminar Mesa")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val input = EditText(this)
                    input.setText(table.name)
                    AlertDialog.Builder(this).setTitle("Editar Nombre").setView(input)
                        .setPositiveButton("Guardar") { _, _ ->
                            table.name = input.text.toString()
                            tableManager.saveTables(tablesList)
                            refreshTablesUI()
                        }.show()
                }
                1 -> {
                    tablesList.remove(table)
                    tableManager.saveTables(tablesList)
                    refreshTablesUI()
                    Toast.makeText(this, "Mesa eliminada", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.show()
    }

    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = R.id.nav_tables
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tables -> true
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

    override fun onResume() {
        super.onResume()
        tablesList = tableManager.getTables()
        refreshTablesUI()
    }
}