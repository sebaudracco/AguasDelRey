package com.sebaudracco.aguasdelrey.data.model

data class RutaReparto(
    val id: String,
    val nombre: String,
    val fecha: String,
    val turno: String,       // "mañana" o "tarde"
    val repartidor: String,
    val paradas: List<ScheduleTask>,
    val estado: Int = 1      // 1=Planificada, 2=En curso, 3=Completada, 4=Cancelada
)
