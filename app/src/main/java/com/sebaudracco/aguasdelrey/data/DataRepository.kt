package com.sebaudracco.aguasdelrey.data

import android.content.Context
import com.sebaudracco.aguasdelrey.data.model.RutaReparto
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask
import org.json.JSONArray

object DataRepository {

    fun fetchRutas(context: Context): List<RutaReparto> {
        val json  = ApiService.get(context, "/api/rutas")
        val array = json.getJSONArray("rutas")
        val rutas = mutableListOf<RutaReparto>()
        for (i in 0 until array.length()) {
            val r       = array.getJSONObject(i)
            val paradas = parseParadas(r.getJSONArray("paradas"))
            rutas.add(RutaReparto(
                id         = r.getString("id"),
                nombre     = r.getString("nombre"),
                fecha      = r.getString("fecha"),
                turno      = r.getString("turno"),
                repartidor = r.getString("repartidor"),
                paradas    = paradas,
                estado     = r.optInt("estado", 1)   // ← nuevo campo
            ))
        }
        return rutas
    }

    private fun parseParadas(array: JSONArray): List<ScheduleTask> {
        val paradas = mutableListOf<ScheduleTask>()
        for (i in 0 until array.length()) {
            val p = array.getJSONObject(i)
            paradas.add(ScheduleTask(
                id                 = p.getString("id"),
                taskId             = p.optInt("id_pedido", 0).toString(),
                addressDescription = p.getString("address"),
                clientDescription  = p.getString("clientDescription"),
                inProgress         = false,
                hasHours           = false,
                idleTask           = false,
                peopleCount        = p.getInt("orden"),
                startTime          = "",
                progressive        = false,
                lunch              = false,
                idPedido           = p.optInt("id_pedido", 0),
                idCliente          = p.optInt("id_cliente", 0),
                idEstado           = p.optInt("id_estado", 1),
                estadoNombre       = p.optString("estado_nombre", "Pendiente")
            ))
        }
        return paradas
    }

    private var rutasCache: MutableList<RutaReparto> = mutableListOf()

    fun setCache(rutas: List<RutaReparto>) {
        rutasCache = rutas.toMutableList()
    }

    fun getCache(): List<RutaReparto> = rutasCache

    fun getRutaById(id: String): RutaReparto? = rutasCache.find { it.id == id }

    /**
     * Elimina una parada del caché local después de que fue procesada
     * (entregada, ausente, etc.) sin necesidad de re-sincronizar.
     * Esto evita que el pedido reaparezca al volver a entrar a la ruta.
     */
    fun eliminarParadaDeCache(idRuta: String, idPedido: Int) {
        val idx = rutasCache.indexOfFirst { it.id == idRuta }
        if (idx < 0) return
        val ruta = rutasCache[idx]
        val nuevasParadas = ruta.paradas.filter { it.idPedido != idPedido }
        rutasCache[idx] = ruta.copy(paradas = nuevasParadas)
    }

    /**
     * Actualiza el estado de la ruta en el caché local después de
     * que el repartidor la inicia o la completa desde RouteActivity.
     */
    fun actualizarEstadoRutaEnCache(idRuta: String, nuevoEstado: Int) {
        val idx = rutasCache.indexOfFirst { it.id == idRuta }
        if (idx < 0) return
        rutasCache[idx] = rutasCache[idx].copy(estado = nuevoEstado)
    }
}

