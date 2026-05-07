package com.example.pitchside.ui.scheduled

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.data.Favorite
import com.example.pitchside.managers.RetrofitManager
import com.example.pitchside.managers.SessionManager
import kotlinx.coroutines.launch

class ScheduledViewModel(application: Application) : AndroidViewModel(application) {
    private val _scheduled = MutableLiveData<List<MatchEntry>>(emptyList())
    val scheduled = _scheduled
    private val _error = MutableLiveData(false)
    private val _isFetching = MutableLiveData(false)
    val error = _error
    val isFetching = _isFetching

    // DODANO: Obsługa bazy danych i ulubionych
    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
    private val _favoriteIds = MutableLiveData<Set<Int>>(emptySet())
    val favoriteIds = _favoriteIds

    val matchesAPI = RetrofitManager.create<MatchesAPI>()

    init {
        getScheduledMatches()
        observeFavorites()
    }

    // DODANO: Obserwowanie zmian w bazie danych
    private fun observeFavorites() {
        val user = SessionManager.loggedInUser ?: return
        viewModelScope.launch {
            favoriteDao.pobierzWszystkieUlubione(user.uzytkownik_id).collect { list ->
                _favoriteIds.postValue(list.filter { it.typ_obiektu == "MECZ" }.map { it.obiekt_id }.toSet())
            }
        }
    }

    // DODANO: Logika przełączania gwiazdki
    fun toggleFavorite(match: MatchEntry) {
        val user = SessionManager.loggedInUser ?: return
        val matchId = match.id ?: return

        viewModelScope.launch {
            val isFav = favoriteDao.czyUlubiony(user.uzytkownik_id, "MECZ", matchId)
            if (isFav) {
                favoriteDao.usunZUlubionych(user.uzytkownik_id, "MECZ", matchId)
            } else {
                favoriteDao.dodajDoUlubionych(
                    Favorite(
                        uzytkownik_id = user.uzytkownik_id,
                        typ_obiektu = "MECZ",
                        obiekt_id = matchId,
                        nazwa_gospodarza = match.homeTeam.name,
                        skrot_gospodarza = match.homeTeam.shortName,
                        herb_gospodarza = match.homeTeam.crest,
                        nazwa_goscia = match.awayTeam.name,
                        skrot_goscia = match.awayTeam.shortName,
                        herb_goscia = match.awayTeam.crest
                    )
                )
            }
        }
    }

    fun getScheduledMatches() {
        viewModelScope.launch {
            _isFetching.value = true
            try {
                val response = matchesAPI.getScheduledMatches()
                if (response.isSuccessful) {
                    _scheduled.value = response.body()?.matches
                    _error.value = false
                } else {
                    _error.value = true
                }
            } catch (e: Exception) {
                _error.value = true
            } finally {
                _isFetching.value = false
            }
        }
    }
}