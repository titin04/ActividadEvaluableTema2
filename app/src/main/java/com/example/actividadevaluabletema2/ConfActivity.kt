package com.example.actividadevaluabletema2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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

        //Precargar valores guardados en los EditText
        val savedPhone = sharedFich.getString(phoneKey, "")
        val savedUbi = sharedFich.getString(ubiKey, "")
        binding.editPhone.setText(savedPhone)
        binding.editUbi.setText(savedUbi)

        //Precargar estado de los CheckBox
        val mostrarCalculadora = sharedFich.getBoolean("mostrar_calculadora", true)
        val mostrarInformacion = sharedFich.getBoolean("mostrar_informacion", true)
        binding.checkCalculadora.isChecked = mostrarCalculadora
        binding.checkInformacion.isChecked = mostrarInformacion

        /** Botón para confirmar y guardar la configuración */
        binding.btnConf.setOnClickListener {
            val numberPhone = binding.editPhone.text.toString()
            val ubicacion = binding.editUbi.text.toString()

            if (numberPhone.isEmpty()) {
                Toast.makeText(this, "Número vacío", Toast.LENGTH_LONG).show()
            } else if (!isValidPhoneNumber(numberPhone)) {
                Toast.makeText(this, "Número no válido", Toast.LENGTH_LONG).show()
            } else if (ubicacion.isEmpty()) {
                Toast.makeText(this, "Ubicación vacía", Toast.LENGTH_LONG).show()
            } else {
                val edit = sharedFich.edit()
                edit.putString(phoneKey, numberPhone)
                edit.putString(ubiKey, ubicacion)

                // Guardar el modo seleccionado
                val selectedId = binding.radioGroupModo.checkedRadioButtonId
                when (selectedId) {
                    R.id.radio_dados -> edit.putString("modo", "dados")
                    R.id.radio_chistes -> edit.putString("modo", "chistes")
                    else -> edit.putString("modo", "ninguno")
                }

                edit.apply()
                startMainActivity(numberPhone)
            }
        }


        // Guardar cambios de los CheckBox al instante
        binding.checkCalculadora.setOnCheckedChangeListener { _, isChecked ->
            sharedFich.edit().putBoolean("mostrar_calculadora", isChecked).apply()
        }

        binding.checkInformacion.setOnCheckedChangeListener { _, isChecked ->
            sharedFich.edit().putBoolean("mostrar_informacion", isChecked).apply()
        }
    }

    private fun startMainActivity(phone: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(getString(R.string.string_phone), phone)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.length in 9..15 && phoneNumber.all { it.isDigit() || it == '+' }
    }

    override fun onResume() {
        super.onResume()

        // Ocultar barra de estado y navegación con WindowInsetsController
        window.insetsController?.let { controller ->
            controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
