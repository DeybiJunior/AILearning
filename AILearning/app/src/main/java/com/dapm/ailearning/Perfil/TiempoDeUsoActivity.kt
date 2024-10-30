package com.dapm.ailearning.Perfil

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dapm.ailearning.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TiempoDeUsoActivity : AppCompatActivity() {

    private lateinit var textViewTiempoUso: TextView
    private lateinit var circularProgressIndicator: CircularProgressIndicator
    private val handler = Handler(Looper.getMainLooper())
    private var tiempoUso: Long = 0 // Tiempo en milisegundos
    private var isRunning: Boolean = false
    private lateinit var listView: ListView

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                // Incrementa el tiempo de uso cada segundo
                tiempoUso += 1000 // Sumar 1 segundo en milisegundos
                actualizarTextoTiempoUso()
                actualizarProgresoCircular()

                // Postea el runnable para el siguiente segundo
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tiempo_de_uso)

        textViewTiempoUso = findViewById(R.id.textViewTiempoUso)
        circularProgressIndicator = findViewById(R.id.circularProgressIndicator)
        listView = findViewById(R.id.list_usage)

        if (hasUsageStatsPermission()) {
            iniciarTiempoUsoDesdeEstadisticas()
            val usageSummary = getWeeklyUsageStats()
            mostrarUsoSemanal(usageSummary)

        } else {
            Toast.makeText(
                this,
                "Por favor, habilita el acceso a las estadísticas de uso para esta app.",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            appOps.checkOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun mostrarUsoSemanal(usageSummary: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, usageSummary)
        listView.adapter = adapter
    }

    private fun getWeeklyUsageStats(): List<String> {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7) // Últimos 7 días

        // Realiza la consulta directamente sin usar SharedPreferences
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            calendar.timeInMillis,
            System.currentTimeMillis()
        )

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val usageSummary = mutableListOf<Pair<String, Long>>()

        // Obtener la fecha actual
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        Log.d("UsageStats", "Cantidad de datos de uso obtenidos: ${usageStatsList.size}")

        for (usageStats in usageStatsList) {
            if (usageStats.packageName == packageName) {
                val lastUsed = Date(usageStats.lastTimeUsed)
                // Comprobar si la fecha de uso es anterior a hoy
                if (lastUsed.before(today.time) && !lastUsed.after(today.time)) {
                    val formattedDate = dateFormat.format(lastUsed)
                    val totalTime = usageStats.totalTimeInForeground
                    if (totalTime > 0) {
                        usageSummary.add(Pair(formattedDate, totalTime))

                        // Log para ver el tiempo total usado
                        Log.d("UsageStats", "Paquete: ${usageStats.packageName}, Fecha: $formattedDate, Tiempo total: $totalTime ms")
                    }
                }
            }
        }

        usageSummary.sortBy { it.second }

        // Crear la representación en cadena
        val formattedSummary = usageSummary.map {
            val hours = it.second / 3600000
            val minutes = (it.second % 3600000) / 60000
            val seconds = (it.second % 60000) / 1000  // Cálculo correcto de los segundos
            "Día: ${it.first} - Tiempo usado: ${hours}h ${minutes}m ${seconds}s"
        }

        Log.d("UsageStats", "Resumen de uso: $formattedSummary")

        if (formattedSummary.isEmpty()) {
            // Manejar caso de lista vacía si es necesario
            Log.d("UsageStats", "No se encontraron datos de uso para la aplicación.")
        }

        return formattedSummary
    }


    private fun iniciarTiempoUsoDesdeEstadisticas() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Obtener datos del día anterior
        val endTime = System.currentTimeMillis()
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            calendar.timeInMillis,
            endTime
        )
        Log.d("UsageStats", "Número de estadísticas de uso recibidas: ${usageStatsList.size}")

        var tiempoTotalUso = 0L
        for (usageStats in usageStatsList) {
            if (usageStats.packageName == packageName) {
                tiempoTotalUso += usageStats.totalTimeInForeground
            }
        }

        tiempoUso = tiempoTotalUso
        actualizarTextoTiempoUso()
        actualizarProgresoCircular()

        // Iniciar el cronómetro
        isRunning = true
        handler.post(runnable) // Comienza el cronómetro una vez
    }

    private fun actualizarTextoTiempoUso() {
        val horas = (tiempoUso / 3600000) % 24
        val minutos = (tiempoUso / 60000) % 60
        val segundos = (tiempoUso / 1000) % 60

        val tiempoFormateado = String.format("%02d:%02d:%02d", horas, minutos, segundos)
        textViewTiempoUso.text = tiempoFormateado
    }

    private fun actualizarProgresoCircular() {
        val segundosActuales = (tiempoUso / 1000) % 60 // Obtener solo los segundos actuales
        val progreso = (segundosActuales.toFloat() / 60) * 100 // Calcula el porcentaje en base a 60 segundos

        circularProgressIndicator.progress = progreso.toInt()

        if (segundosActuales >= 59) {
            circularProgressIndicator.progress = 0 // Reiniciar el indicador al minuto
        }
    }

    override fun onPause() {
        super.onPause()
        isRunning = false // Detener el cronómetro en pausa
        handler.removeCallbacks(runnable) // Evitar múltiples llamadas
    }

    override fun onResume() {
        super.onResume()
        if (hasUsageStatsPermission() && !isRunning) { // Verifica antes de reiniciar
            isRunning = true
            handler.post(runnable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false // Detener el cronómetro al destruir la actividad
        handler.removeCallbacks(runnable)
    }
}
