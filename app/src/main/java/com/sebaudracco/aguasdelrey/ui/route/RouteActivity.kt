package com.sebaudracco.aguasdelrey.ui.route

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
import java.util.*

class RouteActivity : AppCompatActivity(), TasksAdapter.OnClickListener {

    private lateinit var binding: ActivityRouteBinding
    lateinit var viewModel: RouteViewModel
    private lateinit var adapter: TasksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = "Ruta 21 de Agosto"
        initViewModel()
        getDailyTasks()
    }

    private fun initViewModel() {
    }


    private fun getDailyTasks() {
        /* viewModel.getTasksForCurrentDate().observe(this, Observer { tasks ->
             if (::adapter.isInitialized) {
                 updateTaskList(tasks)
             } else {
                         initTasksList(tasks)

             }
             tasks ?: return@Observer
             hideProgressLayout()
         })*/

        initTasksList()
    }

    private fun initTasksList() {

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        val recycler = findViewById<RecyclerView>(R.id.recycler_tasks)
        recycler.layoutManager = layoutManager
        val task = ScheduleTask(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "27 de Abril 370 ",
            "Sebastián Baudracco",
            true,
            true,
            false,
            1,
            "8:45",
            true,
            false
        )

        val task2 = ScheduleTask(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            " Corrientes 18 ",
            "Giuliana Mattio",
            true,
            true,
            false,
            2,
            "9:45",
            true,
            false
        )
        val task3 = ScheduleTask(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "Juan B Justo 726 ",
            "Nicolas Calcagno",
            true,
            true,
            false,
            3,
            "10:45",
            true,
            false
        )
        val task4 = ScheduleTask(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "Ayacucho 468 PB C ",
            "Ezequiel Jimenez",
            true,
            true,
            false,
            4,
            "11:45",
            true,
            false
        )
        val tasks = arrayOf(task4, task3, task2, task)
        adapter = TasksAdapter(tasks)
        adapter.setOnItemActionClickListener(this)
        recycler.adapter = adapter
        recycler.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }



    override fun onCheckProducts(scheduleTask: ScheduleTask, adapterPosition: Int) {
        val intent = Intent().setClass(this, DeliveryActivity::class.java)
        intent.putExtra(Constants.EXTRA_USER_ID,"")
        startActivity(intent)    }

    override fun onUnCheckProducts(scheduleTask: ScheduleTask) {
    }


}