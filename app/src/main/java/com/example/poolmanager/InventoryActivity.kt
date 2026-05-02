package com.example.poolmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.NumberFormat
import java.util.Locale

class InventoryActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var adapter: InventoryAdapter
    private lateinit var userRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        userRole = intent.getStringExtra("user_role") ?: "empleado"

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        setupRecyclerView()
        listenToInventory()

        val fab = findViewById<FloatingActionButton>(R.id.fab_add_product)
        if (userRole == "admin") {
            fab.visibility = View.VISIBLE
            fab.setOnClickListener { showAddProductDialog() }
        } else {
            fab.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rv_inventory)
        adapter = InventoryAdapter(emptyList()) { producto ->
            if (userRole == "admin") showEditDeleteDialog(producto)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun listenToInventory() {
        db.collection("inventario").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val productos = snapshot.toObjects(Producto::class.java)
                for (i in productos.indices) {
                    productos[i].id = snapshot.documents[i].id
                }
                adapter.updateList(productos)
            }
        }
    }

    private fun showAddProductDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nuevo Producto")

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val inputNombre = EditText(this).apply { hint = "Nombre" }
        val inputPrecio = EditText(this).apply { 
            hint = "Precio"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val inputStock = EditText(this).apply { 
            hint = "Stock Inicial"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val inputCat = EditText(this).apply { hint = "Categoría (Bebida, Snack, etc.)" }

        layout.addView(inputNombre)
        layout.addView(inputPrecio)
        layout.addView(inputStock)
        layout.addView(inputCat)
        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nombre = inputNombre.text.toString().trim()
            val precio = inputPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val stock = inputStock.text.toString().toIntOrNull() ?: 0
            val cat = inputCat.text.toString().trim()

            if (nombre.isNotEmpty()) {
                val prod = Producto("", nombre, stock, precio, cat)
                db.collection("inventario").add(prod)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun showEditDeleteDialog(producto: Producto) {
        val options = arrayOf("Editar Stock", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle(producto.nombre)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditStockDialog(producto)
                    1 -> db.collection("inventario").document(producto.id).delete()
                }
            }.show()
    }

    private fun showEditStockDialog(producto: Producto) {
        val input = EditText(this).apply {
            setText(producto.cantidad.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        AlertDialog.Builder(this)
            .setTitle("Actualizar Stock")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoStock = input.text.toString().toIntOrNull() ?: producto.cantidad
                db.collection("inventario").document(producto.id).update("cantidad", nuevoStock)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

class InventoryAdapter(private var list: List<Producto>, private val onClick: (Producto) -> Unit) :
    RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_product_name)
        val category: TextView = view.findViewById(R.id.tv_product_category)
        val stock: TextView = view.findViewById(R.id.tv_product_stock)
        val price: TextView = view.findViewById(R.id.tv_product_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prod = list[position]
        holder.name.text = prod.nombre
        holder.category.text = prod.categoria
        holder.stock.text = "Stock: ${prod.cantidad}"
        holder.price.text = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(prod.precio)
        holder.itemView.setOnClickListener { onClick(prod) }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Producto>) {
        list = newList
        notifyDataSetChanged()
    }
}
