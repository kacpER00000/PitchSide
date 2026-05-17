package com.example.pitchside.repositories

import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.data.Match
import com.example.pitchside.data.MatchDao
import com.example.pitchside.data.MatchDao.ScheduledMatchWithTeams
import com.example.pitchside.data.Team
import com.example.pitchside.data.TeamDao
import kotlinx.coroutines.flow.Flow

class MatchRepository(
    private val matchDao: MatchDao,
    private val teamDao: TeamDao,
    private val api: MatchesAPI
) {

    fun getMatchesByLeagueAndStatus(
        status: String,
        leagueId: Int
    ): Flow<List<Match>> {
        return matchDao.getMatchesByLeagueAndStatus(status, leagueId)
    }

    fun getScheduledMatches(): Flow<List<ScheduledMatchWithTeams>> {
        return matchDao.getScheduledMatchesWithTeams()
    }

    suspend fun refreshData() {
        try {
            matchDao.truncateMatches()
            val response = api.getScheduledMatches()
            if (response.isSuccessful) {
                val matches = response.body()?.matches ?: emptyList()
                val mappedTeams: List<Team> = matches.flatMap {
                    listOf(it.homeTeam, it.awayTeam)
                }.distinctBy { it.id }.map {
                    Team(it.id, it.name, it.shortName, it.tla, it.crest)
                }
                teamDao.insertTeams(mappedTeams)
                val mappedMatches: List<Match> = matches.map {
                    Match(
                        it.id, it.competition.id, it.homeTeam.id, it.awayTeam.id,
                        it.utcDate, it.status, it.score?.fullTime?.home,
                        it.score?.fullTime?.away, it.matchday, it.stage
                    )
                }
                matchDao.insertMatches(mappedMatches)
            }
        } catch (e: Exception) {
            throw e
        }
    }
}