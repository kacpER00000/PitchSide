package com.example.pitchside.repositories

import com.example.pitchside.api.dao.CompetitionAPI
import com.example.pitchside.data.League
import com.example.pitchside.data.LeagueDao
import com.example.pitchside.data.LeagueScorer
import com.example.pitchside.data.LeagueScorerDao
import com.example.pitchside.data.LeagueTable
import com.example.pitchside.data.LeagueTableDao
import com.example.pitchside.data.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.collections.flatMap

class LeagueRepository(private val leagueApi: CompetitionAPI, private val leagueDao: LeagueDao, private val leagueTableDao: LeagueTableDao, private val leagueScorerDao: LeagueScorerDao) {
    fun getAllLeagues(): Flow<Resource<List<League>>> =
        leagueDao.getAllLeagues().asResource()

    fun getStandingForLeague(leagueCode: String): Flow<Resource<List<LeagueTableDao.LeagueTableWithTeam>>> =
        leagueTableDao.getStandingForLeagueWithTeams(leagueCode).asResource()

    fun getLeagueScorersForLeague(leagueCode: String): Flow<Resource<List<LeagueScorerDao.LeagueScorerWithTeam>>> =
        leagueScorerDao.getTopLeagueScorersWithTeam(leagueCode).asResource()

    fun getLeagueByLeagueCode(leagueCode: String): Flow<Resource<League>> =
        leagueDao.getLeagueByCode(leagueCode).asResource()

    suspend fun refreshDataForLeague(leagueCode: String){
        try {
            val standingResponse = leagueApi.getStandingForCompetition(leagueCode)
            var leagueId = 0
            if(standingResponse.isSuccessful){
                val body = standingResponse.body()

                leagueId = body?.competition?.id ?: return
                val tLeagueCode = body.competition.code ?: leagueCode

                val mappedStandings: List<LeagueTable> = body.standings.flatMap { standing ->
                    val group= standing.group
                    standing.table.map { row ->
                        LeagueTable(
                            liga_id = leagueId,
                            kod_ligi = tLeagueCode,
                            druzyna_id = row.team.id,
                            grupa = group,
                            pozycja = row.position,
                            mecze_rozegrane = row.playedGames,
                            wygrane = row.won,
                            remisy = row.draw,
                            porazki = row.lost,
                            punkty = row.points,
                            bramki_zdobyte = row.goalsFor,
                            bramki_stracone = row.goalsAgainst
                        )
                    }
                }
                leagueTableDao.deleteStandingDataForLeague(leagueCode)
                leagueTableDao.insertStanding(mappedStandings)
            }
            val scorersResponse = leagueApi.getCompetitionTopScorers(leagueCode)
            if(scorersResponse.isSuccessful){
                val body = scorersResponse.body()?.scorers
                val mappedLeagueScorers = body?.map { scorerEntry ->
                    LeagueScorer(
                        scorerEntry.player.id,
                        leagueId,
                        leagueCode,
                        scorerEntry.team.id,
                        scorerEntry.player.name,
                        scorerEntry.goals,
                        scorerEntry.assists
                    )
                } ?: emptyList()
                leagueScorerDao.deleteLeagueScorerForLeague(leagueCode)
                leagueScorerDao.insertLeagueScorers(mappedLeagueScorers)
            }
        } catch (e: Exception){
            throw e
        }
    }

    private fun <T> Flow<T>.asResource(): Flow<Resource<T>> =
        map<T, Resource<T>> { Resource.Success(it) }
            .onStart { emit(Resource.Loading) }
            .catch { e ->
                emit(Resource.Error(message = e.localizedMessage ?: "Wystapil nieznany blad", exception = Exception(e)))
            }
}
