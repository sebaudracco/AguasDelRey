package com.sebaudracco.aguasdelrey.data

import com.sebaudracco.aguasdelrey.data.model.LoggedInUser
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            val url = URL("https://administracion-aguadelrey.onrender.com/api/login")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            // Enviar body JSON
            val body = JSONObject().apply {
                put("email", username)
                put("password", password)
            }.toString()

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body)
                writer.flush()
            }

            val httpCode = connection.responseCode

            // Leer respuesta
            val responseStream = if (httpCode == 200) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = responseStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            if (httpCode == 200 && json.optBoolean("ok", false)) {
                val empleado = json.getJSONObject("empleado")
                val token    = json.getString("token")
                val user = LoggedInUser(
                    userId      = empleado.getInt("id").toString(),
                    displayName = "${empleado.getString("nombre")} ${empleado.getString("apellido")}",
                    email       = empleado.getString("email"),
                    idRol       = empleado.getInt("id_rol"),
                    token       = token
                )
                Result.Success(user)
            } else {
                val errorMsg = json.optString("error", "Error de autenticación")
                Result.Error(Exception(errorMsg))
            }

        } catch (e: Exception) {
            Result.Error(Exception("Error de conexión: ${e.message}"))
        }
    }

    fun logout() {
        // El token se elimina desde el Repository
    }
}
