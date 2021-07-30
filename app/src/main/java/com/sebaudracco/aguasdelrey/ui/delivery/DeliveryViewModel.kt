package com.sebaudracco.aguasdelrey.ui.delivery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sebaudracco.aguasdelrey.data.model.Product

class DeliveryViewModel : ViewModel (){

    var userId: String? = null
    var clientDescription : String ? =null
    var delivered = MutableLiveData<List<Product>>()

    fun setOnCheckProducts(product: MutableList<Product>) {
        delivered.postValue(product)
    }

    fun setOnUncheckProducts(product: MutableList<Product>) {
        delivered.postValue(product)
    }



}