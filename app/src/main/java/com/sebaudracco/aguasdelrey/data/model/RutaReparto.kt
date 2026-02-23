package com.sebaudracco.aguasdelrey.data.model

data class RutaReparto(
    val id: String,
    val nombre: String,
    val fecha: String,
    val turno: String,       // "mañana" o "tarde"
    val repartidor: String,
    val paradas: List<ScheduleTask>
)
