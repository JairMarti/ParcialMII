package com.example.poolmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
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
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var userRole: String
    private lateinit var adapter: EventAdapter
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        userRole = intent.getStringExtra("user_role") ?: "empleado"

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
            listenToEvents(selectedDate)
        }

        setupRecyclerView()
        listenToEvents(selectedDate)

        val fab = findViewById<FloatingActionButton>(R.id.fab_add_event)
        if (userRole == "admin") {
            fab.visibility = View.VISIBLE
            fab.setOnClickListener { showAddEventDialog() }
        } else {
            fab.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rv_events)
        adapter = EventAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun listenToEvents(date: Long) {
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val endOfDay = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        db.collection("eventos")
            .whereGreaterThanOrEqualTo("fecha", startOfDay)
            .whereLessThanOrEqualTo("fecha", endOfDay)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val events = snapshot.toObjects(Evento::class.java)
                    adapter.updateList(events)
                }
            }
    }

    private fun showAddEventDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nuevo Evento")
        
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputTitle = EditText(this)
        inputTitle.hint = "Título del evento"
        layout.addView(inputTitle)

        val inputDesc = EditText(this)
        inputDesc.hint = "Descripción"
        layout.addView(inputDesc)

        builder.setView(layout)

        builder.setPositiveButton("Crear") { _, _ ->
            val title = inputTitle.text.toString().trim()
            val desc = inputDesc.text.toString().trim()

            if (title.isNotEmpty()) {
                val event = hashMapOf(
                    "titulo" to title,
                    "descripcion" to desc,
                    "fecha" to selectedDate
                )
                db.collection("eventos").add(event)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    class EventAdapter(private var events: List<Evento>) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(android.R.id.text1)
            val desc: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val event = events[position]
            holder.title.text = event.titulo
            holder.title.setTextColor(android.graphics.Color.WHITE)
            holder.desc.text = event.descripcion
            holder.desc.setTextColor(android.graphics.Color.GRAY)
        }

        override fun getItemCount() = events.size

        fun updateList(newList: List<Evento>) {
            events = newList
            notifyDataSetChanged()
        }
    }
}
