package com.sebaudracco.aguasdelrey.ui.delivery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * DeliveryFactory — fábrica del ViewModel con inyección del repositorio.
 *
 * Decisión: ViewModelProvider.Factory es necesario cuando el ViewModel
 * tiene parámetros en el constructor. Android no puede instanciar el VM
 * solo — necesita que le digamos cómo construirlo.
 * Recibe Context para que el repositorio pueda acceder a SharedPreferences
 * (donde está guardado el JWT token).
 */
class DeliveryFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = DeliveryRepository(context)
        return DeliveryViewModel(repository) as T
    }
}
