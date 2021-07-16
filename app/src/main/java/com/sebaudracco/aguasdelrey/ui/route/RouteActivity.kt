package com.sebaudracco.aguasdelrey.ui.route

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask
import com.sebaudracco.aguasdelrey.databinding.ActivityRouteBinding
import com.sebaudracco.aguasdelrey.helpers.Constants
import com.sebaudracco.aguasdelrey.ui.delivery.DeliveryActivity
import com.sebaudracco.aguasdelrey.ui.home.HomeActivity
import java.util.*

class RouteActivity : AppCompatActivity(),
    TasksAdapter.OnItemActionClickListener {

    private lateinit var binding: ActivityRouteBinding
    lateinit var viewModel: RouteViewModel
    private lateinit var adapter: TasksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = "Ruta 21 de Agosto"
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "", Snackbar.LENGTH_LONG)
                .setAction("", null).show()
        }
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
            "Avenida SiempreViva  721 ",
            "Sebastián Baudracco",
            true,
            true,
            false,
            1,
            "9:45",
            true,
            false
        )
        val tasks = arrayOf(task, task, task, task, task, task, task, task, task, task, task)
        adapter = TasksAdapter(tasks)
        adapter.setOnItemActionClickListener(this)
        recycler.adapter = adapter
        recycler.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    override fun onPhysicalProgressClick(task: ScheduleTask) {
    }

    override fun onPipelineProgressClick(task: ScheduleTask) {
    }

    override fun onStartTaskClick(task: ScheduleTask) {
        val intent = Intent().setClass(this, DeliveryActivity::class.java)
        intent.putExtra(Constants.EXTRA_USER_ID,"")
        startActivity(intent)
    }


}