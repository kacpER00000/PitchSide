package com.example.pitchside.ui.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.CompetitionAPI
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.CompetitionsResponse
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.api.responses.MatchResponse
import com.example.pitchside.api.responses.StandingResponse
import com.example.pitchside.managers.RetrofitManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.SortedMap

class CompetitionDetailsViewModel : ViewModel() {
    private val _currentMatchday = MutableLiveData(1)
    private val _scheduled = MutableLiveData<List<MatchEntry>>(emptyList())
    private val _scheduledMatchesByMatchday = MutableLiveData<SortedMap<Int, List<MatchEntry>>>()
    val scheduledMatchesByMatchday = _scheduledMatchesByMatchday
    private val _standings = MutableLiveData<StandingResponse>()
    val standings = _standings
    private val _finished = MutableLiveData<List<MatchEntry>>(emptyList())
    private val _finishedMatchesByMatchday = MutableLiveData<SortedMap<Int, List<MatchEntry>>>()
    val finishedMatchesByMatchday = _finishedMatchesByMatchday
    private val _error = MutableLiveData(false)
    private val _isFetching = MutableLiveData(false)
    val error = _error
    val isFetching = _isFetching
    private val competitionCode = MutableLiveData("")
    val matchesAPI = RetrofitManager.create<MatchesAPI>()
    val competitionAPI = RetrofitManager.create<CompetitionAPI>()

    fun setCompetitionCode(code: String){competitionCode.value = code}

    fun fetchAllData(){
        viewModelScope.launch {
            _isFetching.value = true
            _error.value = false
            try{
                val standingDeferred = async {competitionAPI.getStandingForCompetition(competitionCode.value!!)}
                val scheduledDeferred = async { matchesAPI.getScheduledMatchesForCompetition(competitionCode.value!!) }
                val finishedDeferred = async {matchesAPI.getFinishedMatchesForCompetition(competitionCode.value!!)}
                val responses = awaitAll(standingDeferred,scheduledDeferred,finishedDeferred)
                val standingResponse = responses[0] as Response<StandingResponse>
                var scheduledResponse = responses[1] as Response<MatchResponse>
                val finishedResponse = responses[2] as Response<MatchResponse>
                if(standingResponse.isSuccessful && scheduledResponse.isSuccessful && finishedResponse.isSuccessful){
                    _standings.value = standingResponse.body()
                    _scheduled.value = scheduledResponse.body()?.matches
                    _finished.value = finishedResponse.body()?.matches?.reversed()
                    _currentMatchday.value = standingResponse.body()?.season?.currentMatchday
                    groupMatchesPerMatchday()
                } else {
                    _error.value = true
                }
            } catch(e: Exception){
                _error.value = true
            } finally{
                _isFetching.value = false
            }
        }
    }

    fun groupMatchesPerMatchday(){
        val allFinished = _finished.value ?: emptyList()
        val scheduled = _scheduled.value ?: emptyList()
        _finishedMatchesByMatchday.value = allFinished.groupBy { it.matchday ?: 1 }.toSortedMap(reverseOrder())
        _scheduledMatchesByMatchday.value = scheduled.groupBy { it.matchday ?: 1 }.toSortedMap()
    }
}