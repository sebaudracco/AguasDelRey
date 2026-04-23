package com.sebaudracco.aguasdelrey.ui.route

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.model.RutaReparto
import com.sebaudracco.aguasdelrey.databinding.ActivitySelectRutaBinding
import com.sebaudracco.aguasdelrey.data.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectRutaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectRutaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectRutaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Seleccionar Ruta"

        cargarRutas()
    }

    private fun cargarRutas() {
        binding.progressBar.visibility   = View.VISIBLE
        binding.recyclerRutas.visibility = View.GONE

        MainScope().launch {
            try {
                val rutas = withContext(Dispatchers.IO) {
                    DataRepository.fetchRutas(applicationContext)
                }

                DataRepository.setCache(rutas)
                binding.progressBar.visibility = View.GONE

                if (rutas.isEmpty()) {
                    Toast.makeText(
                        this@SelectRutaActivity,
                        "No tenés rutas asignadas para hoy.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    binding.recyclerRutas.visibility = View.VISIBLE
                    binding.recyclerRutas.layoutManager = LinearLayoutManager(this@SelectRutaActivity)
                    binding.recyclerRutas.adapter = RutaAdapter(rutas) { ruta ->
                        val intent = Intent(this@SelectRutaActivity, RouteActivity::class.java)
                        intent.putExtra("RUTA_ID", ruta.id)
                        startActivity(intent)
                    }
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                // Mensaje amigable — no exponemos e.message al usuario
                Toast.makeText(
                    this@SelectRutaActivity,
                    "No se pudieron cargar las rutas. Verificá tu conexión e intentá nuevamente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    inner class RutaAdapter(
        private val rutas: List<RutaReparto>,
        private val onClick: (RutaReparto) -> Unit
    ) : RecyclerView.Adapter<RutaAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nombre: TextView  = view.findViewById(R.id.tv_ruta_nombre)
            val detalle: TextView = view.findViewById(R.id.tv_ruta_detalle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ruta, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val ruta = rutas[position]
            holder.nombre.text  = ruta.nombre
            holder.detalle.text = "${ruta.fecha} | Turno ${ruta.turno} | ${ruta.paradas.size} paradas"
            holder.itemView.setOnClickListener { onClick(ruta) }
        }

        override fun getItemCount() = rutas.size
    }
}
