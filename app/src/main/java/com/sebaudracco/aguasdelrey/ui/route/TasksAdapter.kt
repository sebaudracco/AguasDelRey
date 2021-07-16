package com.sebaudracco.aguasdelrey.ui.route

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask


class TasksAdapter(private var scheduledTasks: Array<ScheduleTask>) : RecyclerView.Adapter<TasksAdapter.ReportTasksHolder>() {

    interface OnItemActionClickListener {
        fun onPhysicalProgressClick(task: ScheduleTask)
        fun onPipelineProgressClick(task: ScheduleTask)
        fun onStartTaskClick(task: ScheduleTask)
    }

    private lateinit var itemButtonsClickListener: OnItemActionClickListener

    fun setOnItemActionClickListener(itemButtonClickListener: OnItemActionClickListener) {
        this.itemButtonsClickListener = itemButtonClickListener
    }

    fun setTasks(scheduledTasks: Array<ScheduleTask>) {
        this.scheduledTasks = scheduledTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportTasksHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_report_scheduled_task,
                parent, false)

        return ReportTasksHolder(view)
    }

    override fun getItemCount(): Int {
        return scheduledTasks.size
    }

    override fun onBindViewHolder(holder: ReportTasksHolder, position: Int) {
        val task = scheduledTasks[position]
        holder.bind(task)


         holder.itemView.setOnClickListener {
            itemButtonsClickListener.onStartTaskClick(task)
        }

       /* holder.itemView.tv_physical_progress.setOnClickListener {
            if (realTimeTask.progressive) {
                itemButtonsClickListener.onPipelineProgressClick(realTimeTask)
            } else {
                itemButtonsClickListener.onPhysicalProgressClick(realTimeTask)
            }
        }*/
    }

    class ReportTasksHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(task: ScheduleTask) {

           // itemView. tv_client_value.text = task.clientDescription
           // itemView.tv_adress_value.text = task.addressDescription
           // itemView.tv_person_count.text = task.peopleCount.toString()
            setProgressTypeText(task.progressive)
            showAsUnproductiveTask(task.idleTask)
            showTaskStateAndAvailableActions(task)
            showAsLunchTask(task)
        }

        private fun showAsLunchTask(task: ScheduleTask) {

            if (task.lunch) {
             //   itemView.iv_task.setImageResource(R.drawable.ic_restaurant)
            } else {
             //   itemView.iv_task.setImageResource(R.drawable.ic_assignment)
            }
        }

        private fun setProgressTypeText(progressive: Boolean) {
            if (progressive) {
             //   itemView.tv_physical_progress.text = itemView.context.getString(R.string.add_daily_task_item_tv_pipeline_progress)
            } else {
            //    itemView.tv_physical_progress.text = itemView.context.getString(R.string.add_daily_task_item_tv_physical_progress)
            }
        }

        private fun showTaskStateAndAvailableActions(task: ScheduleTask) {
            when {
                task.inProgress -> {
                  /*  itemView.tv_complete_task.text = itemView.context
                        .getString(R.string.real_time_report_activity_btn_see_presentism_participants)

                    itemView.tv_complete_task.visibility = View.VISIBLE
                    itemView.pb_in_progress.visibility = View.VISIBLE
                    itemView.tv_in_progress.visibility = View.VISIBLE*/
                }
                else -> {
                   /* itemView.tv_complete_task.text = itemView.context.getString(R.string.real_time_report_activity_start_task)
                    itemView.tv_complete_task.visibility = View.VISIBLE
                    itemView.pb_in_progress.visibility = View.GONE
                    itemView.tv_in_progress.visibility = View.GONE*/
                }
            }
        }

        private fun showAsUnproductiveTask(idleTask: Boolean) {
           /* if (idleTask) {
                itemView.tv_idle_hours.visibility = View.VISIBLE
                itemView.tv_physical_progress.visibility = View.GONE
            } else {
                itemView.tv_idle_hours.visibility = View.GONE
                itemView.tv_physical_progress.visibility = View.VISIBLE
            }*/
        }

    }
}