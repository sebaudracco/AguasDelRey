package com.sebaudracco.aguasdelrey.ui.delivery

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sebaudracco.aguasdelrey.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DeliveryViewModel — maneja el estado de la pantalla de entrega.
 *
 * Decisión de diseño:
 * - Recibe el repositorio por constructor (inyección de dependencias manual).
 *   Esto permite testear el ViewModel con un repo falso sin tocar la red.
 * - Exponemos LiveData de solo lectura hacia la Activity (patrón recomendado
 *   por Google: MutableLiveData privado, LiveData público).
 * - Estados de la pantalla: Loading / Success / Error — la Activity reacciona
 *   a estos estados sin lógica propia.
 */
class DeliveryViewModel(
    private val repository: DeliveryRepository
) : ViewModel() {

    // ── Estado general de la pantalla ─────────────────────────────────────────
    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> get() = _error

    // ── Datos del pedido ──────────────────────────────────────────────────────
    private val _productos = MutableLiveData<List<Product>>()
    val productos: MutableLiveData<List<Product>> get() = _productos

    private val _totalPedido = MutableLiveData<Double>()
    val totalPedido: MutableLiveData<Double> get() = _totalPedido

    // ── Estado de entrega ─────────────────────────────────────────────────────
    // 'delivered' mantiene la lista de productos marcados como entregados
    // (lo usa la Activity para saber cuándo habilitar el botón confirmar)
    val delivered = MutableLiveData<List<Product>>(emptyList())

    // ── Resultado de confirmar entrega ────────────────────────────────────────
    private val _entregaExitosa = MutableLiveData<Boolean>()
    val entregaExitosa: MutableLiveData<Boolean> get() = _entregaExitosa

    // ── Metadatos ─────────────────────────────────────────────────────────────
    var idPedido: Int = 0
    var userId: String? = null
    var clientDescription: String? = null

    /**
     * Carga los productos del pedido desde la API.
     * Llamar al inicializar la Activity, una vez que se recibió idPedido.
     */
    fun cargarPedido() {
        if (idPedido <= 0) {
            _error.postValue("ID de pedido inválido")
            return
        }
        _loading.postValue(true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.fetchPedido(idPedido)
            }
            _loading.postValue(false)
            when (result) {
                is PedidoResult.Success -> {
                    _productos.postValue(result.productos)
                    _totalPedido.postValue(result.total)
                }
                is PedidoResult.Error -> {
                    _error.postValue(result.mensaje)
                }
            }
        }
    }

    /**
     * Ajusta la cantidad entregada de un producto (+1).
     * No puede superar la cantidad original del pedido.
     */
    fun incrementCounter(product: Product) {
        val lista = _productos.value?.toMutableList() ?: return
        val idx   = lista.indexOfFirst { it.idDetalle == product.idDetalle }
        if (idx >= 0 && lista[idx].cantidadEntregada < lista[idx].quantity) {
            lista[idx] = lista[idx].copy(cantidadEntregada = lista[idx].cantidadEntregada + 1)
            _productos.postValue(lista)
            recalcularTotal(lista)
        }
    }

    /**
     * Ajusta la cantidad entregada de un producto (-1).
     * No puede bajar de 0.
     */
    fun decreaseCounter(product: Product) {
        val lista = _productos.value?.toMutableList() ?: return
        val idx   = lista.indexOfFirst { it.idDetalle == product.idDetalle }
        if (idx >= 0 && lista[idx].cantidadEntregada > 0) {
            lista[idx] = lista[idx].copy(cantidadEntregada = lista[idx].cantidadEntregada - 1)
            _productos.postValue(lista)
            recalcularTotal(lista)
        }
    }

    private fun recalcularTotal(lista: List<Product>) {
        val total = lista.sumOf { it.cantidadEntregada * it.price }
        _totalPedido.postValue(total)
    }

    fun setOnCheckProducts(entregados: MutableList<Product>) {
        delivered.postValue(entregados)
    }

    fun setOnUncheckProducts(entregados: MutableList<Product>) {
        delivered.postValue(entregados)
    }

    /**
     * Confirma la entrega enviando los datos al servidor.
     */
    fun confirmarEntrega(montoCobrado: Double, dniReceptor: String, observaciones: String) {
        val prods = _productos.value ?: return
        _loading.postValue(true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.confirmarEntrega(
                    EntregaRequest(
                        idPedido      = idPedido,
                        productos     = prods,
                        montoCobrado  = montoCobrado,
                        dniReceptor   = dniReceptor,
                        observaciones = observaciones
                    )
                )
            }
            _loading.postValue(false)
            when (result) {
                is EntregaResult.Success -> _entregaExitosa.postValue(true)
                is EntregaResult.Error   -> _error.postValue(result.mensaje)
            }
        }
    }
}
