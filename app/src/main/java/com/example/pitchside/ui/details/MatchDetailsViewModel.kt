package com.example.pitchside.ui.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.managers.RetrofitManager
import kotlinx.coroutines.launch

class MatchDetailsViewModel : ViewModel() {
    private val matchesAPI = RetrofitManager.create<MatchesAPI>()

    private val _match = MutableLiveData<MatchEntry?>(null)
    val match = _match

    private val _isFetching = MutableLiveData(false)
    val isFetching = _isFetching

    fun fetchMatchDetails(matchId: Int) {
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
}