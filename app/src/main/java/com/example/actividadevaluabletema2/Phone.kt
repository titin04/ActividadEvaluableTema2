package com.example.actividadevaluabletema2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.actividadevaluabletema2.databinding.ActivityPhoneBinding

class Phone : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneBinding
    private var phoneSOS: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón para realizar la llamada
        binding.btnPhone.setOnClickListener {
            if (!phoneSOS.isNullOrEmpty()) { // Verificar que el número de teléfono no sea nulo o vacío
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$phoneSOS")
                }
                startActivity(intent)
            } else { // Mostrar un mensaje si el número de teléfono no está disponible
                Toast.makeText(this, "Número de teléfono no disponible", Toast.LENGTH_LONG).show()
            }
        }

        // Botón para ir al MainActivity
        binding.btnGotoMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val phoneKey = getString(R.string.string_phone)
        phoneSOS = intent.getStringExtra(phoneKey)
        binding.textPhone.setText(phoneSOS)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
