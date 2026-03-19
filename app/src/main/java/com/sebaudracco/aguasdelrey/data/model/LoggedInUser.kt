package com.sebaudracco.aguasdelrey.data.model

/**
 * Data class que representa al empleado autenticado
 */
data class LoggedInUser(
        val userId      : String,
        val displayName : String,
        val email       : String,
        val idRol       : Int,
        val token       : String
)
