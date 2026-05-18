package com.example.pitchside.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeagueTableDao {
    data class LeagueTableWithTeam(
        val position: Int,
        val group: String?,
        val playedMatches: Int,
        val won: Int,
        val drew: Int,
        val lost: Int,
        val points: Int,
        val goalsFor: Int,
        val goalsAgainst: Int,
        val teamId: Int,
        val teamName: String?,
        val teamEmblem: String?
    )
    @Insert(onConflict = REPLACE)
    suspend fun insertStanding(standing: List<LeagueTable>)

    @Query("""
    SELECT 
        t.pozycja as position, 
        t.grupa as "group",
        t.mecze_rozegrane as playedMatches, 
        t.wygrane as won, 
        t.remisy as drew, 
        t.porazki as lost, 
        t.punkty as points, 
        t.bramki_zdobyte as goalsFor, 
        t.bramki_stracone as goalsAgainst,
        d.druzyna_id as teamId,
        d.skrocona_nazwa as teamName,
        d.logo as teamEmblem
    FROM Tabela_Ligowa t
    INNER JOIN Druzyny d ON t.druzyna_id = d.druzyna_id
    WHERE t.kod_ligi = :leagueCode
    ORDER BY t.grupa ASC, t.pozycja ASC
""")
    fun getStandingForLeagueWithTeams(leagueCode: String): Flow<List<LeagueTableWithTeam>>

    @Query("DELETE FROM Tabela_Ligowa WHERE kod_ligi = :leagueCode")
    fun deleteStandingDataForLeague(leagueCode: String)

}