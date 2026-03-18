package com.example.poolmanager

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TableManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pool_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTables(tables: List<Table>) {
        val json = gson.toJson(tables)
        prefs.edit().putString("tables_list", json).apply()
    }

    fun getTables(): MutableList<Table> {
        val json = prefs.getString("tables_list", null) ?: return mutableListOf(
            Table(1, "Mesa 1", "Disponible"),
            Table(2, "Mesa 2", "Disponible")
        )
        val type = object : TypeToken<MutableList<Table>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveProfile(name: String, email: String) {
        prefs.edit().putString("user_name", name).putString("user_email", email).apply()
    }

    fun getProfileName(): String = prefs.getString("user_name", "Admin Pool") ?: "Admin Pool"
    fun getProfileEmail(): String = prefs.getString("user_email", "admin@poolclub.com") ?: "admin@poolclub.com"

    // --- Reportes (Facturación Diaria e Historial) ---
    fun addDailyBilling(amount: Double) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentBilling = getDailyBilling(today)
        prefs.edit().putFloat("billing_$today", (currentBilling + amount).toFloat()).apply()
        
        // Guardar en la lista de fechas registradas para el historial
        val datesJson = prefs.getString("billing_dates", "[]")
        val type = object : TypeToken<MutableList<String>>() {}.type
        val dates: MutableList<String> = gson.fromJson(datesJson, type)
        if (!dates.contains(today)) {
            dates.add(today)
            prefs.edit().putString("billing_dates", gson.toJson(dates)).apply()
        }
    }

    fun getDailyBilling(date: String): Double {
        return prefs.getFloat("billing_$date", 0.0f).toDouble()
    }

    fun getAllBillingHistory(): List<Pair<String, Double>> {
        val datesJson = prefs.getString("billing_dates", "[]")
        val type = object : TypeToken<List<String>>() {}.type
        val dates: List<String> = gson.fromJson(datesJson, type)
        return dates.map { it to getDailyBilling(it) }.sortedByDescending { it.first }
    }

    // --- Estadísticas (Uso de Mesas) ---
    fun incrementTableUsage(tableName: String) {
        val currentUsage = getTableUsage(tableName)
        prefs.edit().putInt("usage_$tableName", currentUsage + 1).apply()
    }

    fun getTableUsage(tableName: String): Int {
        return prefs.getInt("usage_$tableName", 0)
    }
}
