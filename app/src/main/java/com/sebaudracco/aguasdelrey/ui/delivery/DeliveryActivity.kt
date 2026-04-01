package com.sebaudracco.aguasdelrey.ui.delivery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sebaudracco.aguasdelrey.data.model.Product
import com.sebaudracco.aguasdelrey.databinding.ActivityDeliveryBinding
import com.sebaudracco.aguasdelrey.helpers.Constants

class DeliveryActivity : AppCompatActivity(), ProductAdapter.OnClickListener {

    private lateinit var binding: ActivityDeliveryBinding
    private lateinit var adapter: ProductAdapter
    private lateinit var deliveredProducts: MutableList<Product>
    private lateinit var viewModel: DeliveryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        receiveIntentData()
        initProductList()
        observeViewModel()
        setupButtons()
        setupDniFormatter()
        setupBidonesSpinner()
    }

    private fun initViewModel() {
        val factory = DeliveryFactory(applicationContext)
        viewModel   = ViewModelProvider(this, factory)[DeliveryViewModel::class.java]
    }

    private fun receiveIntentData() {
        viewModel.idPedido          = intent.getIntExtra(Constants.EXTRA_PEDIDO_ID, 0)
        viewModel.userId            = intent.getStringExtra(Constants.EXTRA_USER_ID)
        viewModel.clientDescription = intent.getStringExtra(Constants.EXTRA_USER_NAME)

        if (viewModel.clientDescription != null) {
            binding.tvUserName.text = viewModel.clientDescription
        }

        viewModel.cargarPedido()
        viewModel.cargarProductosActivos()
    }

    private fun initProductList() {
        deliveredProducts = mutableListOf()
        binding.recyclerDeliver.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter(mutableListOf(), viewModel)
        adapter.setOnClickListener(this)
        binding.recyclerDeliver.adapter = adapter
        binding.recyclerDeliver.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun observeViewModel() {

        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                binding.shimmerUser.visibility     = View.VISIBLE
                binding.shimmerUser.startShimmer()
                binding.recyclerDeliver.visibility = View.GONE
            } else {
                binding.shimmerUser.stopShimmer()
                binding.shimmerUser.visibility     = View.GONE
                binding.lyProfileArea.visibility   = View.VISIBLE
                binding.recyclerDeliver.visibility = View.VISIBLE
            }
        }

        viewModel.error.observe(this) { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.productos.observe(this) { productos ->
            adapter.updateProductos(productos.toMutableList())
        }

        viewModel.productoActualizado.observe(this) { producto ->
            adapter.updateProductoEnPosicion(producto)
        }

        viewModel.totalPedido.observe(this) { total ->
            binding.tvAmount.text = "$${"%.2f".format(total)}"
        }

        viewModel.delivered.observe(this) { entregados ->
            binding.btnSave.isEnabled = entregados.isNotEmpty()
        }

        viewModel.entregaExitosa.observe(this) { exitosa ->
            if (exitosa) {
                val intent = Intent()
                intent.putExtra(Constants.EXTRA_USER_ID, viewModel.userId)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun setupButtons() {
        binding.btnSave.isEnabled = false

        binding.ivCancel.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancelar entrega")
                .setMessage("¿Querés cancelar la entrega de este pedido?")
                .setCancelable(true)
                .setPositiveButton("Sí, cancelar") { _, _ -> finish() }
                .setNegativeButton("No", null)
                .show()
        }

        binding.ivAddProduct.setOnClickListener {
            mostrarDialogAgregarProducto()
        }

        binding.btnSave.setOnClickListener {
            mostrarDialogConfirmacion()
        }
    }

    /**
     * Dialog de confirmación que muestra los productos con switch activado.
     * Decisión: en lugar de mostrar solo el total (que puede confundir al repartidor),
     * mostramos la lista de lo que realmente se confirmó entregar — más transparente
     * y evita errores de confirmación accidental.
     */
    private fun mostrarDialogConfirmacion() {
        val monto  = binding.etAmountCobro.text.toString().toDoubleOrNull() ?: 0.0
        val dni    = binding.etDni.text.toString().replace(".", "").trim()
        val bidones= binding.spinnerBidones.selectedItem.toString().toIntOrNull() ?: 0

        // Construir resumen de productos entregados (solo los con switch activado)
        val entregados = viewModel.delivered.value ?: emptyList()
        val resumen = buildString {
            appendLine("Productos entregados:")
            appendLine()
            entregados.forEach { prod ->
                appendLine("• ${prod.description} x${prod.cantidadEntregada}")
            }
            if (bidones > 0) {
                appendLine()
                appendLine("Bidones vacíos: $bidones")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar entrega")
            .setMessage(resumen)
            .setCancelable(false)
            .setPositiveButton("Confirmar") { _, _ ->
                viewModel.confirmarEntrega(monto, dni, bidones, "")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Spinner con valores 0-20 para bidones vacíos devueltos.
     * Decisión: rango 0-20 basado en casos reales de reparto de agua.
     * Spinner evita errores de tipeo del repartidor en campo.
     */
    private fun setupBidonesSpinner() {
        val valores = (0..20).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, valores)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBidones.adapter = adapter
        binding.spinnerBidones.setSelection(0) // por defecto: 0 bidones
    }

    /**
     * Formatea el campo DNI como XX.XXX.XXX mientras se escribe.
     */
    private fun setupDniFormatter() {
        binding.etDni.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val digits = s.toString().replace(".", "").take(8)
                val formatted = when {
                    digits.length <= 2 -> digits
                    digits.length <= 5 -> "${digits.take(2)}.${digits.drop(2)}"
                    else -> "${digits.take(2)}.${digits.drop(2).take(3)}.${digits.drop(5)}"
                }
                s.replace(0, s.length, formatted)
                isFormatting = false
            }
        })
    }

    private fun mostrarDialogAgregarProducto() {
        val productosActivos = viewModel.productosActivos.value
        if (productosActivos.isNullOrEmpty()) {
            Toast.makeText(this, "Cargando productos...", Toast.LENGTH_SHORT).show()
            return
        }
        val nombres = productosActivos.map {
            "${it.nombre} — $${"%.2f".format(it.precioUnitario)}"
        }.toTypedArray()
        var seleccion = 0
        AlertDialog.Builder(this)
            .setTitle("Agregar producto extra")
            .setSingleChoiceItems(nombres, 0) { _, which -> seleccion = which }
            .setPositiveButton("Agregar") { _, _ ->
                viewModel.agregarProductoExtra(productosActivos[seleccion])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onCheckProducts(product: Product, adapterPosition: Int) {
        deliveredProducts.add(product)
        viewModel.setOnCheckProducts(deliveredProducts)
    }

    override fun onUnCheckProducts(product: Product) {
        deliveredProducts.remove(product)
        viewModel.setOnUncheckProducts(deliveredProducts)
    }
}

