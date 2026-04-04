package com.sebaudracco.aguasdelrey.ui.sync

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.databinding.FragmentFirstBinding
import com.sebaudracco.aguasdelrey.ui.home.ui.home.HomeFragment

/**
 * FirstFragment — pantalla "Resultado de sincronización".
 *
 * Decisión: en lugar de strings hardcodeados, lee los datos guardados
 * en SharedPreferences por SyncActivity o HomeFragment al momento
 * de la última sync exitosa.
 * Así siempre muestra información real y actualizada.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

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

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
