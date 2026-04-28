package com.example.pitchside.ui.scheduled

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.managers.RetrofitManager
import kotlinx.coroutines.launch

class ScheduledViewModel : ViewModel() {
    private val _scheduled = MutableLiveData<List<MatchEntry>>(emptyList())
    val scheduled = _scheduled
    private val _error = MutableLiveData(false)
    private val _isFetching = MutableLiveData(false)
    val error = _error
    val isFetching = _isFetching
    val matchesAPI = RetrofitManager.create<MatchesAPI>()
    init{
        getScheduledMatches()
    }
    fun getScheduledMatches(){
        viewModelScope.launch {
            _isFetching.value = true
            try{
                val response = matchesAPI.getScheduledMatches()
                if(response.isSuccessful){
                    _scheduled.value = response.body()?.matches
                    _error.value = false
                } else {
                    _error.value = true
                }
            } catch (e: Exception){
                _error.value = true
            } finally {
                _isFetching.value = false
            }
        }
    }
}
