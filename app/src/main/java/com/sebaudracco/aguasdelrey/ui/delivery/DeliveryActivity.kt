package com.sebaudracco.aguasdelrey.ui.delivery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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

        // Productos cargados desde API → actualizar lista completa
        viewModel.productos.observe(this) { productos ->
            adapter.updateProductos(productos.toMutableList())
        }

        // Fix P3: refresh quirúrgico por ítem cuando cambia +/-
        // sin redibujar toda la lista (evita parpadeo)
        viewModel.productoActualizado.observe(this) { producto ->
            adapter.updateProductoEnPosicion(producto)
        }

        viewModel.totalPedido.observe(this) { total ->
            binding.tvAmount.text = "$${"%.2f".format(total)}"
        }

        // Botón habilitado cuando al menos 1 producto está marcado como entregado
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

        // ✕ — cancelar entrega con confirmación
        binding.ivCancel.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancelar entrega")
                .setMessage("¿Querés cancelar la entrega de este pedido?")
                .setCancelable(true)
                .setPositiveButton("Sí, cancelar") { _, _ -> finish() }
                .setNegativeButton("No", null)
                .show()
        }

        // + — agregar producto extra al pedido
        binding.ivAddProduct.setOnClickListener {
            mostrarDialogAgregarProducto()
        }

        // Finalizar pedido
        binding.btnSave.setOnClickListener {
            // Monto no obligatorio — si está vacío se envía 0
            val monto = binding.etAmountCobro.text.toString().toDoubleOrNull() ?: 0.0
            // DNI no obligatorio — se envía vacío si no se ingresó
            val dni   = binding.etDni.text.toString().replace(".", "").trim()

            AlertDialog.Builder(this)
                .setTitle("Confirmar entrega")
                .setMessage("¿Confirmar la entrega?\nTotal: $${"%.2f".format(viewModel.totalPedido.value ?: 0.0)}")
                .setCancelable(false)
                .setPositiveButton("Confirmar") { _, _ ->
                    viewModel.confirmarEntrega(monto, dni, "")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    /**
     * Formatea el campo DNI como XX.XXX.XXX mientras se escribe.
     * Enviamos solo dígitos al servidor — el formato es solo visual.
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

    /**
     * Dialog para seleccionar producto extra a agregar al pedido.
     * Usa la lista precargada en cargarProductosActivos().
     */
    private fun mostrarDialogAgregarProducto() {
        val productosActivos = viewModel.productosActivos.value
        if (productosActivos.isNullOrEmpty()) {
            Toast.makeText(this, "Cargando productos...", Toast.LENGTH_SHORT).show()
            return
        }
        val nombres  = productosActivos.map {
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
