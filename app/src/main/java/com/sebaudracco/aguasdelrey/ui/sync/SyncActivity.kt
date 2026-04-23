package com.sebaudracco.aguasdelrey.ui.sync

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.DataRepository
import com.sebaudracco.aguasdelrey.databinding.ActivitySyncBinding
import com.sebaudracco.aguasdelrey.ui.route.SelectRutaActivity
import com.sebaudracco.aguasdelrey.ui.home.ui.home.HomeFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SyncActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySyncBinding
    private lateinit var loadingDialog: AlertDialog

    /**
     * Estado de la última sync — lo lee FirstFragment directamente desde la Activity.
     * Solución al problema de pasar argumentos con Navigation Component cuando
     * FirstFragment es el startDestination (el bundle no llega en ese caso).
     *
     * null  = sync aún no ejecutada (estado inicial)
     * true  = sync exitosa con rutas
     * false = sin rutas o error
     */
    var ultimoSyncOk: Boolean? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_sync)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { ejecutarSync() }
        ejecutarSync()
    }

    fun ejecutarSync() {
        mostrarLoadingDialog()
        MainScope().launch {
            try {
                val rutas = withContext(Dispatchers.IO) {
                    DataRepository.fetchRutas(applicationContext)
                }
                DataRepository.setCache(rutas)

                val ahora   = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val resumen = rutas.joinToString("\n") { "• ${it.nombre}" }
                    .ifEmpty { "Sin rutas disponibles para hoy" }

                getSharedPreferences(HomeFragment.PREFS_SYNC, Context.MODE_PRIVATE)
                    .edit()
                    .putString(HomeFragment.KEY_LAST_SYNC,     ahora)
                    .putString(HomeFragment.KEY_RUTAS_RESUMEN, resumen)
                    .apply()

                ultimoSyncOk = rutas.isNotEmpty()

            } catch (e: Exception) {
                ultimoSyncOk = false

            } finally {
                ocultarLoadingDialog()
                setResult(Activity.RESULT_OK)
                mostrarResultadoEnFragment()
            }
        }
    }

    /**
     * En lugar de navegar a FirstFragment (que ya ES el startDestination y no
     * acepta el bundle), le avisamos directamente al fragment que actualice su UI.
     * El fragment se obtiene por tag del NavHostFragment.
     */
    private fun mostrarResultadoEnFragment() {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_sync)
        val firstFragment = navHost
            ?.childFragmentManager
            ?.fragments
            ?.filterIsInstance<FirstFragment>()
            ?.firstOrNull()

        firstFragment?.aplicarEstado(ultimoSyncOk == true)
    }

    fun navegarASelectRuta() {
        val intent = Intent(this, SelectRutaActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun mostrarLoadingDialog() {
        if (!::loadingDialog.isInitialized) {
            val builder = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_receiving_data, null)
            builder.setView(view)
            builder.setCancelable(false)
            loadingDialog = builder.create()
            loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        if (!loadingDialog.isShowing) loadingDialog.show()
    }

    private fun ocultarLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_sync)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
