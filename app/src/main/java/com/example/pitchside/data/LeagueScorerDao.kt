package com.example.pitchside.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeagueScorerDao {
    data class LeagueScorerWithTeam(
        val playerId: Int,
        val playerName: String,
        val goals: Int,
        val assists: Int,
        val teamId: Int,
        val teamName: String?,
        val teamEmblem: String?
    )
    @Insert(onConflict = REPLACE)
    suspend fun insertLeagueScorers(leagueScorers: List<LeagueScorer>)

    @Query("""
    SELECT 
        s.strzelec_id as playerId, 
        s.nazwisko_zawodnika as playerName, 
        s.liczba_goli as goals, 
        s.liczba_asyst as assists,
        d.druzyna_id as teamId,
        d.skrocona_nazwa as teamName,
        d.logo as teamEmblem
        
    FROM Strzelcy_Ligi s
    INNER JOIN Druzyny d ON s.druzyna_id = d.druzyna_id
    WHERE s.kod_ligi = :leagueCode
    ORDER BY s.liczba_goli DESC
""")
    fun getTopLeagueScorersWithTeam(leagueCode: String): Flow<List<LeagueScorerWithTeam>>

    @Query("DELETE FROM strzelcy_ligi WHERE kod_ligi = :leagueCode")
    suspend fun deleteLeagueScorerForLeague(leagueCode: String)

}