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
                // Create a copy of the user with the new username.
                val updatedUser = currentUser.copy(nazwa_uzytkownika = nowaNazwa)
                userDao.aktualizuj(updatedUser)

                // Keep the session in sync with the database update.
                SessionManager.loggedInUser = updatedUser
                updateSuccess.value = true
            } catch (e: Exception) {
                error.value = "Could not change username"
            }
        }
    }
}
