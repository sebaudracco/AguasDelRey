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
import com.sebaudracco.aguasdelrey.data.DataRepository

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

        // Recibir la ruta seleccionada desde SelectRutaActivity
        val rutaId = intent.getStringExtra("RUTA_ID") ?: ""
        val ruta = DataRepository.getRutaById(rutaId)

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
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true

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
        val intent = Intent().setClass(this, DeliveryActivity::class.java)
        intent.putExtra(Constants.EXTRA_USER_ID, scheduleTask.id)
        intent.putExtra(Constants.EXTRA_USER_NAME, scheduleTask.clientDescription)
        intent.putExtra("TASK_ID", scheduleTask.id)
        startActivityForResult(intent, RC_EDIT_TASKS)
    }

    override fun onUnCheckProducts(scheduleTask: ScheduleTask) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_EDIT_TASKS && resultCode == Activity.RESULT_OK) {
            val userId = data?.extras?.getString(Constants.EXTRA_USER_ID)
            tasks.removeIf { it.id == userId }
            initTasksList()
        }
    }
}
