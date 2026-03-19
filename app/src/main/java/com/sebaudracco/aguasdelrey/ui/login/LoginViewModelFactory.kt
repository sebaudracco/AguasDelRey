package com.sebaudracco.aguasdelrey.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sebaudracco.aguasdelrey.data.LoginDataSource
import com.sebaudracco.aguasdelrey.data.LoginRepository

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                loginRepository = LoginRepository(LoginDataSource(), context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
