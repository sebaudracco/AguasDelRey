package com.sebaudracco.aguasdelrey.ui.delivery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
    }

    // ── 1. Inicializar ViewModel con factory que inyecta el repo ──────────────
    private fun initViewModel() {
        val factory = DeliveryFactory(applicationContext)
        viewModel   = ViewModelProvider(this, factory)[DeliveryViewModel::class.java]
    }

    // ── 2. Leer datos del Intent ──────────────────────────────────────────────
    // Decisión: leemos idPedido (nuevo) y mantenemos EXTRA_USER_ID / EXTRA_USER_NAME
    // para no romper el onActivityResult de RouteActivity que espera EXTRA_USER_ID.
    private fun receiveIntentData() {
        viewModel.idPedido         = intent.getIntExtra(Constants.EXTRA_PEDIDO_ID, 0)
        viewModel.userId           = intent.getStringExtra(Constants.EXTRA_USER_ID)
        viewModel.clientDescription= intent.getStringExtra(Constants.EXTRA_USER_NAME)

        if (viewModel.clientDescription != null) {
            binding.tvUserName.text = viewModel.clientDescription
        }

        // Cargar pedido desde la API
        viewModel.cargarPedido()
    }

    // ── 3. Inicializar RecyclerView vacío (se poblará por LiveData) ───────────
    private fun initProductList() {
        deliveredProducts = mutableListOf()

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerDeliver.layoutManager = layoutManager

        adapter = ProductAdapter(mutableListOf(), viewModel)
        adapter.setOnClickListener(this)
        binding.recyclerDeliver.adapter = adapter
        binding.recyclerDeliver.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    // ── 4. Observar LiveData del ViewModel ────────────────────────────────────
    private fun observeViewModel() {

        // Loading: mostrar/ocultar shimmer
        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                binding.shimmerUser.visibility = View.VISIBLE
                binding.shimmerUser.startShimmer()
                binding.recyclerDeliver.visibility = View.GONE
            } else {
                binding.shimmerUser.stopShimmer()
                binding.shimmerUser.visibility = View.GONE
                binding.lyProfileArea.visibility = View.VISIBLE
                binding.recyclerDeliver.visibility = View.VISIBLE
            }
        }

        // Error: mostrar Toast
        viewModel.error.observe(this) { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }
        }

        // Productos cargados: actualizar adapter
        viewModel.productos.observe(this) { productos ->
            adapter.updateProductos(productos.toMutableList())
        }

        // Total recalculado: actualizar TextView
        viewModel.totalPedido.observe(this) { total ->
            binding.tvAmount.text = "$${"%.2f".format(total)}"
        }

        // Lista de entregados: habilitar botón cuando todos están marcados
        viewModel.delivered.observe(this) { entregados ->
            val totalProds = viewModel.productos.value?.size ?: 0
            binding.btnSave.isEnabled = entregados.size == totalProds && totalProds > 0
        }

        // Entrega exitosa: cerrar activity con RESULT_OK
        viewModel.entregaExitosa.observe(this) { exitosa ->
            if (exitosa) {
                val intent = Intent()
                intent.putExtra(Constants.EXTRA_USER_ID, viewModel.userId)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    // ── 5. Botón confirmar entrega ────────────────────────────────────────────
    private fun setupButtons() {
        binding.btnSave.isEnabled = false  // deshabilitado hasta que se entregue todo

        binding.btnSave.setOnClickListener {
            val monto = binding.etAmountCobro.text.toString().toDoubleOrNull()
            val dni   = binding.etDni.text.toString().trim()

            if (monto == null || monto <= 0) {
                Toast.makeText(this, "Ingresá el monto cobrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dni.isEmpty()) {
                Toast.makeText(this, "Ingresá el DNI del receptor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Confirmación antes de enviar
            android.app.AlertDialog.Builder(this)
                .setTitle("Confirmar entrega")
                .setMessage("¿Confirmar entrega al cliente?\nMonto cobrado: $$monto")
                .setCancelable(false)
                .setPositiveButton("Confirmar") { _, _ ->
                    viewModel.confirmarEntrega(monto, dni, "")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    // ── Callbacks del adapter ─────────────────────────────────────────────────
    override fun onCheckProducts(product: Product, adapterPosition: Int) {
        deliveredProducts.add(product)
        viewModel.setOnCheckProducts(deliveredProducts)
    }

    override fun onUnCheckProducts(product: Product) {
        deliveredProducts.remove(product)
        viewModel.setOnUncheckProducts(deliveredProducts)
    }
}
