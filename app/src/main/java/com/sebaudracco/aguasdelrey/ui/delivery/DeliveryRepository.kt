package com.sebaudracco.aguasdelrey.ui.delivery

import android.content.Context
import com.sebaudracco.aguasdelrey.data.ApiService
import com.sebaudracco.aguasdelrey.data.model.Product
import org.json.JSONArray
import org.json.JSONObject

/**
 * DeliveryRepository — capa de acceso a datos para el módulo de entrega.
 *
 * Decisión de arquitectura (patrón Repository):
 * En la arquitectura MVVM que usa el proyecto, el ViewModel NO debe saber
 * cómo se obtienen los datos (HTTP, BD local, caché). Esa responsabilidad
 * es del Repository. El ViewModel solo llama métodos del repo y expone
 * LiveData a la Activity.
 *
 * Decisión sobre HttpURLConnection vs ApiService:
 * La versión anterior manejaba HTTP directamente con HttpURLConnection y leía
 * el token de SharedPreferences("auth") con clave "jwt_token".
 * El problema: el token se guarda en "aguadelrey_prefs" con clave "jwt_token"
 * (ver LoginRepository). Al usar SharedPreferences("auth") llegaba vacío → 401.
 *
 * Solución: usar ApiService que ya existe en el proyecto y ya resuelve
 * correctamente el token via LoginRepository.getToken() — misma fuente
 * de verdad que usa DataRepository para /api/rutas (que sí funcionaba).
 * Toda la lógica HTTP (timeouts, headers, error stream) sigue existiendo
 * dentro de ApiService — no se eliminó, se centralizó.
 */
class DeliveryRepository(private val context: Context) {

    /**
     * GET /api/pedido?id_pedido=X
     * Obtiene el pedido con su detalle de productos desde la API.
     * Se ejecuta en un hilo IO (llamar desde viewModelScope con Dispatchers.IO).
     *
     * ApiService.get() internamente:
     * - Lee el token via LoginRepository.getToken() desde "aguadelrey_prefs"
     * - Abre HttpURLConnection con Authorization: Bearer <token>
     * - Maneja connectTimeout=15000, readTimeout=15000
     * - Parsea errorStream si el código no es 200
     * - Lanza excepción si ok=false en la respuesta
     */
    fun fetchPedido(idPedido: Int): PedidoResult {
        return try {
            val json    = ApiService.get(context, "/api/pedido?id_pedido=$idPedido")
            val pedidoJ = json.getJSONObject("pedido")
            val arr     = pedidoJ.getJSONArray("productos")
            val productos = mutableListOf<Product>()

            for (i in 0 until arr.length()) {
                val p   = arr.getJSONObject(i)
                val qty = p.getInt("cantidad")
                productos.add(
                    Product(
                        idDetalle         = p.getInt("id_detalle"),
                        idProducto        = p.getInt("id_producto"),
                        description       = p.getString("nombre"),
                        quantity          = qty,
                        cantidadEntregada = qty,   // por defecto entrega todo
                        delivered         = false,
                        price             = p.getDouble("precio_unitario")
                    )
                )
            }

            PedidoResult.Success(
                productos        = productos,
                clienteNombre    = pedidoJ.optString("cliente_nombre", ""),
                clienteDireccion = pedidoJ.optString("cliente_direccion", ""),
                total            = pedidoJ.optDouble("total", 0.0),
                observaciones    = pedidoJ.optString("observaciones_cliente", "")
            )
        } catch (e: Exception) {
            PedidoResult.Error(e.message ?: "Error de conexión")
        }
    }

    /**
     * GET /api/productos
     * Obtiene productos activos para agregar extras al pedido.
     * Se ejecuta en un hilo IO.
     */
    fun fetchProductosActivos(): ProductosResult {
        return try {
            val json = ApiService.get(context, "/api/productos")
            val arr  = json.getJSONArray("productos")
            val lista = mutableListOf<ProductoActivo>()

            for (i in 0 until arr.length()) {
                val p = arr.getJSONObject(i)
                lista.add(
                    ProductoActivo(
                        idProducto     = p.getInt("id_producto"),
                        nombre         = p.getString("nombre"),
                        precioUnitario = p.getDouble("precio_unitario")
                    )
                )
            }
            ProductosResult.Success(lista)
        } catch (e: Exception) {
            ProductosResult.Error(e.message ?: "Error de conexión")
        }
    }

    /**
     * POST /api/entrega
     * Registra la entrega en el servidor.
     * Se ejecuta en un hilo IO.
     *
     * Nota sobre id_producto en el body: se incluye para que el backend
     * pueda hacer INSERT cuando el id_detalle es negativo (producto extra
     * que el repartidor agregó en campo y no estaba en el pedido original).
     */
    fun confirmarEntrega(request: EntregaRequest): EntregaResult {
        return try {
            val productosArr = JSONArray()
            request.productos.forEach { prod ->
                val obj = JSONObject()
                obj.put("id_detalle",         prod.idDetalle)
                obj.put("id_producto",        prod.idProducto)   // necesario para extras
                obj.put("cantidad_entregada", prod.cantidadEntregada)
                productosArr.put(obj)
            }

            val body = JSONObject()
            body.put("id_pedido",      request.idPedido)
            body.put("productos",      productosArr)
            body.put("monto_cobrado",  request.montoCobrado)
            body.put("dni_receptor",   request.dniReceptor)
            body.put("bidones_vacios", request.bidonesVacios)
            body.put("observaciones",  request.observaciones)

            val json = ApiService.post(context, "/api/entrega", body)
            EntregaResult.Success(json.optDouble("total_final", 0.0))
        } catch (e: Exception) {
            EntregaResult.Error(e.message ?: "Error de conexión")
        }
    }
}

// ── Sealed classes de resultado ───────────────────────────────────────────────
// Decisión: sealed class en lugar de nullable o exceptions.
// El ViewModel puede hacer un 'when' exhaustivo sin riesgo de olvidar el caso error.

sealed class PedidoResult {
    data class Success(
        val productos:        List<Product>,
        val clienteNombre:    String,
        val clienteDireccion: String,
        val total:            Double,
        val observaciones:    String
    ) : PedidoResult()
    data class Error(val mensaje: String) : PedidoResult()
}

sealed class EntregaResult {
    data class Success(val totalFinal: Double) : EntregaResult()
    data class Error(val mensaje: String) : EntregaResult()
}

// ── Request model ─────────────────────────────────────────────────────────────
data class EntregaRequest(
    val idPedido:      Int,
    val productos:     List<com.sebaudracco.aguasdelrey.data.model.Product>,
    val montoCobrado:  Double,
    val dniReceptor:   String,
    val bidonesVacios: Int = 0,
    val observaciones: String
)

// ── Producto activo (para agregar extras al pedido) ───────────────────────────
data class ProductoActivo(
    val idProducto:     Int,
    val nombre:         String,
    val precioUnitario: Double
)

sealed class ProductosResult {
    data class Success(val productos: List<ProductoActivo>) : ProductosResult()
    data class Error(val mensaje: String) : ProductosResult()
}
