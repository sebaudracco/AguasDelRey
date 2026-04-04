package com.sebaudracco.aguasdelrey.ui.home.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ramotion.circlemenu.CircleMenuView
import com.sebaudracco.aguasdelrey.databinding.FragmentHomeBinding
import com.sebaudracco.aguasdelrey.helpers.NetworkUtils
import com.sebaudracco.aguasdelrey.ui.map.MapsActivity
import com.sebaudracco.aguasdelrey.ui.route.SelectRutaActivity
import com.sebaudracco.aguasdelrey.ui.sync.SyncActivity
import com.sebaudracco.aguasdelrey.data.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    companion object {
        const val ROUTE_SYNC = 11
        // Claves SharedPreferences para persistir datos de sync
        const val PREFS_SYNC       = "sync_prefs"
        const val KEY_LAST_SYNC    = "last_sync_datetime"
        const val KEY_RUTAS_RESUMEN= "rutas_resumen"
    }

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var countLastSync = 0
    private lateinit var loadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) { textView.text = it }

        setCircleMenu()

        // Al entrar al home, sincronizar automáticamente si hay red
        if (NetworkUtils.isNetworkConnected(requireContext())) {
            sincronizarAutomatico()
        } else {
            // Sin red: mostrar última sync guardada si existe
            mostrarUltimaSync()
        }

        return root
    }

    /**
     * Sincronización automática al entrar al Home.
     * Decisión: misma lógica que el botón de sync manual, pero sin navegar
     * a SyncActivity — se hace en background y actualiza los textos del Home.
     */
    private fun sincronizarAutomatico() {
        mostrarLoadingDialog()
        MainScope().launch {
            try {
                val rutas = withContext(Dispatchers.IO) {
                    DataRepository.fetchRutas(requireContext())
                }
                DataRepository.setCache(rutas)

                // Guardar fecha y resumen en SharedPreferences
                val ahora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val resumen = rutas.joinToString("\n") { "• ${it.nombre}" }
                    .ifEmpty { "Sin rutas disponibles para hoy" }

                requireContext().getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_LAST_SYNC,     ahora)
                    .putString(KEY_RUTAS_RESUMEN, resumen)
                    .apply()

                countLastSync = rutas.size

                ocultarLoadingDialog()
                mostrarUltimaSync()

            } catch (e: Exception) {
                ocultarLoadingDialog()
                // Si falla la sync automática, mostrar última sync guardada sin interrumpir
                mostrarUltimaSync()
                Log.w("HomeFragment", "Sync automática fallida: ${e.message}")
            }
        }
    }

    /**
     * Muestra en pantalla los datos de la última sync guardada en SharedPreferences.
     */
    private fun mostrarUltimaSync() {
        val prefs   = requireContext().getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
        val fecha   = prefs.getString(KEY_LAST_SYNC, null)
        val resumen = prefs.getString(KEY_RUTAS_RESUMEN, null)

        if (fecha != null) {
            countLastSync = 1
            binding.tvLastSync.text      = "Última sincronización:"
            binding.tvLastSyncData.text  = fecha
            binding.tvLastSyncState.text = resumen ?: ""
            binding.tvLastSync.visibility      = View.VISIBLE
            binding.tvLastSyncData.visibility  = View.VISIBLE
            binding.tvLastSyncState.visibility = View.VISIBLE
        } else {
            binding.tvLastSync.text     = "Sin sincronización previa"
            binding.tvLastSync.visibility = View.VISIBLE
        }
    }

    private fun mostrarLoadingDialog() {
        if (!::loadingDialog.isInitialized) {
            val builder = AlertDialog.Builder(requireContext())
            val view = LayoutInflater.from(requireContext())
                .inflate(com.sebaudracco.aguasdelrey.R.layout.dialog_receiving_data, null)
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

    private fun setCircleMenu() {
        val menu: CircleMenuView = binding.circleMenu
        menu.setEventListener(object : CircleMenuView.EventListener() {
            override fun onMenuOpenAnimationStart(view: CircleMenuView) {}
            override fun onMenuOpenAnimationEnd(view: CircleMenuView) {}
            override fun onMenuCloseAnimationStart(view: CircleMenuView) {}
            override fun onMenuCloseAnimationEnd(view: CircleMenuView) {}
            override fun onButtonClickAnimationStart(view: CircleMenuView, index: Int) {}

            override fun onButtonClickAnimationEnd(view: CircleMenuView, index: Int) {
                when (index) {
                    0 -> { // REPARTO
                        if (countLastSync > 0) {
                            startActivity(Intent(requireContext(), SelectRutaActivity::class.java))
                        } else {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Error al iniciar la ruta de repartos.")
                                .setMessage("¡Primero debes sincronizar!")
                                .setNegativeButton("Entendido") { d, _ -> d.dismiss() }
                                .create().show()
                        }
                    }
                    1 -> { // Sync manual
                        if (NetworkUtils.isNetworkConnected(requireContext())) {
                            startActivityForResult(
                                Intent(requireContext(), SyncActivity::class.java),
                                ROUTE_SYNC
                            )
                        } else {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Sin conexión a Internet")
                                .setMessage("Es necesario tener conectividad para sincronizar.")
                                .setNegativeButton("Entendido") { d, _ -> d.dismiss() }
                                .create().show()
                        }
                    }
                    2 -> { // GPS
                        startActivity(Intent(requireContext(), MapsActivity::class.java))
                    }
                }
            }

            override fun onButtonLongClick(view: CircleMenuView, index: Int): Boolean = true
            override fun onButtonLongClickAnimationStart(view: CircleMenuView, index: Int) {}
            override fun onButtonLongClickAnimationEnd(view: CircleMenuView, index: Int) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ROUTE_SYNC && resultCode == Activity.RESULT_OK) {
            // Sync manual completada — refrescar datos en pantalla
            countLastSync = 1
            mostrarUltimaSync()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ocultarLoadingDialog()
        _binding = null
    }
}
