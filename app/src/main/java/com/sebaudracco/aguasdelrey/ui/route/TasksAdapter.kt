package com.sebaudracco.aguasdelrey.ui.route

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask

class TasksAdapter(var scheduleTask: MutableList<ScheduleTask>) :
    RecyclerView.Adapter<TasksAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onCheckProducts(scheduleTask: ScheduleTask, adapterPosition: Int)
        fun onUnCheckProducts(scheduleTask: ScheduleTask)
        // NUEVO: callback para marcar cliente ausente
        fun onClienteAusente(scheduleTask: ScheduleTask, adapterPosition: Int)
    }

    private lateinit var itemButtonsClickListener: OnClickListener

    fun setOnItemActionClickListener(itemButtonClickListener: OnClickListener) {
        this.itemButtonsClickListener = itemButtonClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_scheduled_task, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = scheduleTask[position]
        holder.name.text    = item.clientDescription
        holder.address.text = item.addressDescription
        holder.count.text   = item.peopleCount.toString()
        holder.time.text    = item.startTime

        // Click en toda la fila = Entregar (comportamiento existente)
        holder.itemView.setOnClickListener {
            itemButtonsClickListener.onCheckProducts(item, position)
        }

        // Click en "Entregar" (tv_complete_task) = igual que click en fila
        holder.tvEntregar.setOnClickListener {
            itemButtonsClickListener.onCheckProducts(item, position)
        }

        // Click en "Ausente" — nuevo
        holder.tvAusente.setOnClickListener {
            itemButtonsClickListener.onClienteAusente(item, position)
        }
    }

    override fun getItemCount(): Int = scheduleTask.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val container:   ConstraintLayout = mView.findViewById(R.id.route_client)
        val name:        TextView         = mView.findViewById(R.id.tv_client_value)
        val address:     TextView         = mView.findViewById(R.id.tv_adress_value)
        val count:       TextView         = mView.findViewById(R.id.tv_count)
        val time:        TextView         = mView.findViewById(R.id.tv_task_start_time)
        val tvEntregar:  TextView         = mView.findViewById(R.id.tv_complete_task)
        val tvAusente:   TextView         = mView.findViewById(R.id.tv_ausente)
    }
}