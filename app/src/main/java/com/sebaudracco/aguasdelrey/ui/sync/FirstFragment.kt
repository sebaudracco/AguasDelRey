package com.sebaudracco.aguasdelrey.ui.sync

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.databinding.FragmentFirstBinding
import com.sebaudracco.aguasdelrey.ui.home.ui.home.HomeFragment

/**
 * FirstFragment — pantalla de resultado de sincronización.
 *
 * El estado (éxito / advertencia) se recibe via aplicarEstado() llamado
 * directamente desde SyncActivity una vez que termina la corrutina.
 *
 * Por qué no usamos argumentos del nav graph:
 * FirstFragment es el startDestination, entonces cuando SyncActivity llama
 * navigate(R.id.FirstFragment, bundle), la condición currentDestination == FirstFragment
 * es true desde el arranque y el navigate (con bundle) nunca se ejecuta.
 */
class FirstFragment : Fragment() {

    companion object {
        const val ARG_SYNC_OK = "SYNC_OK" // se mantiene por compatibilidad
    }

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val autoAvanceHandler = Handler(Looper.getMainLooper())
    private val autoAvanceRunnable = Runnable { navegarASelectRuta() }
    private var segundosRestantes = 3

    // Estado pendiente de aplicar si la sync termina antes de que la vista esté lista
    private var estadoPendiente: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si SyncActivity ya terminó antes de que la vista estuviera lista,
        // aplicamos el estado que quedó pendiente
        estadoPendiente?.let { aplicarEstado(it) }

        binding.buttonFirst.setOnClickListener {
            autoAvanceHandler.removeCallbacks(autoAvanceRunnable)
            navegarASelectRuta()
        }
    }

    /**
     * Llamado por SyncActivity cuando termina la corrutina de sync.
     * Si la vista todavía no está lista, guarda el estado para aplicarlo en onViewCreated.
     */
    fun aplicarEstado(syncOk: Boolean) {
        if (_binding == null) {
            // Vista no lista todavía — guardar para aplicar cuando esté
            estadoPendiente = syncOk
            return
        }

        estadoPendiente = null

        if (syncOk) {
            mostrarEstadoExito()
        } else {
            mostrarEstadoAdvertencia()
        }

        iniciarCountdown()
    }

    private fun mostrarEstadoExito() {
        val prefs   = requireContext().getSharedPreferences(HomeFragment.PREFS_SYNC, Context.MODE_PRIVATE)
        val fecha   = prefs.getString(HomeFragment.KEY_LAST_SYNC, null)
        val resumen = prefs.getString(HomeFragment.KEY_RUTAS_RESUMEN, null)

        binding.syncRoot.setBackgroundResource(R.drawable.final_gradient_top_blue)
        binding.activationIcon.setImageResource(R.drawable.ic_activation_ok)
        binding.activationText1.text = "Sincronización exitosa"
        binding.activationText2.text = if (fecha != null) "Última sync: $fecha" else ""
        binding.activationText3.text = resumen ?: "Sin rutas disponibles"
    }

    private fun mostrarEstadoAdvertencia() {
        binding.syncRoot.setBackgroundResource(R.drawable.final_gradient_top_warning)
        binding.activationIcon.setImageResource(R.drawable.ic_activation_warning)
        binding.activationText1.text = "Sin rutas disponibles"
        binding.activationText2.text = "No hay rutas asignadas para hoy"
        binding.activationText3.text = "Podés continuar de todas formas\no reintentar más tarde"
    }

    private fun iniciarCountdown() {
        // Cancela cualquier countdown previo antes de iniciar uno nuevo
        autoAvanceHandler.removeCallbacksAndMessages(null)
        segundosRestantes = 3
        actualizarTextoContinuar()

        val countdownRunnable = object : Runnable {
            override fun run() {
                segundosRestantes--
                if (segundosRestantes > 0) {
                    actualizarTextoContinuar()
                    autoAvanceHandler.postDelayed(this, 1000)
                } else {
                    autoAvanceHandler.post(autoAvanceRunnable)
                }
            }
        }
        autoAvanceHandler.postDelayed(countdownRunnable, 1000)
    }

    private fun actualizarTextoContinuar() {
        binding.buttonFirst.text = "Continuar ($segundosRestantes)"
    }

    private fun navegarASelectRuta() {
        (activity as? SyncActivity)?.navegarASelectRuta()
    }

    override fun onDestroyView() {
        autoAvanceHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
        _binding = null
    }
}