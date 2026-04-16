package com.sebaudracco.aguasdelrey.data.model

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
    // ── Campos de BD ──────────────────────────────────
    var idPedido: Int = 0,
    var idCliente: Int = 0,
    // ── Estado real del pedido ────────────────────────
    // Valores: 1=Pendiente, 2=En ruta, 3=Entregado, 4=Cancelado
    var idEstado: Int = 1,
    var estadoNombre: String = "Pendiente"
)
