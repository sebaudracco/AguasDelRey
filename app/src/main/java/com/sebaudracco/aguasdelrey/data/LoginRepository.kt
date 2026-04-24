package com.sebaudracco.aguasdelrey.data

import android.content.Context
import android.content.SharedPreferences
import com.sebaudracco.aguasdelrey.data.model.LoggedInUser

class LoginRepository(val dataSource: LoginDataSource, private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aguadelrey_prefs", Context.MODE_PRIVATE)

    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null || prefs.getString("jwt_token", null) != null

    init {
        user = null
    }

    fun logout() {
        user = null
        prefs.edit().clear().apply()
        dataSource.logout()
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        val result = dataSource.login(username, password)
        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }
        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        prefs.edit().apply {
            putString("jwt_token",       loggedInUser.token)
            putString("empleado_nombre", loggedInUser.displayName)
            putString("empleado_email",  loggedInUser.email)
            putInt("empleado_id",        loggedInUser.userId.toInt())
            putInt("empleado_rol",       loggedInUser.idRol)
            apply()
        }
    }

    companion object {
        fun getToken(context: Context): String? =
            context.getSharedPreferences("aguadelrey_prefs", Context.MODE_PRIVATE)
                .getString("jwt_token", null)

        fun getNombre(context: Context): String? =
            context.getSharedPreferences("aguadelrey_prefs", Context.MODE_PRIVATE)
                .getString("empleado_nombre", null)

        // Agregado para mostrar el email en el header del drawer
        fun getEmail(context: Context): String? =
            context.getSharedPreferences("aguadelrey_prefs", Context.MODE_PRIVATE)
                .getString("empleado_email", null)

        fun getRol(context: Context): Int =
            context.getSharedPreferences("aguadelrey_prefs", Context.MODE_PRIVATE)
                .getInt("empleado_rol", 0)

        fun clearSession(context: Context) {
            context.getSharedPreferences("aguadelrey_prefs", Context.MODE_PRIVATE)
                .edit().clear().apply()
        }
    }
}
