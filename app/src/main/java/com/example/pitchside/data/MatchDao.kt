package com.example.pitchside.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao{
    data class ScheduledMatchWithTeams(
        val matchId: Int,
        val startDate: String?,
        val homeTeamName: String?,
        val homeTeamCrest: String?,
        val awayTeamName: String?,
        val awayTeamCrest: String?
    )
    @Upsert
    suspend fun insertMatches(matches: List<Match>)

    @Query("SELECT * FROM Mecze WHERE status = :status AND liga_id = :leagueId")
    fun getMatchesByLeagueAndStatus(status: String,leagueId: Int): Flow<List<Match>>

    @Query("""
    SELECT 
        m.mecz_id AS matchId, 
        m.data_meczu AS startDate,
        COALESCE(gospodarz.skrocona_nazwa, gospodarz.pelna_nazwa) AS homeTeamName,
        gospodarz.logo AS homeTeamCrest,
        COALESCE(gosc.skrocona_nazwa, gosc.pelna_nazwa) AS awayTeamName,
        gosc.logo AS awayTeamCrest
        
    FROM Mecze m
    INNER JOIN Druzyny gospodarz ON m.id_gospodarza = gospodarz.druzyna_id
    INNER JOIN Druzyny gosc ON m.id_goscia = gosc.druzyna_id
    WHERE m.status IN ('TIMED')
    ORDER BY m.data_meczu ASC
""")
    fun getScheduledMatchesWithTeams(): Flow<List<ScheduledMatchWithTeams>>

    @Query("DELETE FROM Mecze")
    suspend fun truncateMatches()
}
