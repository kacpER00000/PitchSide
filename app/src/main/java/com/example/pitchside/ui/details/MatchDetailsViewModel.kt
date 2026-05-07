package com.example.pitchside.ui.details

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

class MatchDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val matchesAPI = RetrofitManager.create<MatchesAPI>()
    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()

    private val _match = MutableLiveData<MatchEntry?>(null)
    val match = _match

    private val _isFetching = MutableLiveData(false)
    val isFetching = _isFetching

    // DODANO: Stan ulubionego meczu
    private val _isFavorite = MutableLiveData(false)
    val isFavorite = _isFavorite

    fun fetchMatchDetails(matchId: Int) {
        observeFavoriteStatus(matchId) // Zaczynamy obserwować status w bazie
        viewModelScope.launch {
            _isFetching.value = true
            try {
                val response = matchesAPI.getMatchById(matchId)
                if (response.isSuccessful) {
                    _match.value = response.body()
                }
            } catch (e: Exception) {
                _match.value = null
            } finally {
                _isFetching.value = false
            }
        }
    }

    // DODANO: Obserwowanie czy ten konkretny mecz jest w ulubionych
    private fun observeFavoriteStatus(matchId: Int) {
        val user = SessionManager.loggedInUser ?: return
        viewModelScope.launch {
            favoriteDao.pobierzWszystkieUlubione(user.uzytkownik_id).collect { list ->
                _isFavorite.postValue(list.any { it.typ_obiektu == "MECZ" && it.obiekt_id == matchId })
            }
        }
    }

    // DODANO: Logika przełączania gwiazdki
    fun toggleFavorite() {
        val user = SessionManager.loggedInUser ?: return
        val m = _match.value ?: return
        val matchId = m.id ?: return

        viewModelScope.launch {
            val isFav = _isFavorite.value ?: false
            if (isFav) {
                favoriteDao.usunZUlubionych(user.uzytkownik_id, "MECZ", matchId)
            } else {
                favoriteDao.dodajDoUlubionych(
                    Favorite(
                        uzytkownik_id = user.uzytkownik_id,
                        typ_obiektu = "MECZ",
                        obiekt_id = matchId,
                        nazwa_gospodarza = m.homeTeam.name,
                        skrot_gospodarza = m.homeTeam.shortName,
                        herb_gospodarza = m.homeTeam.crest,
                        nazwa_goscia = m.awayTeam.name,
                        skrot_goscia = m.awayTeam.shortName,
                        herb_goscia = m.awayTeam.crest
                    )
                )
            }
        }
    }
}