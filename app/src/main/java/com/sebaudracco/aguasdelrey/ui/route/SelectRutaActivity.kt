package com.sebaudracco.aguasdelrey.ui.route

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.DataRepository
import com.sebaudracco.aguasdelrey.data.model.RutaReparto
import com.sebaudracco.aguasdelrey.databinding.ActivitySelectRutaBinding

class SelectRutaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectRutaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectRutaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Seleccionar Ruta"

        val rutas = DataRepository.rutas

        binding.recyclerRutas.layoutManager = LinearLayoutManager(this)
        binding.recyclerRutas.adapter = RutaAdapter(rutas) { ruta ->
            val intent = Intent(this, RouteActivity::class.java)
            intent.putExtra("RUTA_ID", ruta.id)
            startActivity(intent)
        }
    }

    // Adapter interno simple para el listado de rutas
    inner class RutaAdapter(
        private val rutas: List<RutaReparto>,
        private val onClick: (RutaReparto) -> Unit
    ) : RecyclerView.Adapter<RutaAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nombre: TextView = view.findViewById(R.id.tv_ruta_nombre)
            val detalle: TextView = view.findViewById(R.id.tv_ruta_detalle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ruta, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val ruta = rutas[position]
            holder.nombre.text = ruta.nombre
            holder.detalle.text = "${ruta.fecha} | Turno ${ruta.turno} | ${ruta.paradas.size} paradas"
            holder.itemView.setOnClickListener { onClick(ruta) }
        }

        override fun getItemCount() = rutas.size
    }
}
