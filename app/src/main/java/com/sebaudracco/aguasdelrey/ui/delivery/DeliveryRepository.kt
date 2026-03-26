package com.sebaudracco.aguasdelrey.ui.delivery

import android.content.Context
import com.sebaudracco.aguasdelrey.data.model.Product
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * DeliveryRepository — capa de acceso a datos para el módulo de entrega.
 *
 * Decisión de arquitectura (patrón Repository):
 * En la arquitectura MVVM que usa el proyecto, el ViewModel NO debe saber
 * cómo se obtienen los datos (HTTP, BD local, caché). Esa responsabilidad
 * es del Repository. El ViewModel solo llama métodos del repo y expone
 * LiveData a la Activity.
 * Este patrón es el recomendado por Google para Android y lo que se enseña
 * en Ingeniería de Software como "separación de responsabilidades" (SRP).
 *
 * Sobre el uso de HttpURLConnection en lugar de Retrofit:
 * El proyecto ya usa HttpURLConnection en DataRepository para /api/rutas,
 * entonces mantenemos consistencia tecnológica. Si en el futuro se migra
 * a Retrofit, este es el único archivo que cambia.
 */
class DeliveryRepository(private val context: Context) {

    companion object {
        private const val BASE_URL = "https://administracion-aguadelrey.onrender.com/api"
    }

    /**
     * Obtiene el pedido con su detalle de productos desde la API.
     * Se ejecuta en un hilo IO (llamar desde viewModelScope con Dispatchers.IO).
     */
    fun fetchPedido(idPedido: Int): PedidoResult {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", "") ?: ""

        val url = URL("$BASE_URL/pedido?id_pedido=$idPedido")
        val conn = url.openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 10000
            conn.readTimeout    = 10000

            val code = conn.responseCode
            val body = if (code == 200) {
                conn.inputStream.bufferedReader().readText()
            } else {
                conn.errorStream?.bufferedReader()?.readText() ?: ""
            }

            if (code == 200) {
                val json     = JSONObject(body)
                val pedidoJ  = json.getJSONObject("pedido")
                val productos = mutableListOf<Product>()
                val arr      = pedidoJ.getJSONArray("productos")
                for (i in 0 until arr.length()) {
                    val p = arr.getJSONObject(i)
                    val qty = p.getInt("cantidad")
                    productos.add(
                        Product(
                            idDetalle        = p.getInt("id_detalle"),
                            idProducto       = p.getInt("id_producto"),
                            description      = p.getString("nombre"),
                            quantity         = qty,
                            cantidadEntregada= qty,   // por defecto entrega todo
                            delivered        = false,
                            price            = p.getDouble("precio_unitario")
                        )
                    )
                }
                PedidoResult.Success(
                    productos         = productos,
                    clienteNombre     = pedidoJ.optString("cliente_nombre", ""),
                    clienteDireccion  = pedidoJ.optString("cliente_direccion", ""),
                    total             = pedidoJ.optDouble("total", 0.0),
                    observaciones     = pedidoJ.optString("observaciones_cliente", "")
                )
            } else {
                val errJson = runCatching { JSONObject(body).getString("error") }.getOrDefault("Error $code")
                PedidoResult.Error(errJson)
            }
        } catch (e: Exception) {
            PedidoResult.Error(e.message ?: "Error de conexión")
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Registra la entrega en el servidor.
     * Se ejecuta en un hilo IO.
     */
    fun confirmarEntrega(request: EntregaRequest): EntregaResult {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", "") ?: ""

        val url  = URL("$BASE_URL/entrega")
        val conn = url.openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput      = true
            conn.connectTimeout= 10000
            conn.readTimeout   = 10000

            // Construir JSON del body
            val productosArr = org.json.JSONArray()
            request.productos.forEach { prod ->
                val obj = JSONObject()
                obj.put("id_detalle",         prod.idDetalle)
                obj.put("cantidad_entregada", prod.cantidadEntregada)
                productosArr.put(obj)
            }
            val bodyJson = JSONObject()
            bodyJson.put("id_pedido",     request.idPedido)
            bodyJson.put("productos",     productosArr)
            bodyJson.put("monto_cobrado", request.montoCobrado)
            bodyJson.put("dni_receptor",  request.dniReceptor)
            bodyJson.put("observaciones", request.observaciones)

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(bodyJson.toString())
            writer.flush()

            val code = conn.responseCode
            val body = if (code == 200) {
                conn.inputStream.bufferedReader().readText()
            } else {
                conn.errorStream?.bufferedReader()?.readText() ?: ""
            }

            if (code == 200) {
                val json = JSONObject(body)
                EntregaResult.Success(json.optDouble("total_final", 0.0))
            } else {
                val errJson = runCatching { JSONObject(body).getString("error") }.getOrDefault("Error $code")
                EntregaResult.Error(errJson)
            }
        } catch (e: Exception) {
            EntregaResult.Error(e.message ?: "Error de conexión")
        } finally {
            conn.disconnect()
        }
    }
}

// ── Data classes de resultado (sealed classes para manejar éxito/error) ───────
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

// ── Request model ──────────────────────────────────────────────────────────────
data class EntregaRequest(
    val idPedido:     Int,
    val productos:    List<com.sebaudracco.aguasdelrey.data.model.Product>,
    val montoCobrado: Double,
    val dniReceptor:  String,
    val observaciones:String
)
