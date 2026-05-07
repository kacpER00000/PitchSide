package com.example.pitchside.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.CompetitionAPI
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.CompetitionResponse
import com.example.pitchside.api.responses.CompetitionsResponse
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.api.responses.MatchResponse
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.data.Favorite
import com.example.pitchside.managers.RetrofitManager
import com.example.pitchside.managers.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.Response

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val competitionAPI = RetrofitManager.create<CompetitionAPI>()
    private val matchesAPI = RetrofitManager.create<MatchesAPI>()
    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()

    private val _competitions = MutableLiveData<List<CompetitionResponse>>(emptyList())
    val competitions = _competitions

    private val _scheduled = MutableLiveData<List<MatchEntry>>(emptyList())
    val scheduled = _scheduled

    private val _favoriteIds = MutableLiveData<Set<Int>>(emptySet())
    val favoriteIds = _favoriteIds

    // NOWE: ID ulubionych lig
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
                // Rozdzielamy mecze od lig na dwie listy
                _favoriteIds.postValue(list.filter { it.typ_obiektu == "MECZ" }.map { it.obiekt_id }.toSet())
                _favoriteLeagueIds.postValue(list.filter { it.typ_obiektu == "LIGA" }.map { it.obiekt_id }.toSet())
            }
        }
    }

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

    // NOWE: Obsługa ulubionej ligi
    fun toggleFavoriteLeague(competition: CompetitionResponse) {
        val user = SessionManager.loggedInUser ?: return
        val leagueId = competition.id ?: return

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
                        nazwa_ligi = competition.name,
                        emblem_ligi = competition.emblem,
                        kod_ligi = competition.code
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
                val compDef = async { competitionAPI.getAllCompetitions() }
                val matchDef = async { matchesAPI.getScheduledMatches() }
                val responses = awaitAll(compDef, matchDef)
                val compRes = responses[0] as Response<CompetitionsResponse>
                val matchRes = responses[1] as Response<MatchResponse>

                if (compRes.isSuccessful && matchRes.isSuccessful) {
                    _competitions.value = compRes.body()?.competitions
                    _scheduled.value = matchRes.body()?.matches
                } else { _error.value = true }
            } catch (e: Exception) { _error.value = true }
            finally { _isFetching.value = false }
        }
    }
}