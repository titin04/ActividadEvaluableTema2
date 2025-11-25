package com.example.actividadevaluabletema2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.actividadevaluabletema2.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener SharedPreferences y claves
        val prefs = getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE)
        val phoneKey = getString(R.string.string_phone)

        // Establecer visibilidad de botones según las preferencias
        val mostrarCalculadora = prefs.getBoolean("mostrar_calculadora", true)
        val mostrarInformacion = prefs.getBoolean("mostrar_informacion", true)
        binding.btnCalculadora.visibility = if (mostrarCalculadora) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnInfo.visibility = if (mostrarInformacion) android.view.View.VISIBLE else android.view.View.GONE

        /** Botón para ir al activity phone */
        binding.btnLlamada.setOnClickListener {
            val updatedPhone = prefs.getString(phoneKey, "") ?: ""

            if (isPermissionCall()) { // Verificar si el permiso de llamada está concedido
                val intent = Intent(this, Phone::class.java)
                intent.putExtra(phoneKey, updatedPhone) // Pasar el número de teléfono actualizado
                startActivity(intent)
            } else { // Solicitar permiso de llamada si no está concedido
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 100)
            }
        }

        /** Botón para abrir URL */
        binding.btnUrl.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/titin04/"))
            startActivity(intent)
        }

        /** Botón para configurar alarma con hora actual + 2 minutos */
        binding.btnAlarm.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 2) // Sumar 2 minutos
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
                putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, "Alarma en 2 minutos")
                putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
            }
            startActivity(intent)
        }

        /** Botón para mostrar ubicación elegida en el mapa */
        binding.btnUbicacion.setOnClickListener {
            // Obtener la ubicación guardada en SharedPreferences
            val ubiKey = getString(R.string.string_ubi)
            val ubicacion = prefs.getString(ubiKey, null)

            if (!ubicacion.isNullOrEmpty()) { // Verificar si la ubicación está configurada
                val intent = Intent(Intent.ACTION_VIEW).apply { // Crear intención para ver ubicación
                    data = Uri.parse("geo:0,0?q=${Uri.encode(ubicacion)}") // Formatear la ubicación para la URI
                }
                startActivity(intent)
            } else { // Mostrar mensaje si la ubicación no está configurada
                Toast.makeText(this, "Ubicación no configurada", Toast.LENGTH_LONG).show()
            }
        }

        /** Botón para ir a la configuración */
        binding.btnLlamadaConf.setOnClickListener {
            val intent = Intent(this, ConfActivity::class.java)
            startActivity(intent)
        }


        /** Botón para ir al activity info */
        binding.btnInfo.setOnClickListener {
            val intent = Intent(this, Info::class.java)
            startActivity(intent)
        }

        /** Botón para ir a la actividad de chistes */
        binding.btnChistes.setOnClickListener {
            val modo = prefs.getString("modo", "ninguno")
            if (modo == "chistes") {
                startActivity(Intent(this, Chistes::class.java))
            } else {
                Toast.makeText(this, "Activa el modo Chistes en configuración", Toast.LENGTH_SHORT).show()
            }
        }

        /** Botón para ir a la actividad de dados */
        binding.btnDados.setOnClickListener {
            val modo = prefs.getString("modo", "ninguno")
            if (modo == "dados") {
                startActivity(Intent(this, Dados::class.java))
            } else {
                Toast.makeText(this, "Activa el modo Dados en configuración", Toast.LENGTH_SHORT).show()
            }
        }

        /** Botón para ir a la actividad de calculadora */
        binding.btnCalculadora.setOnClickListener {
            val intent = Intent(this, Calculadora::class.java)
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    /** Maneja el resultado de la solicitud de permisos */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Siempre llama al método de la superclase

        if (requestCode == 100) { // Verificar si el código de solicitud coincide
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permiso concedido
                Toast.makeText(this, "Permiso de llamada concedido", Toast.LENGTH_SHORT).show()
            } else { // Permiso denegado
                Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Verifica si el permiso de llamada está concedido */
    private fun isPermissionCall(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            true // Permiso concedido automáticamente en versiones anteriores a Marshmallow
        } else { // Verificar el permiso en tiempo de ejecución
            checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE)

        val mostrarCalculadora = prefs.getBoolean("mostrar_calculadora", true)
        val mostrarInformacion = prefs.getBoolean("mostrar_informacion", true)

        binding.btnCalculadora.visibility = if (mostrarCalculadora) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnInfo.visibility = if (mostrarInformacion) android.view.View.VISIBLE else android.view.View.GONE
    }

}
