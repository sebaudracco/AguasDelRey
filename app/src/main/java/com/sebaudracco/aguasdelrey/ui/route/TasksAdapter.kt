package com.sebaudracco.aguasdelrey.ui.route

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask

class TasksAdapter(var scheduleTask: MutableList<ScheduleTask>) :
    RecyclerView.Adapter<TasksAdapter.ViewHolder>() {

    // Estados
    companion object {
        const val ESTADO_PENDIENTE  = 1
        const val ESTADO_EN_RUTA    = 2
        const val ESTADO_ENTREGADO  = 3
        const val ESTADO_CANCELADO  = 4
    }

    interface OnClickListener {
        fun onCheckProducts(scheduleTask: ScheduleTask, adapterPosition: Int)
        fun onUnCheckProducts(scheduleTask: ScheduleTask)
        fun onClienteAusente(scheduleTask: ScheduleTask, adapterPosition: Int)
    }

    private lateinit var itemButtonsClickListener: OnClickListener

    fun setOnItemActionClickListener(l: OnClickListener) {
        itemButtonsClickListener = l
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

        // ── Badge de estado ───────────────────────────────────────────────
        holder.tvEstado.text = item.estadoNombre
        when (item.idEstado) {
            ESTADO_ENTREGADO -> {
                holder.tvEstado.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.color_green)
                )
            }
            ESTADO_CANCELADO -> {
                holder.tvEstado.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.design_default_color_error)
                )
            }
            else -> { // Pendiente / En ruta
                holder.tvEstado.setTextColor(Color.GRAY)
            }
        }

        // ── "Entrega en camino" solo si está Pendiente o En ruta ──────────
        val enCurso = item.idEstado == ESTADO_PENDIENTE || item.idEstado == ESTADO_EN_RUTA
        holder.tvInProgress.visibility = if (enCurso) View.VISIBLE else View.GONE
        holder.pbInProgress.visibility = if (enCurso) View.VISIBLE else View.GONE
        holder.ivTaskClient.visibility = if (enCurso) View.VISIBLE else View.GONE

        // ── Acciones según estado ─────────────────────────────────────────
        if (enCurso) {
            // Pedido procesable — habilitar botones
            holder.tvEntregar.visibility = View.VISIBLE
            holder.tvAusente.visibility  = View.VISIBLE
            holder.tvEntregar.alpha = 1f
            holder.tvAusente.alpha  = 1f

            holder.itemView.setOnClickListener {
                itemButtonsClickListener.onCheckProducts(item, holder.adapterPosition)
            }
            holder.tvEntregar.setOnClickListener {
                itemButtonsClickListener.onCheckProducts(item, holder.adapterPosition)
            }
            holder.tvAusente.setOnClickListener {
                itemButtonsClickListener.onClienteAusente(item, holder.adapterPosition)
            }
        } else {
            // Pedido ya procesado — deshabilitar acciones, mostrar mensaje
            holder.tvEntregar.visibility = View.GONE
            holder.tvAusente.visibility  = View.GONE

            holder.itemView.setOnClickListener {
                // Informar al usuario que no puede operar sobre este pedido
                android.widget.Toast.makeText(
                    holder.itemView.context,
                    "Este pedido ya fue ${item.estadoNombre.lowercase()} y no puede modificarse",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount(): Int = scheduleTask.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val container:    ConstraintLayout = mView.findViewById(R.id.route_client)
        val name:         TextView         = mView.findViewById(R.id.tv_client_value)
        val address:      TextView         = mView.findViewById(R.id.tv_adress_value)
        val count:        TextView         = mView.findViewById(R.id.tv_count)
        val time:         TextView         = mView.findViewById(R.id.tv_task_start_time)
        val tvEntregar:   TextView         = mView.findViewById(R.id.tv_complete_task)
        val tvAusente:    TextView         = mView.findViewById(R.id.tv_ausente)
        val tvEstado:     TextView         = mView.findViewById(R.id.tv_estado_pedido)
        val tvInProgress: TextView         = mView.findViewById(R.id.tv_in_progress)
        val pbInProgress: android.widget.ProgressBar = mView.findViewById(R.id.pb_in_progress)
        val ivTaskClient: android.widget.ImageView   = mView.findViewById(R.id.iv_task_client)
    }
}