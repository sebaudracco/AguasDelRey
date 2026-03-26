package com.sebaudracco.aguasdelrey.data.model

/**
 * Decisión de diseño:
 * - Agregamos idPedido e idCliente como campos reales de BD.
 * - idPedido viene del campo id_pedido de parada_ruta — ya existe en BD,
 *   solo faltaba mapearlo en el modelo y en la respuesta de /api/rutas.
 * - idCliente viene de pedido.id_cliente — necesario para referencia.
 * - Los campos anteriores (id, taskId como String) se mantienen por
 *   compatibilidad con TasksAdapter hasta que hagamos el refactor completo.
 */
data class ScheduleTask(
    var id: String,
    var taskId: String,
    var addressDescription: String,
    var clientDescription: String,
    var inProgress: Boolean,
    var hasHours: Boolean,
    var idleTask: Boolean,
    var peopleCount: Int,
    var startTime: String,
    var progressive: Boolean,
    var lunch: Boolean,
    // ── Campos nuevos ──────────────────────────────────
    var idPedido: Int = 0,       // FK a pedido.id_pedido (viene de parada_ruta)
    var idCliente: Int = 0       // FK a cliente.id_cliente
)
