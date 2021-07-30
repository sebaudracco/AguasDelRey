package com.sebaudracco.aguasdelrey.ui.delivery

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.model.Product


class ProductAdapter(var products: MutableList<Product>) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onCheckProducts(product: Product, adapterPosition: Int)
        fun onUnCheckProducts(product: Product)
    }

    private lateinit var mOnClickListener: View.OnClickListener
    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: DeliveryActivity) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_product, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = products[position]
        holder.name.text = item.description

        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            item.delivered = isChecked
            if (isChecked) {
                listener?.onCheckProducts(products!![position], position)
                if (item.description == "Vacío") {
                    holder.delivered.text = "Retirado"
                } else {
                    holder.delivered.text = "Entregado"
                }
                holder.delivered.visibility = View.VISIBLE
            } else {
                holder.delivered.visibility = View.GONE
                listener?.onUnCheckProducts(item)
            }

        }
        with(holder.container) {
            tag = item
            //  setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val container: ConstraintLayout = mView.findViewById(R.id.product)
        val name: TextView = mView.findViewById(R.id.tv_name_product)
        val switch: SwitchCompat = mView.findViewById(R.id.swt_is_delevered)
        val delivered: TextView = mView.findViewById(R.id.lblEntregado)
    }
}
