package com.sebaudracco.aguasdelrey.ui.route

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.ApiService
import com.sebaudracco.aguasdelrey.data.DataRepository
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask
import com.sebaudracco.aguasdelrey.databinding.ActivityRouteBinding
import com.sebaudracco.aguasdelrey.helpers.Constants
import com.sebaudracco.aguasdelrey.ui.delivery.DeliveryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RouteActivity : AppCompatActivity(), TasksAdapter.OnClickListener {

    companion object {
        const val RC_EDIT_TASKS = 10

        // Estados de ruta_reparto
        const val ESTADO_RUTA_PLANIFICADA = 1
        const val ESTADO_RUTA_EN_CURSO    = 2
        const val ESTADO_RUTA_COMPLETADA  = 3
    }

    private lateinit var binding: ActivityRouteBinding
    lateinit var viewModel: RouteViewModel
    private lateinit var adapter: TasksAdapter
    private lateinit var tasks: MutableList<ScheduleTask>

    private var rutaId: String = ""
    private var estadoRuta: Int = ESTADO_RUTA_PLANIFICADA

    // Referencia al ítem del menú para actualizar su título/ícono dinámicamente
    private var menuItemEstado: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        rutaId = intent.getStringExtra("RUTA_ID") ?: ""
        val ruta = DataRepository.getRutaById(rutaId)

        if (ruta != null) {
            supportActionBar?.title = ruta.nombre
            tasks = ruta.paradas.toMutableList()
            estadoRuta = ruta.estado
        } else {
            supportActionBar?.title = "Ruta de Reparto"
            tasks = mutableListOf()
        }

        initTasksList()
    }

    // ── Menú ────────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_route, menu)
        menuItemEstado = menu.findItem(R.id.action_estado_ruta)
        actualizarMenuEstado()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_estado_ruta -> {
                manejarCambioEstadoRuta()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Actualiza título e ícono del botón de estado según el estado actual de la ruta.
     *   Planificada (1) → "Iniciar ruta"    ícono: ic_travel
     *   En curso    (2) → "Completar ruta"  ícono: ic_cloud_done
     *   Completada  (3) → oculto
     */
    private fun actualizarMenuEstado() {
        menuItemEstado?.let { item ->
            when (estadoRuta) {
                ESTADO_RUTA_PLANIFICADA -> {
                    item.title = "Iniciar ruta"
                    item.setIcon(R.drawable.ic_travel)
                    item.isVisible = true
                }
                ESTADO_RUTA_EN_CURSO -> {
                    item.title = "Completar ruta"
                    item.setIcon(R.drawable.ic_cloud_done)
                    item.isVisible = true
                }
                else -> {
                    // Completada o cancelada — ocultar el botón
                    item.isVisible = false
                }
            }
        }
    }

    /**
     * Decide qué acción ejecutar según el estado actual:
     *   Planificada → confirmar inicio → POST estado=2
     *   En curso    → verificar pedidos pendientes → confirmar cierre → POST estado=3
     */
    private fun manejarCambioEstadoRuta() {
        when (estadoRuta) {
            ESTADO_RUTA_PLANIFICADA -> {
                AlertDialog.Builder(this)
                    .setTitle("Iniciar ruta")
                    .setMessage("¿Confirmar que estás iniciando esta ruta de reparto?\n\nEl estado cambiará a \"En curso\" y se verá reflejado en el sistema.")
                    .setPositiveButton("Sí, iniciar") { _, _ ->
                        cambiarEstadoRuta(ESTADO_RUTA_EN_CURSO)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            ESTADO_RUTA_EN_CURSO -> {
                // Verificar si quedan pedidos sin procesar antes de confirmar
                val pendientes = tasks.count {
                    it.idEstado == 1 || it.idEstado == 2
                }
                if (pendientes > 0) {
                    AlertDialog.Builder(this)
                        .setTitle("Pedidos sin procesar")
                        .setMessage("Todavía quedan $pendientes pedido(s) sin entregar o marcar como ausente.\n\n¿Querés completar la ruta de todas formas?")
                        .setPositiveButton("Sí, completar igual") { _, _ ->
                            cambiarEstadoRuta(ESTADO_RUTA_COMPLETADA)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Completar ruta")
                        .setMessage("Todos los pedidos fueron procesados.\n\n¿Confirmar que la ruta está completada?")
                        .setPositiveButton("Sí, completar") { _, _ ->
                            cambiarEstadoRuta(ESTADO_RUTA_COMPLETADA)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
        }
    }

    private fun cambiarEstadoRuta(nuevoEstado: Int) {
        MainScope().launch {
            try {
                val body = JSONObject().apply {
                    put("id_ruta", rutaId.toInt())
                    put("estado",  nuevoEstado)
                }

                withContext(Dispatchers.IO) {
                    ApiService.post(applicationContext, "/api/estado-ruta", body)
                }

                // Actualizar estado local y en caché
                estadoRuta = nuevoEstado
                DataRepository.actualizarEstadoRutaEnCache(rutaId, nuevoEstado)
                actualizarMenuEstado()

                val mensaje = if (nuevoEstado == ESTADO_RUTA_EN_CURSO)
                    "¡Ruta iniciada! Buen reparto 🚚"
                else
                    "Ruta completada. ¡Bien hecho!"

                Toast.makeText(this@RouteActivity, mensaje, Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@RouteActivity,
                    "No se pudo actualizar el estado de la ruta. Verificá tu conexión.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ── Lista de paradas ────────────────────────────────────────────────────

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
                registrarAusencia(scheduleTask)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun registrarAusencia(scheduleTask: ScheduleTask) {
        MainScope().launch {
            try {
                val body = JSONObject().apply {
                    put("id_pedido", scheduleTask.idPedido)
                }

                withContext(Dispatchers.IO) {
                    ApiService.post(applicationContext, "/api/ausencia", body)
                }

                // Eliminar de la lista local Y del caché — fix problema 2
                val idx = tasks.indexOfFirst { it.idPedido == scheduleTask.idPedido }
                if (idx >= 0) {
                    tasks.removeAt(idx)
                    adapter.notifyItemRemoved(idx)
                }
                DataRepository.eliminarParadaDeCache(rutaId, scheduleTask.idPedido)

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
            val userId   = data?.extras?.getString(Constants.EXTRA_USER_ID)
            val idPedido = data?.extras?.getInt(Constants.EXTRA_PEDIDO_ID, -1) ?: -1

            // Eliminar de la lista local
            tasks.removeIf { it.id == userId }
            initTasksList()

            // Eliminar del caché — fix problema 2
            if (idPedido > 0) {
                DataRepository.eliminarParadaDeCache(rutaId, idPedido)
            }
        }
    }
}


