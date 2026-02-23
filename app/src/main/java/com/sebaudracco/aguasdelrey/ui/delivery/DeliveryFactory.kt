package com.sebaudracco.aguasdelrey.ui.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DeliveryFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeliveryViewModel() as T
    }
}
