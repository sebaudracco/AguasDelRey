package com.sebaudracco.aguasdelrey.ui.route

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.DataRepository
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask
import com.sebaudracco.aguasdelrey.databinding.ActivityRouteBinding
import com.sebaudracco.aguasdelrey.helpers.Constants
import com.sebaudracco.aguasdelrey.ui.delivery.DeliveryActivity
import com.sebaudracco.aguasdelrey.data.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RouteActivity : AppCompatActivity(), TasksAdapter.OnClickListener {

    companion object {
        const val RC_EDIT_TASKS = 10
    }

    private lateinit var binding: ActivityRouteBinding
    lateinit var viewModel: RouteViewModel
    private lateinit var adapter: TasksAdapter
    private lateinit var tasks: MutableList<ScheduleTask>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        val rutaId = intent.getStringExtra("RUTA_ID") ?: ""
        val ruta   = DataRepository.getRutaById(rutaId)

        if (ruta != null) {
            binding.toolbarLayout.title = ruta.nombre
            tasks = ruta.paradas.toMutableList()
        } else {
            binding.toolbarLayout.title = "Ruta de Reparto"
            tasks = mutableListOf()
        }

        initTasksList()
    }

    private fun initTasksList() {
        val layoutManager = LinearLayoutManager(this)
        val recycler = findViewById<RecyclerView>(R.id.recycler_tasks)
        recycler.layoutManager = layoutManager
        adapter = TasksAdapter(tasks)
        adapter.setOnItemActionClickListener(this)
        recycler.adapter = adapter
        recycler.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    // Entregar — navega a DeliveryActivity
    override fun onCheckProducts(scheduleTask: ScheduleTask, adapterPosition: Int) {
        val intent = Intent(this, DeliveryActivity::class.java)
        intent.putExtra(Constants.EXTRA_PEDIDO_ID,  scheduleTask.idPedido)
        intent.putExtra(Constants.EXTRA_CLIENTE_ID, scheduleTask.idCliente)
        intent.putExtra(Constants.EXTRA_USER_ID,    scheduleTask.id)
        intent.putExtra(Constants.EXTRA_USER_NAME,  scheduleTask.clientDescription)
        startActivityForResult(intent, RC_EDIT_TASKS)
    }

    override fun onUnCheckProducts(scheduleTask: ScheduleTask) {}

    /**
     * Cliente ausente — dialog de confirmación → POST /api/ausencia.
     * El pedido pasa a estado 5 (No entregado) y se registra fecha_ausencia.
     */
    override fun onClienteAusente(scheduleTask: ScheduleTask, adapterPosition: Int) {
        AlertDialog.Builder(this)
            .setTitle("Cliente ausente")
            .setMessage(
                "¿Confirmar que el cliente ${scheduleTask.clientDescription} " +
                        "no se encontraba en el domicilio?\n\n" +
                        "El pedido quedará marcado como No entregado y se registrará " +
                        "la fecha y hora del intento."
            )
            .setCancelable(true)
            .setPositiveButton("Sí, confirmar") { _, _ ->
                registrarAusencia(scheduleTask, adapterPosition)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun registrarAusencia(scheduleTask: ScheduleTask, adapterPosition: Int) {
        MainScope().launch {
            try {
                val body = JSONObject()
                body.put("id_pedido", scheduleTask.idPedido)

                withContext(Dispatchers.IO) {
                    ApiService.post(applicationContext, "/api/ausencia", body)
                }

                // Buscar por idPedido — la posición puede haber cambiado si se
                // eliminaron ítems previos durante la misma sesión de ruta
                val idx = tasks.indexOfFirst { it.idPedido == scheduleTask.idPedido }
                if (idx >= 0) {
                    tasks.removeAt(idx)
                    adapter.notifyItemRemoved(idx)
                }

                Toast.makeText(
                    this@RouteActivity,
                    "Ausencia registrada para ${scheduleTask.clientDescription}",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@RouteActivity,
                    "No se pudo registrar la ausencia. Verificá tu conexión e intentá nuevamente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_EDIT_TASKS && resultCode == Activity.RESULT_OK) {
            val userId = data?.extras?.getString(Constants.EXTRA_USER_ID)
            tasks.removeIf { it.id == userId }
            initTasksList()
        }
    }
}

