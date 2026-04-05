package com.sebaudracco.aguasdelrey.ui.sync

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sebaudracco.aguasdelrey.databinding.FragmentFirstBinding
import com.sebaudracco.aguasdelrey.ui.home.ui.home.HomeFragment

/**
 * FirstFragment — pantalla "Resultado de sincronización exitosa".
 *
 * Muestra:
 * - Fecha y hora de la sync
 * - Rutas encontradas en texto plano
 * - Botón "Continuar" para ir a SelectRutaActivity
 * - Auto-avance en 3 segundos si el usuario no toca nada
 *
 * Decisión UX: el auto-avance evita que el repartidor quede bloqueado
 * si no lee la pantalla, pero el botón le da control si quiere leerla.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // Handler para el auto-avance — se cancela si el usuario toca el botón
    private val autoAvanceHandler = Handler(Looper.getMainLooper())
    private val autoAvanceRunnable = Runnable { navegarASelectRuta() }

    private var segundosRestantes = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Leer datos reales de la última sync
        val prefs   = requireContext().getSharedPreferences(
            HomeFragment.PREFS_SYNC, Context.MODE_PRIVATE
        )
        val fecha   = prefs.getString(HomeFragment.KEY_LAST_SYNC, null)
        val resumen = prefs.getString(HomeFragment.KEY_RUTAS_RESUMEN, null)

        if (fecha != null) {
            binding.activationText1.text = "Sincronización exitosa"
            binding.activationText2.text = "Última sync: $fecha"
            binding.activationText3.text = resumen ?: "Sin rutas disponibles"
        } else {
            binding.activationText1.text = "Sin datos de sincronización"
            binding.activationText2.text = "Realizá una sincronización para ver las rutas"
            binding.activationText3.text = ""
        }

        // Botón "Continuar" — cancela el auto-avance y navega inmediatamente
        binding.buttonFirst.text = "Continuar"
        binding.buttonFirst.setOnClickListener {
            autoAvanceHandler.removeCallbacks(autoAvanceRunnable)
            navegarASelectRuta()
        }

        // Iniciar countdown visual en el botón: "Continuar (3)"
        iniciarCountdown()
    }

    /**
     * Countdown visual en el texto del botón.
     * Actualiza cada segundo y navega al llegar a 0.
     */
    private fun iniciarCountdown() {
        segundosRestantes = 3
        actualizarTextoContinuar()

        val countdownRunnable = object : Runnable {
            override fun run() {
                segundosRestantes--
                if (segundosRestantes > 0) {
                    actualizarTextoContinuar()
                    autoAvanceHandler.postDelayed(this, 1000)
                } else {
                    // Llegó a 0 → ejecutar auto-avance
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
        // Cancelar cualquier handler pendiente al salir del fragment
        autoAvanceHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
        _binding = null
    }
}