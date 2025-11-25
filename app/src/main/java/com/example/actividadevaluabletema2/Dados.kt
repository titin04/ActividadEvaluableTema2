package com.example.actividadevaluabletema2

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import com.example.actividadevaluabletema2.databinding.ActivityDadosBinding
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class Dados : AppCompatActivity() {

    private lateinit var binding: ActivityDadosBinding
    private var sum: Int = 0
    private lateinit var handler: Handler
    private lateinit var prefs: SharedPreferences
    private var tiempoTirada: Int = 1000 // valor por defecto en milisegundos

    private val mensajes = arrayOf(
        "3 → ¡Haz una voltereta!",
        "4 → Cuenta un chiste malo",
        "5 → Baila 10 segundos",
        "6 → Envía un emoji a un amigo",
        "7 → Haz 5 flexiones",
        "8 → Canta una canción corta",
        "9 → Imita a un animal",
        "10 → Haz una pose divertida",
        "11 → Di tu comida favorita",
        "12 → Haz 3 sentadillas",
        "13 → Aplaude fuerte 5 veces",
        "14 → Di un trabalenguas",
        "15 → Haz una cara graciosa",
        "16 → Da una vuelta sobre ti mismo",
        "17 → Di tu número de la suerte",
        "18 → ¡Premio! Puedes descansar 🎉"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())
        prefs = getSharedPreferences("config", MODE_PRIVATE)
        tiempoTirada = obtenerTiempoDesdePrefs()

        initEvent()

        binding.btnVolverMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initEvent() {
        binding.txtResultado.visibility = View.INVISIBLE
        binding.imageButton.setOnClickListener {
            binding.txtResultado.visibility = View.VISIBLE
            game()
        }
    }

    private fun game() {
        sheduleRun()
    }

    private fun sheduleRun() {
        val schedulerExecutor = Executors.newSingleThreadScheduledExecutor()

        for (i in 1..5) {
            schedulerExecutor.schedule(
                { handler.post { throwDadoInTime() } },
                tiempoTirada * i.toLong(), TimeUnit.MILLISECONDS
            )
        }

        schedulerExecutor.schedule(
            { handler.post { viewResult() } },
            tiempoTirada * 7.toLong(), TimeUnit.MILLISECONDS
        )

        schedulerExecutor.shutdown()
    }

    private fun throwDadoInTime() {
        val numDados = Array(3) { Random.nextInt(1, 7) }
        val imagViews: Array<ImageView> = arrayOf(
            binding.imagviewDado1,
            binding.imagviewDado2,
            binding.imagviewDado3
        )

        sum = numDados.sum()
        for (i in 0..2) {
            selectView(imagViews[i], numDados[i])
        }
    }

    private fun selectView(imgV: ImageView, v: Int) {
        val resId = when (v) {
            1 -> R.drawable.dado1
            2 -> R.drawable.dado2
            3 -> R.drawable.dado3
            4 -> R.drawable.dado4
            5 -> R.drawable.dado5
            6 -> R.drawable.dado6
            else -> R.drawable.dado1
        }
        imgV.setImageResource(resId)
    }

    private fun viewResult() {
        val index = sum - 3 // porque el array empieza en 0 y la suma mínima es 3
        val mensaje = mensajes[index]
        binding.txtResultado.text = mensaje
    }

    private fun obtenerTiempoDesdePrefs(): Int {
        val texto = prefs.getString("tiempo_tirada", "1 segundo") ?: "1 segundo"
        return when (texto) {
            "1 segundo" -> 1000
            "2 segundos" -> 2000
            "3 segundos" -> 3000
            else -> 1000
        }
    }
}
