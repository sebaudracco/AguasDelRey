package com.sebaudracco.aguasdelrey.data

import android.content.Context
import com.sebaudracco.aguasdelrey.data.model.RutaReparto
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask
import org.json.JSONArray

/**
 * DataRepository — ahora conecta con la API REST real.
 * Reemplaza los datos hardcodeados por llamadas HTTP.
 */
object DataRepository {

    /**
     * Obtiene las rutas del repartidor desde la API.
     * Debe llamarse desde un hilo de IO (no en el main thread).
     */
    fun fetchRutas(context: Context): List<RutaReparto> {
        val json  = ApiService.get(context, "/api/rutas")
        val array = json.getJSONArray("rutas")
        val rutas = mutableListOf<RutaReparto>()

        for (i in 0 until array.length()) {
            val r       = array.getJSONObject(i)
            val paradas = parseParadas(r.getJSONArray("paradas"), r.getString("id"))
            rutas.add(
                RutaReparto(
                    id         = r.getString("id"),
                    nombre     = r.getString("nombre"),
                    fecha      = r.getString("fecha"),
                    turno      = r.getString("turno"),
                    repartidor = r.getString("repartidor"),
                    paradas    = paradas
                )
            )
        }
        return rutas
    }

    private fun parseParadas(array: JSONArray, rutaId: String): List<ScheduleTask> {
        val paradas = mutableListOf<ScheduleTask>()
        for (i in 0 until array.length()) {
            val p = array.getJSONObject(i)
            paradas.add(
                ScheduleTask(
                    id                = p.getString("id"),
                    routeId           = rutaId,
                    address           = p.getString("address"),
                    clientDescription = p.getString("clientDescription"),
                    isAvailable       = true,
                    isActive          = true,
                    isDelivered       = false,
                    taskOrder         = p.getInt("orden"),
                    scheduledTime     = "",
                    isScheduled       = true,
                    isEmergency       = false
                )
            )
        }
        return paradas
    }

    // Cache en memoria para que RouteActivity pueda acceder por id
    private var rutasCache: List<RutaReparto> = emptyList()

    fun setCache(rutas: List<RutaReparto>) {
        rutasCache = rutas
    }

    fun getRutaById(id: String): RutaReparto? {
        return rutasCache.find { it.id == id }
    }
}
