package com.example.pitchside.repositories

import com.example.pitchside.api.dao.CompetitionAPI
import com.example.pitchside.data.League
import com.example.pitchside.data.LeagueDao
import com.example.pitchside.data.LeagueScorer
import com.example.pitchside.data.LeagueScorerDao
import com.example.pitchside.data.LeagueTable
import com.example.pitchside.data.LeagueTableDao
import kotlinx.coroutines.flow.Flow
import kotlin.collections.flatMap

class LeagueRepository(private val leagueApi: CompetitionAPI, private val leagueDao: LeagueDao, private val leagueTableDao: LeagueTableDao, private val leagueScorerDao: LeagueScorerDao) {
    fun getAllLeagues(): Flow<List<League>> {
        return leagueDao.getAllLeagues()
    }

    fun getStandingForLeague(leagueCode: String): Flow<List<LeagueTableDao.LeagueTableWithTeam>>{
        return leagueTableDao.getStandingForLeagueWithTeams(leagueCode)
    }

    fun getLeagueScorersForLeague(leagueCode: String): Flow<List<LeagueScorerDao.LeagueScorerWithTeam>>{
        return leagueScorerDao.getTopLeagueScorersWithTeam(leagueCode)
    }

    fun getLeagueByLeagueCode(leagueCode: String): Flow<League> {
        return leagueDao.getLeagueByCode(leagueCode)
    }

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
}