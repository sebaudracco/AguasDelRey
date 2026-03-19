package com.sebaudracco.aguasdelrey.data

import android.content.Context
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente HTTP centralizado para la API REST de Agua del Rey.
 * Maneja automáticamente el token JWT desde SharedPreferences.
 */
object ApiService {

    private const val BASE_URL = "https://administracion-aguadelrey.onrender.com"

    /**
     * GET autenticado — devuelve el JSONObject de respuesta o lanza excepción
     */
    fun get(context: Context, endpoint: String): JSONObject {
        val token = LoginRepository.getToken(context)
            ?: throw Exception("No hay sesión activa")

        val url = URL("$BASE_URL$endpoint")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = 15000
        connection.readTimeout    = 15000

        val httpCode = connection.responseCode
        val responseText = if (httpCode == 200) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "{}"
        }

        val json = JSONObject(responseText)
        if (!json.optBoolean("ok", false)) {
            throw Exception(json.optString("error", "Error en la API"))
        }
        return json
    }

    /**
     * POST autenticado con body JSON
     */
    fun post(context: Context, endpoint: String, body: JSONObject): JSONObject {
        val token = LoginRepository.getToken(context)
            ?: throw Exception("No hay sesión activa")

        val url = URL("$BASE_URL$endpoint")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput    = true
        connection.connectTimeout = 15000
        connection.readTimeout    = 15000

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(body.toString())
            writer.flush()
        }

        val httpCode = connection.responseCode
        val responseText = if (httpCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "{}"
        }

        val json = JSONObject(responseText)
        if (!json.optBoolean("ok", false)) {
            throw Exception(json.optString("error", "Error en la API"))
        }
        return json
    }
}
