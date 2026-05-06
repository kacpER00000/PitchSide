package com.example.pitchside.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.data.User
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    val loginResult = MutableLiveData<User?>(null)
    val error = MutableLiveData<String?>(null)
    val isSuccess = MutableLiveData(false)

    fun zaloguj(login: String, haslo: String) {
        viewModelScope.launch {
            val user = userDao.zaloguj(login, haslo)
            if (user != null) {
                com.example.pitchside.managers.SessionManager.loggedInUser = user
                isSuccess.value = true
            } else {
                error.value = "Błędny login lub hasło"
            }
        }
    }

    fun zarejestruj(email: String, haslo: String, nazwa: String) {
        viewModelScope.launch {
            try {
                val newUser = User(email = email, haslo = haslo, nazwa_uzytkownika = nazwa)
                userDao.zarejestruj(newUser)
                isSuccess.value = true
            } catch (e: Exception) {
                error.value = "Użytkownik o takim emailu już istnieje"
            }
        }
    }
}