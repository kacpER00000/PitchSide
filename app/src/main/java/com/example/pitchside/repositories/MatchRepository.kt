package com.example.pitchside.repositories

import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.data.Match
import com.example.pitchside.data.MatchDao
import com.example.pitchside.data.MatchDao.MatchWithTeams
import com.example.pitchside.data.Team
import com.example.pitchside.data.TeamDao
import kotlinx.coroutines.flow.Flow
import kotlin.collections.map

class MatchRepository(
    private val matchDao: MatchDao,
    private val teamDao: TeamDao,
    private val api: MatchesAPI
) {


    fun getScheduledMatches(): Flow<List<MatchWithTeams>> {
        return matchDao.getStatusMatchesWithTeams("TIMED")
    }
    fun getStatusMatchesForLeague(leagueCode: String, status: String): Flow<List<MatchWithTeams>>{
        return matchDao.getStatusMatchesWithTeamsForLeague(status,leagueCode)
    }

    suspend fun refreshData() {
        try {
            val response = api.getScheduledMatches()
            if (response.isSuccessful) {
                val matches = response.body()?.matches ?: emptyList()
                updateTeams(matches)
                updateMatches(matches)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun refreshMatchesForLeague(leagueCode: String){
        try{
            val scheduledResponse = api.getScheduledMatchesForCompetition(leagueCode)
            val finishedResponse = api.getFinishedMatchesForCompetition(leagueCode)
            val scheduledMatches = if (scheduledResponse.isSuccessful) scheduledResponse.body()?.matches ?: emptyList() else emptyList()
            val finishedMatches = if (finishedResponse.isSuccessful) finishedResponse.body()?.matches ?: emptyList() else emptyList()
            val allFetchedMatches = scheduledMatches + finishedMatches
            if (allFetchedMatches.isNotEmpty()) {
                matchDao.truncateStatusMatchesForLeague(leagueCode,"TIMED")
                matchDao.truncateStatusMatchesForLeague(leagueCode,"FINISHED")
                updateTeams(allFetchedMatches)
                updateMatches(allFetchedMatches)
            }
        } catch (e: Exception){
            throw e
        }
    }

    private suspend fun updateTeams(matches: List<MatchEntry>){
        val mappedTeams: List<Team> = matches.flatMap {
            listOf(it.homeTeam, it.awayTeam)
        }.distinctBy { it.id }.map {
            Team(it.id, it.name, it.shortName, it.tla, it.crest)
        }
        teamDao.insertTeams(mappedTeams)
    }

    private suspend fun updateMatches(matches: List<MatchEntry>){
        val mappedMatches: List<Match> = matches.map {
            Match(
                it.id, it.competition.id, it.competition.code, it.homeTeam.id, it.awayTeam.id,
                it.utcDate, it.status, it.score?.fullTime?.home,
                it.score?.fullTime?.away, it.matchday, it.stage
            )
        }
        matchDao.insertMatches(mappedMatches)
    }

}