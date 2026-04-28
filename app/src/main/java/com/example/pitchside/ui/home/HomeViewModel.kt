package com.example.pitchside.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.CompetitionAPI
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.CompetitionResponse
import com.example.pitchside.api.responses.CompetitionsResponse
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.api.responses.MatchResponse
import com.example.pitchside.managers.RetrofitManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.Response

class HomeViewModel : ViewModel() {
    private val competitionAPI = RetrofitManager.create<CompetitionAPI>()
    private val matchesAPI = RetrofitManager.create<MatchesAPI>()
    private val _competitions = MutableLiveData<List<CompetitionResponse>>(emptyList())
    val competitions = _competitions
    private val _scheduled = MutableLiveData<List<MatchEntry>>(emptyList())
    val scheduled = _scheduled
    private val _error = MutableLiveData(false)
    val error = _error
    private val _isFetching = MutableLiveData(false)
    val isFetching = _isFetching
    init{
        fetchAllData()
    }
    fun fetchAllData(){
        viewModelScope.launch {
            _isFetching.value = true
            _error.value = false
            try{
                val competitionDeferred = async { competitionAPI.getAllCompetitions() }
                val scheduledDeferred = async { matchesAPI.getScheduledMatches() }
                val responses = awaitAll(competitionDeferred,scheduledDeferred)
                val competitionResponse = responses[0] as Response<CompetitionsResponse>
                val matchResponse = responses[1] as Response<MatchResponse>
                if(competitionResponse.isSuccessful && matchResponse.isSuccessful){
                    _competitions.value = competitionResponse.body()?.competitions
                    _scheduled.value = matchResponse.body()?.matches
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
}