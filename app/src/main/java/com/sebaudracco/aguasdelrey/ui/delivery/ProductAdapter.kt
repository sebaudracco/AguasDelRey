package com.sebaudracco.aguasdelrey.ui.delivery

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.model.Product

/**
 * ProductAdapter — adaptador del RecyclerView de productos en DeliveryActivity.
 *
 * Cambios respecto al original:
 * - updateProductos(): método para refrescar la lista desde LiveData.
 * - Los botones +/- ahora llaman al ViewModel real (sin comentar).
 * - El counter muestra cantidadEntregada (no quantity fija).
 * - Firma de OnClickListener actualizada para el nuevo Product.
 */
class ProductAdapter(
    private var products: MutableList<Product>,
    private val viewModel: DeliveryViewModel
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onCheckProducts(product: Product, adapterPosition: Int)
        fun onUnCheckProducts(product: Product)
    }

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: DeliveryActivity) {
        this.listener = listener
    }

    /**
     * Actualiza la lista completa desde el ViewModel.
     * Decisión: usamos notifyDataSetChanged por simplicidad — para listas
     * pequeñas de productos (< 20 ítems) el costo es despreciable.
     * En una lista grande usaríamos DiffUtil.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateProductos(nuevos: MutableList<Product>) {
        products = nuevos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_product, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = products[position]

        holder.name.text              = item.description
        holder.formatCounterTotal.text= item.cantidadEntregada.toString()

        // Botones +/- ahora conectados al ViewModel real
        holder.subtract.setOnClickListener {
            viewModel.decreaseCounter(item)
        }
        holder.add.setOnClickListener {
            viewModel.incrementCounter(item)
        }

        holder.switch.setOnCheckedChangeListener(null) // evitar trigger al rebind
        holder.switch.isChecked = item.delivered
        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            item.delivered = isChecked
            if (isChecked) {
                holder.deliveredLabel.text       = "Entregado"
                holder.deliveredLabel.visibility = View.VISIBLE
                listener?.onCheckProducts(products[holder.adapterPosition], holder.adapterPosition)
            } else {
                holder.deliveredLabel.visibility = View.GONE
                listener?.onUnCheckProducts(item)
            }
        }
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val container:          ConstraintLayout = mView.findViewById(R.id.product)
        val name:               TextView         = mView.findViewById(R.id.tv_name_product)
        val switch:             SwitchCompat     = mView.findViewById(R.id.swt_is_delevered)
        val deliveredLabel:     TextView         = mView.findViewById(R.id.lblEntregado)
        val formatCounterTotal: TextView         = mView.findViewById(R.id.format_counter_total)
        val subtract:           ImageView        = mView.findViewById(R.id.format_counter_subtract)
        val add:                ImageView        = mView.findViewById(R.id.format_counter_add)
    }
}
