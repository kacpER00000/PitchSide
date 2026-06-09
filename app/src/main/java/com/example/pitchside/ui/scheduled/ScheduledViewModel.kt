package com.example.pitchside.ui.scheduled

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.data.Favorite
import com.example.pitchside.data.MatchDao
import com.example.pitchside.managers.RetrofitManager
import com.example.pitchside.managers.SessionManager
import com.example.pitchside.repositories.MatchRepository
import kotlinx.coroutines.launch

class ScheduledViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val matchesAPI = RetrofitManager.create<MatchesAPI>()
    private val matchRepository = MatchRepository(db.matchDao(), db.teamDao(), matchesAPI)
    val scheduled = matchRepository.getScheduledMatches().asLiveData()
    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
    private val _favoriteIds = MutableLiveData<Set<Int>>(emptySet())
    val favoriteIds = _favoriteIds

    init {
        observeFavorites()
    }
    private fun observeFavorites() {
        val user = SessionManager.loggedInUser ?: return
        viewModelScope.launch {
            favoriteDao.pobierzWszystkieUlubione(user.uzytkownik_id).collect { list ->
                _favoriteIds.postValue(list.filter { it.typ_obiektu == "MECZ" }.map { it.obiekt_id }.toSet())
            }
        }
    }
    fun toggleFavorite(match: MatchDao.MatchWithTeams) {
        val user = SessionManager.loggedInUser ?: return
        val matchId = match.matchId ?: return

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
                        nazwa_gospodarza = match.homeTeamName,
                        skrot_gospodarza = match.homeTeamName,
                        herb_gospodarza = match.homeTeamCrest,
                        nazwa_goscia = match.awayTeamName,
                        skrot_goscia = match.awayTeamName,
                        herb_goscia = match.awayTeamCrest
                    )
                )
            }
        }
    }
}