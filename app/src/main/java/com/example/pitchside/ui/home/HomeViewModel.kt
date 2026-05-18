package com.example.pitchside.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.CompetitionAPI
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.data.Favorite
import com.example.pitchside.data.League
import com.example.pitchside.data.MatchDao.MatchWithTeams
import com.example.pitchside.managers.RetrofitManager
import com.example.pitchside.managers.SessionManager
import com.example.pitchside.repositories.LeagueRepository
import com.example.pitchside.repositories.MatchRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val matchesAPI = RetrofitManager.create<MatchesAPI>()
    private val matchRepository = MatchRepository(db.matchDao(), db.teamDao(), matchesAPI)
    private val competitionAPI = RetrofitManager.create<CompetitionAPI>()
    private val leagueRepository = LeagueRepository(competitionAPI, db.leagueDao(), db.leagueTableDao(), db.leagueScorerDao())
    private val favoriteDao = db.favoriteDao()

    val competitions: LiveData<List<League>> = leagueRepository.getAllLeagues().asLiveData()
    val scheduled: LiveData<List<MatchWithTeams>> = matchRepository.getScheduledMatches().asLiveData()

    private val _favoriteIds = MutableLiveData<Set<Int>>(emptySet())
    val favoriteIds = _favoriteIds

    private val _favoriteLeagueIds = MutableLiveData<Set<Int>>(emptySet())
    val favoriteLeagueIds = _favoriteLeagueIds

    private val _error = MutableLiveData(false)
    val error = _error

    private val _isFetching = MutableLiveData(false)
    val isFetching = _isFetching

    init {
        fetchAllData()
        observeFavorites()
    }


    private fun observeFavorites() {
        val user = SessionManager.loggedInUser ?: return
        viewModelScope.launch {
            favoriteDao.pobierzWszystkieUlubione(user.uzytkownik_id).collect { list ->
                _favoriteIds.postValue(list.filter { it.typ_obiektu == "MECZ" }.map { it.obiekt_id }.toSet())
                _favoriteLeagueIds.postValue(list.filter { it.typ_obiektu == "LIGA" }.map { it.obiekt_id }.toSet())
            }
        }
    }

    fun toggleFavorite(match: MatchWithTeams) {
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

    fun toggleFavoriteLeague(competition: League) {
        val user = SessionManager.loggedInUser ?: return
        val leagueId = competition.liga_id ?: return

        viewModelScope.launch {
            val isFav = favoriteDao.czyUlubiony(user.uzytkownik_id, "LIGA", leagueId)
            if (isFav) {
                favoriteDao.usunZUlubionych(user.uzytkownik_id, "LIGA", leagueId)
            } else {
                favoriteDao.dodajDoUlubionych(
                    Favorite(
                        uzytkownik_id = user.uzytkownik_id,
                        typ_obiektu = "LIGA",
                        obiekt_id = leagueId,
                        nazwa_ligi = competition.nazwa_ligi,
                        emblem_ligi = competition.emblemat_ligi,
                        kod_ligi = competition.kod_ligi
                    )
                )
            }
        }
    }

    fun fetchAllData() {
        viewModelScope.launch {
            _isFetching.value = true
            _error.value = false
            try {
                if(scheduled.value.isNullOrEmpty()){
                    matchRepository.refreshData()
                }
            } catch (e: Exception) {
                _error.value = true
            } finally {
                _isFetching.value = false
            }
        }
    }
}