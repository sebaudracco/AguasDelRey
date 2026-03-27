package com.sebaudracco.aguasdelrey.ui.route

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask
import com.sebaudracco.aguasdelrey.databinding.ActivityRouteBinding
import com.sebaudracco.aguasdelrey.helpers.Constants
import com.sebaudracco.aguasdelrey.ui.delivery.DeliveryActivity

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
        // R3 FIX: orden natural ASC, sin reverseLayout ni stackFromEnd
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

    override fun onCheckProducts(scheduleTask: ScheduleTask, adapterPosition: Int) {
        val intent = Intent(this, DeliveryActivity::class.java)

        // ── Cambio principal respecto al original ────────────────────────────
        // Antes: se pasaba scheduleTask.id como EXTRA_USER_ID (incorrecto,
        //        era el id de la tarea, no del cliente ni del pedido).
        // Ahora: pasamos idPedido (de BD) y mantenemos EXTRA_USER_ID con el
        //        taskId para que onActivityResult siga funcionando igual.
        intent.putExtra(Constants.EXTRA_PEDIDO_ID,  scheduleTask.idPedido)   // NUEVO
        intent.putExtra(Constants.EXTRA_CLIENTE_ID, scheduleTask.idCliente)  // NUEVO
        intent.putExtra(Constants.EXTRA_USER_ID,    scheduleTask.id)         // existente — para RESULT_OK
        intent.putExtra(Constants.EXTRA_USER_NAME,  scheduleTask.clientDescription)

        startActivityForResult(intent, RC_EDIT_TASKS)
    }

    override fun onUnCheckProducts(scheduleTask: ScheduleTask) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Sin cambios — sigue funcionando igual que antes
        if (requestCode == RC_EDIT_TASKS && resultCode == Activity.RESULT_OK) {
            val userId = data?.extras?.getString(Constants.EXTRA_USER_ID)
            tasks.removeIf { it.id == userId }
            initTasksList()
        }
    }
}
