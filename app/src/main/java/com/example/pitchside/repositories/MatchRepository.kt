package com.example.pitchside.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.data.Match
import com.example.pitchside.data.MatchDao
import com.example.pitchside.data.MatchDao.MatchWithTeams
import com.example.pitchside.data.Resource
import com.example.pitchside.data.Team
import com.example.pitchside.data.TeamDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate
import kotlin.collections.map

class MatchRepository(
    private val matchDao: MatchDao,
    private val teamDao: TeamDao,
    private val api: MatchesAPI
) {

    suspend fun isScheduledMatchesEmpty(): Boolean{
        return matchDao.getScheduledMatchCount() == 0
    }
    fun getScheduledMatches(): Flow<Resource<List<MatchWithTeams>>> =
        matchDao.getScheduledMatchesWithTeams().asResource()
    fun getStatusMatchesForLeague(leagueCode: String, status: String): Flow<Resource<List<MatchWithTeams>>> =
        matchDao.getStatusMatchesWithTeamsForLeague(status,leagueCode).asResource()

    fun getMatchByMatchId(matchId: Int): Flow<Resource<MatchWithTeams>> =
        matchDao.getMatchByMatchId(matchId).asResource()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshData() {
        try {
            val dateFrom = LocalDate.now().toString()
            val dateTo = LocalDate.now().plusDays(7).toString()
            val response = api.getScheduledMatches(dateFrom,dateTo)
            if (response.isSuccessful) {
                val matches = response.body()?.matches ?: emptyList()
                this.matchDao.clearOnlyScheduledMatches()
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
                it.score?.fullTime?.away, it.matchday, it.stage, it.referees?.firstOrNull()?.name
            )
        }
        matchDao.insertMatches(mappedMatches)
    }

    private fun <T> Flow<T>.asResource(): Flow<Resource<T>> =
        map<T, Resource<T>> { Resource.Success(it) }
            .onStart { emit(Resource.Loading) }
            .catch { e ->
                emit(Resource.Error(message = e.localizedMessage ?: "An unknown error occurred", exception = Exception(e)))
            }

}
