package com.sebaudracco.aguasdelrey.ui.delivery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.model.Product
import com.sebaudracco.aguasdelrey.databinding.ActivityDeliveryBinding
import com.sebaudracco.aguasdelrey.helpers.Constants
import com.sebaudracco.aguasdelrey.ui.delivery.ui.main.SectionsPagerAdapter
import java.util.*

class DeliveryActivity : AppCompatActivity(), ProductAdapter.OnClickListener {

    private lateinit var binding: ActivityDeliveryBinding
    lateinit var adapter: ProductAdapter
    private lateinit var deliveredProducts: MutableList<Product>
    private lateinit var products: MutableList<Product>
    private lateinit var viewModel: DeliveryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter

        initViewModel()
        loadShimmer()

        deliveredProducts = mutableListOf()
        products = mutableListOf()
        observeViewModel()
        initProductList()
        viewModel.userId = intent.extras?.getString(Constants.EXTRA_USER_ID)
        viewModel.clientDescription = intent.extras?.getString(Constants.EXTRA_USER_NAME)
        if (viewModel.clientDescription != null) {
            binding.tvUserName.text = viewModel.clientDescription
        }
    }

    private fun initViewModel() {
        val factory = DeliveryFactory()
        viewModel = ViewModelProvider(this, factory).get(DeliveryViewModel::class.java)
    }

    private fun initProductList() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true

        val recyclerInitial = findViewById<RecyclerView>(R.id.recycler_deliver)
        recyclerInitial.layoutManager = layoutManager

        val product = Product(UUID.randomUUID().toString(), "Bidón 20 litros", 2, false, "0")
        val product2 = Product(UUID.randomUUID().toString(), "Abono mensual", 1, false, "0")
        val product3 = Product(UUID.randomUUID().toString(), "Café La Virginia 500 gr.", 1, false, "0")

        products = arrayOf(product, product2, product3).toMutableList()
        adapter = ProductAdapter(products, viewModel)
        adapter.setOnClickListener(this)

        recyclerInitial.adapter = adapter
        recyclerInitial.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun loadShimmer() {
        binding.shimmerUser.visibility = View.VISIBLE
        binding.shimmerUser.startShimmer()   // API actualizada: startShimmer()
        Handler(Looper.getMainLooper()).postDelayed({
            hideShimmer()
        }, 5000)
    }

    private fun hideShimmer() {
        try {
            binding.shimmerUser.stopShimmer()  // API actualizada: stopShimmer()
            binding.shimmerUser.visibility = View.GONE
            binding.lyProfileArea.visibility = View.VISIBLE
        } catch (e: NullPointerException) {
        }
    }

    private fun observeViewModel() {
        viewModel.delivered.observe(this, androidx.lifecycle.Observer {
            if (it.size == products.size) {
                val dialog = android.app.AlertDialog.Builder(this)
                    .setTitle("Entrega finalizada")
                    .setCancelable(false)
                    .setMessage(R.string.activity_delivered_products)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        deliveredProducts.clear()
                        val intent = Intent()
                        intent.putExtra(Constants.EXTRA_USER_ID, viewModel.userId)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                    .setNegativeButton("Revisar entrega") { dialog, _ -> }
                dialog.show()
            }
        })
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
