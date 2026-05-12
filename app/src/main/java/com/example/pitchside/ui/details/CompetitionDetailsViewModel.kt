package com.example.pitchside.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
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
import com.example.pitchside.managers.RetrofitManager
import com.example.pitchside.managers.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.SortedMap

class CompetitionDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()

    private val _currentMatchday = MutableLiveData(1)
    private val _scheduled = MutableLiveData<List<MatchEntry>>(emptyList())
    private val _scheduledMatchesByMatchday = MutableLiveData<SortedMap<Int, List<MatchEntry>>>()
    val scheduledMatchesByMatchday = _scheduledMatchesByMatchday

    private val _standings = MutableLiveData<StandingResponse>()
    val standings = _standings

    private val _finished = MutableLiveData<List<MatchEntry>>(emptyList())
    private val _finishedMatchesByMatchday = MutableLiveData<SortedMap<Int, List<MatchEntry>>>()
    val finishedMatchesByMatchday = _finishedMatchesByMatchday

    private val _scorers = MutableLiveData<List<ScorerEntry>>(emptyList())
    val scorers = _scorers

    private val _error = MutableLiveData(false)
    val error = _error

    private val _isFetching = MutableLiveData(false)
    val isFetching = _isFetching
    private val _isFavorite = MutableLiveData(false)
    val isFavorite = _isFavorite

    private val competitionCode = MutableLiveData("")
    val matchesAPI = RetrofitManager.create<MatchesAPI>()
    val competitionAPI = RetrofitManager.create<CompetitionAPI>()

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
        val currentStanding = _standings.value ?: return

        viewModelScope.launch {
            val isFav = _isFavorite.value ?: false
            if (isFav) {
                val leagueId = currentStanding.competition?.id ?: return@launch
                favoriteDao.usunZUlubionych(user.uzytkownik_id, "LIGA", leagueId)
            } else {
                val newFav = Favorite(
                    uzytkownik_id = user.uzytkownik_id,
                    typ_obiektu = "LIGA",
                    obiekt_id = currentStanding.competition?.id ?: 0,
                    nazwa_ligi = currentStanding.competition?.name,
                    emblem_ligi = currentStanding.competition?.emblem,
                    kod_ligi = code
                )
                favoriteDao.dodajDoUlubionych(newFav)
            }
        }
    }

    fun fetchAllData() {
        if (competitionCode.value.isNullOrBlank()) return

        viewModelScope.launch {
            _isFetching.value = true
            _error.value = false
            try {
                val standingDeferred = async { competitionAPI.getStandingForCompetition(competitionCode.value!!) }
                val scheduledDeferred = async { matchesAPI.getScheduledMatchesForCompetition(competitionCode.value!!) }
                val finishedDeferred = async { matchesAPI.getFinishedMatchesForCompetition(competitionCode.value!!) }
                val scorersDeferred = async {competitionAPI.getCompetitionTopScorers(competitionCode.value!!)}

                val responses = awaitAll(standingDeferred, scheduledDeferred, finishedDeferred,scorersDeferred)

                val standingResponse = responses[0] as Response<StandingResponse>
                val scheduledResponse = responses[1] as Response<MatchResponse>
                val finishedResponse = responses[2] as Response<MatchResponse>
                val scorersResponse = responses[3] as Response<ScorersResponse>

                if (standingResponse.isSuccessful && scheduledResponse.isSuccessful && finishedResponse.isSuccessful && scorersResponse.isSuccessful) {
                    _standings.value = standingResponse.body()
                    _scheduled.value = scheduledResponse.body()?.matches
                    _finished.value = finishedResponse.body()?.matches?.reversed()
                    _currentMatchday.value = standingResponse.body()?.season?.currentMatchday
                    groupMatchesPerMatchday()
                    _scorers.value = scorersResponse.body()?.scorers
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

    fun groupMatchesPerMatchday() {
        val allFinished = _finished.value ?: emptyList()
        val scheduled = _scheduled.value ?: emptyList()
        _finishedMatchesByMatchday.value = allFinished.groupBy { getStageOrder(it) }.toSortedMap(reverseOrder())
        _scheduledMatchesByMatchday.value = scheduled.groupBy { getStageOrder(it) }.toSortedMap()
    }

    private fun getStageOrder(match: MatchEntry): Int {
        return when (match.stage) {
            "FINAL" -> 100
            "SEMI_FINALS" -> 99
            "QUARTER_FINALS" -> 98
            else -> match.matchday ?: 1
        }
    }
}