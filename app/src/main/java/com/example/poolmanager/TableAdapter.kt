package com.example.poolmanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TableAdapter(
    private var mesas: List<Mesa>,
    private val role: String,
    private val onActionClick: (Mesa) -> Unit,
    private val onLongClick: (Mesa) -> Unit
) : RecyclerView.Adapter<TableAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_item_table_name)
        val tvStatus: TextView = view.findViewById(R.id.tv_item_table_status)
        val btnAction: Button = view.findViewById(R.id.btn_item_table_action)
        val ivTable: ImageView = view.findViewById(R.id.iv_item_table_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_table_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mesa = mesas[position]
        holder.tvName.text = mesa.nombre
        holder.tvStatus.text = mesa.estado.replaceFirstChar { it.uppercase() }

        when (mesa.estado) {
            "disponible" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                holder.btnAction.text = "Abrir Mesa"
                holder.btnAction.setBackgroundColor(Color.parseColor("#4CAF50"))
            }
            "ocupada" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
                holder.btnAction.text = "Gestionar"
                holder.btnAction.setBackgroundColor(Color.parseColor("#F44336"))
            }
            "mantenimiento" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"))
                holder.btnAction.text = "Mantenimiento"
                holder.btnAction.setBackgroundColor(Color.parseColor("#FF9800"))
            }
        }

        holder.btnAction.setOnClickListener { onActionClick(mesa) }
        
        // El administrador puede editar/eliminar con clic largo
        if (role == "admin") {
            holder.itemView.setOnLongClickListener {
                onLongClick(mesa)
                true
            }
        }
    }

    override fun getItemCount() = mesas.size

    fun updateList(newList: List<Mesa>) {
        mesas = newList
        notifyDataSetChanged()
    }
}
