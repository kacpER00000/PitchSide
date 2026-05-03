package com.example.pitchside.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.managers.SessionManager
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    val updateSuccess = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    fun zmienNazwe(nowaNazwa: String) {
        val currentUser = SessionManager.loggedInUser ?: return

        viewModelScope.launch {
            try {
                // Tworzymy kopię usera z nową nazwą
                val updatedUser = currentUser.copy(nazwa_uzytkownika = nowaNazwa)
                userDao.aktualizuj(updatedUser)

                // AKTUALIZACJA SESJI - to jest kluczowe!
                SessionManager.loggedInUser = updatedUser
                updateSuccess.value = true
            } catch (e: Exception) {
                error.value = "Nie udało się zmienić nazwy"
            }
        }
    }
}