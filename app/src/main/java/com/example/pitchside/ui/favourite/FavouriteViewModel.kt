package com.example.pitchside.ui.favourite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.data.Favorite
import com.example.pitchside.data.Resource
import com.example.pitchside.managers.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavouriteViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).favoriteDao()
    private val userId = SessionManager.loggedInUser?.uzytkownik_id ?: -1

    // Funkcja sprawdzająca status logowania
    fun isLoggedIn(): Boolean = SessionManager.isLoggedIn()

    val favoriteMatches: StateFlow<Resource<List<Favorite>>> = dao.pobierzWszystkieUlubione(userId)
        .map<List<Favorite>, Resource<List<Favorite>>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading) }
        .catch { e ->
            emit(Resource.Error(message = e.localizedMessage ?: "Wystapil nieznany blad", exception = Exception(e)))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    fun usunUlubione(favorite: Favorite) {
        viewModelScope.launch {
            dao.usunZUlubionych(userId, favorite.typ_obiektu, favorite.obiekt_id)
        }
    }
}
