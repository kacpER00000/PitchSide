package com.example.pitchside.repositories

import com.example.pitchside.api.dao.CompetitionAPI
import com.example.pitchside.data.League
import com.example.pitchside.data.LeagueDao
import kotlinx.coroutines.flow.Flow

class LeagueRepository(private val api: CompetitionAPI, private val dao: LeagueDao) {
    fun getAllLeagues(): Flow<List<League>> {
        return dao.getAllLeagues()
    }
}