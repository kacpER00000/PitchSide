package com.example.pitchside.ui.details

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.CompetitionAPI
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.api.responses.MatchResponse
import com.example.pitchside.api.responses.ScorerEntry
import com.example.pitchside.api.responses.ScorersResponse
import com.example.pitchside.api.responses.StandingResponse
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.data.Favorite
import com.example.pitchside.data.League
import com.example.pitchside.data.LeagueScorerDao
import com.example.pitchside.data.LeagueTableDao
import com.example.pitchside.data.MatchDao.MatchWithTeams
import com.example.pitchside.managers.CacheManager
import com.example.pitchside.managers.RetrofitManager
import com.example.pitchside.managers.SessionManager
import com.example.pitchside.repositories.LeagueRepository
import com.example.pitchside.repositories.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.SortedMap

class CompetitionDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val cacheManager = CacheManager(application.applicationContext)
    private val competitionCode = MutableLiveData("")
    private val db = AppDatabase.getDatabase(application)
    private val matchesAPI = RetrofitManager.create<MatchesAPI>()
    private val matchRepository = MatchRepository(db.matchDao(), db.teamDao(), matchesAPI)
    private val competitionAPI = RetrofitManager.create<CompetitionAPI>()
    private val leagueRepository = LeagueRepository(competitionAPI, db.leagueDao(), db.leagueTableDao(), db.leagueScorerDao())
    val scheduled: LiveData<List<MatchWithTeams>> = competitionCode.switchMap { code ->
        matchRepository.getStatusMatchesForLeague(code, "TIMED").asLiveData()
    }

    val finished: LiveData<List<MatchWithTeams>> = competitionCode.switchMap { code ->
        matchRepository.getStatusMatchesForLeague(code, "FINISHED").asLiveData()
    }

    val standing: LiveData<List<LeagueTableDao.LeagueTableWithTeam>> = competitionCode.switchMap { code ->
        leagueRepository.getStandingForLeague(code).asLiveData()
    }

    val scorers: LiveData<List<LeagueScorerDao.LeagueScorerWithTeam>> = competitionCode.switchMap { code ->
        leagueRepository.getLeagueScorersForLeague(code).asLiveData()
    }

    val leagueInfo: LiveData<League> = competitionCode.switchMap { code ->
        leagueRepository.getLeagueByLeagueCode(code).asLiveData()
    }

    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
    val scheduledMatchesByMatchday = scheduled.map { matches ->
        matches.groupBy { getStageOrder(it) }.toSortedMap()
    }
    val finishedMatchesByMatchday = finished.map { matches ->
        matches.groupBy { getStageOrder(it) }.toSortedMap()
    }


    val standingsByGroups = standing.map { tabele ->
        tabele.groupBy { it.group ?: "Tabela" }.toSortedMap()
    }


    private val _error = MutableLiveData(false)
    val error = _error

    private val _isFetching = MutableLiveData(false)
    val isFetching = _isFetching
    private val _isFavorite = MutableLiveData(false)
    val isFavorite = _isFavorite

    fun setCompetitionCode(code: String) {
        competitionCode.value = code
        observeFavoriteStatus()
    }

    private fun observeFavoriteStatus() {
        val user = SessionManager.loggedInUser ?: return
        val code = competitionCode.value ?: return

        viewModelScope.launch {
            favoriteDao.pobierzWszystkieUlubione(user.uzytkownik_id).collect { list ->
                _isFavorite.postValue(list.any { it.typ_obiektu == "LIGA" && it.kod_ligi == code })
            }
        }
    }

    fun toggleFavorite() {
        val user = SessionManager.loggedInUser ?: return
        val code = competitionCode.value ?: return
        if (code.isBlank()) return

        viewModelScope.launch {
            val isFav = _isFavorite.value ?: false
            if (isFav) {
                favoriteDao.usunZUlubionych(user.uzytkownik_id, "LIGA", leagueInfo.value.liga_id)
            } else {
                val newFav = Favorite(
                    uzytkownik_id = user.uzytkownik_id,
                    typ_obiektu = "LIGA",
                    obiekt_id = leagueInfo.value.liga_id,
                    nazwa_ligi = leagueInfo.value.nazwa_ligi,
                    emblem_ligi = leagueInfo.value.emblemat_ligi,
                    kod_ligi = code
                )
                favoriteDao.dodajDoUlubionych(newFav)
            }
        }
    }

    fun fetchData() {
        if (competitionCode.value.isNullOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) {

            _isFetching.postValue(true)
            try {
                val code = competitionCode.value!!
                if (cacheManager.isLeagueCacheExpired(code)) {
                    matchRepository.refreshMatchesForLeague(code)
                    leagueRepository.refreshDataForLeague(code)
                    cacheManager.updateLastLeagueRefresh(code)
                }

            } catch (e: Exception) {
                _error.postValue(true)
            } finally {
                _isFetching.postValue(false)
            }
        }
    }

    private fun getStageOrder(match: MatchWithTeams): Int {
        return when (match.stage) {
            "FINAL" -> 100
            "SEMI_FINALS" -> 99
            "QUARTER_FINALS" -> 98
            else -> match.matchday
        }
    }
}