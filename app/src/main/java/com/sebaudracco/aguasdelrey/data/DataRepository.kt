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
                paradas    = paradas
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
                // ── Campos nuevos ────────────────────────────────────────────
                // Decisión: usamos optInt() en lugar de getInt() para que si
                // por algún motivo el campo no viene en la respuesta, el valor
                // quede en 0 en vez de lanzar una excepción que rompa toda la
                // carga de rutas. Fail-safe sobre fail-fast aquí porque es UI.
                idPedido           = p.optInt("id_pedido", 0),
                idCliente          = p.optInt("id_cliente", 0)
            ))
        }
        return paradas
    }

    private var rutasCache: List<RutaReparto> = emptyList()

    fun setCache(rutas: List<RutaReparto>) { rutasCache = rutas }

    fun getRutaById(id: String): RutaReparto? = rutasCache.find { it.id == id }
}
