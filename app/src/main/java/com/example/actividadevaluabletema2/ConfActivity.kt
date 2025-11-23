package com.example.actividadevaluabletema2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.actividadevaluabletema2.databinding.ActivityConfBinding

class ConfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Acceder a SharedPreferences
        val sharedFich = getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE)
        val phoneKey = getString(R.string.string_phone)
        val ubiKey = getString(R.string.string_ubi)

        // Comprobar si ya hay un número guardado y redirigir a MainActivity si es así
        val savedPhone = sharedFich.getString(phoneKey, null)
        savedPhone?.let {
            startMainActivity(it)
        }

        /** Botón para confirmar y guardar la configuración */
        binding.btnConf.setOnClickListener {

            // Obtener los valores ingresados por el usuario
            val numberPhone = binding.editPhone.text.toString()
            val ubicacion = binding.editUbi.text.toString()

            // Validar los datos ingresados
            if (numberPhone.isEmpty()) {  // Validar número de teléfono vacío
                Toast.makeText(this, "Número vacío", Toast.LENGTH_LONG).show()

            } else if (!isValidPhoneNumber(numberPhone)) { // Validar formato del número de teléfono
                Toast.makeText(this, "Número no válido", Toast.LENGTH_LONG).show()

            }  else if (ubicacion.isEmpty()) { // Validar ubicación vacía
                Toast.makeText(this, "Ubicación vacía", Toast.LENGTH_LONG).show()

            } else { // Guardar los datos en SharedPreferences si todo es válido
                val edit = sharedFich.edit()
                edit.putString(phoneKey, numberPhone)
                edit.putString(ubiKey, ubicacion)
                edit.apply()
                startMainActivity(numberPhone)
            }
        }
    }

    /** Inicia la MainActivity pasando el número de teléfono como extra */
    private fun startMainActivity(phone: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(getString(R.string.string_phone), phone)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    /** Valida el formato del número de teléfono */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.length in 9..15 && phoneNumber.all { it.isDigit() || it == '+' } // Permitir dígitos y el símbolo '+'
    }

    override fun onResume() {
        super.onResume()
        val ret = intent.getBooleanExtra("back", false)
        if (ret) {
            binding.editPhone.setText("")
            Toast.makeText(this, "Introduce un nuevo número", Toast.LENGTH_LONG).show()
            intent.removeExtra("back")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
