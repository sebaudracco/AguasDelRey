package com.sebaudracco.aguasdelrey.data

import com.sebaudracco.aguasdelrey.data.model.LoggedInUser
import java.io.IOException


class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(),
                "Sebastián Baudracco")
        return if(password == "12345678"){
            Result.Success(fakeUser)
        }else{
            Result.Error(IOException("La constraseña no es válida."))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}