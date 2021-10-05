package com.sebaudracco.aguasdelrey.ui.delivery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sebaudracco.aguasdelrey.data.model.Product
import kotlinx.coroutines.launch

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


    fun incrementCounter(product:  Product) {
        viewModelScope.launch {
           /* try {
                val c = repository.increment(id)
                refreshedCounterList.postValue(c)

            } catch (e: HttpException) {
                error.postValue(e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                error.postValue("UNKNOWN ERROR")
            }*/
        }
    }

    fun decreaseCounter(product:  Product) {
        viewModelScope.launch {
           /* try {
                val c = repository.decrease(id)
                refreshedCounterList.postValue(c)

            } catch (e: HttpException) {
                error.postValue(e.response()?.errorBody()?.string())
            } catch (e: Exception) {
                error.postValue("UNKNOWN ERROR")
            }*/
        }
    }


}