package com.example.pitchside.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.data.Favorite
import com.example.pitchside.data.MatchDao
import com.example.pitchside.managers.RetrofitManager
import com.example.pitchside.managers.SessionManager
import com.example.pitchside.repositories.MatchRepository
import kotlinx.coroutines.launch

class MatchDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val matchesAPI = RetrofitManager.create<MatchesAPI>()
    private val db = AppDatabase.getDatabase(application)
    private val favoriteDao = db.favoriteDao()
    private val matchRepository = MatchRepository(db.matchDao(),db.teamDao(), matchesAPI)

    private val matchId = MutableLiveData(-1);

    val match: LiveData<MatchDao.MatchWithTeams> = matchId.switchMap { matchId ->
        matchRepository.getMatchByMatchId(matchId).asLiveData()
    }

    private val _isFavorite = MutableLiveData(false)
    val isFavorite = _isFavorite

    fun setMatchId(matchId: Int){
        observeFavoriteStatus(matchId)
        this.matchId.value = matchId
    }
    private fun observeFavoriteStatus(matchId: Int) {
        val user = SessionManager.loggedInUser ?: return
        viewModelScope.launch {
            favoriteDao.pobierzWszystkieUlubione(user.uzytkownik_id).collect { list ->
                _isFavorite.postValue(list.any { it.typ_obiektu == "MECZ" && it.obiekt_id == matchId })
            }
        }
    }

    fun toggleFavorite() {
        val user = SessionManager.loggedInUser ?: return
        if(matchId.value == null){return}
        val m = match.value ?: return

        viewModelScope.launch {
            val isFav = _isFavorite.value ?: false
            if (isFav) {
                favoriteDao.usunZUlubionych(user.uzytkownik_id, "MECZ", matchId.value)
            } else {
                favoriteDao.dodajDoUlubionych(
                    Favorite(
                        uzytkownik_id = user.uzytkownik_id,
                        typ_obiektu = "MECZ",
                        obiekt_id = matchId.value,
                        nazwa_gospodarza = m.homeTeamName,
                        skrot_gospodarza = m.homeTeamName,
                        herb_gospodarza = m.homeTeamCrest,
                        nazwa_goscia = m.awayTeamName,
                        skrot_goscia = m.awayTeamName,
                        herb_goscia = m.awayTeamCrest
                    )
                )
            }
        }
    }
}