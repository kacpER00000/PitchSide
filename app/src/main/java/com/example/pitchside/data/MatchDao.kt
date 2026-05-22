package com.example.pitchside.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao{
    data class MatchWithTeams(
        val matchId: Int,
        val startDate: String?,
        val stage: String?,
        val matchday: Int,
        val homeTeamName: String?,
        val homeTeamCrest: String?,
        val awayTeamName: String?,
        val awayTeamCrest: String?,
        val homeTeamScore: Int?,
        val awayTeamScore: Int?,
        val referee: String?,
        val leagueName: String?,
        val status: String
    )
    @Upsert
    suspend fun insertMatches(matches: List<Match>)

    @Query("DELETE FROM Mecze WHERE status IN ('SCHEDULED', 'TIMED')")
    suspend fun clearOnlyScheduledMatches()

    @Query("SELECT COUNT(*) FROM Mecze")
    suspend fun getMatchCount(): Int


    @Query("""
    SELECT 
        m.mecz_id AS matchId, 
        m.data_meczu AS startDate,
        m.faza as stage,
        m.kolejka as matchday,
        COALESCE(gospodarz.skrocona_nazwa, gospodarz.pelna_nazwa) AS homeTeamName,
        gospodarz.logo AS homeTeamCrest,
        COALESCE(gosc.skrocona_nazwa, gosc.pelna_nazwa) AS awayTeamName,
        gosc.logo AS awayTeamCrest,
        m.wynik_gospodarz AS homeTeamScore,
        m.wynik_gosc AS awayTeamScore,
        m.sedzia as referee,
        l.nazwa_ligi as leagueName,
        m.status as status
        
    FROM Mecze m
    INNER JOIN Druzyny gospodarz ON m.id_gospodarza = gospodarz.druzyna_id
    INNER JOIN Druzyny gosc ON m.id_goscia = gosc.druzyna_id
    INNER JOIN Ligi l ON m.liga_id = l.liga_id
    WHERE m.status = :status AND m.kod_ligi = :leagueCode AND m.id_gospodarza != 0 AND m.id_goscia != 0
    ORDER BY m.data_meczu ASC
""")
    fun getStatusMatchesWithTeamsForLeague(status: String, leagueCode: String): Flow<List<MatchWithTeams>>

    @Query("""
    SELECT 
        m.mecz_id AS matchId, 
        m.data_meczu AS startDate,
        m.faza as stage,
        m.kolejka as matchday,
        COALESCE(gospodarz.skrocona_nazwa, gospodarz.pelna_nazwa) AS homeTeamName,
        gospodarz.logo AS homeTeamCrest,
        COALESCE(gosc.skrocona_nazwa, gosc.pelna_nazwa) AS awayTeamName,
        gosc.logo AS awayTeamCrest,
        m.wynik_gospodarz AS homeTeamScore,
        m.wynik_gosc AS awayTeamScore,
        m.sedzia as referee,
        l.nazwa_ligi as leagueName,
        m.status as status
        
    FROM Mecze m
    INNER JOIN Druzyny gospodarz ON m.id_gospodarza = gospodarz.druzyna_id
    INNER JOIN Druzyny gosc ON m.id_goscia = gosc.druzyna_id
    INNER JOIN Ligi l ON m.liga_id = l.liga_id
    WHERE m.status = :status AND m.id_gospodarza != 0 AND m.id_goscia != 0
    ORDER BY m.data_meczu ASC
""")
    fun getStatusMatchesWithTeams(status: String): Flow<List<MatchWithTeams>>


    @Query("""
    SELECT 
        m.mecz_id AS matchId, 
        m.data_meczu AS startDate,
        m.faza as stage,
        m.kolejka as matchday,
        COALESCE(gospodarz.skrocona_nazwa, gospodarz.pelna_nazwa) AS homeTeamName,
        gospodarz.logo AS homeTeamCrest,
        COALESCE(gosc.skrocona_nazwa, gosc.pelna_nazwa) AS awayTeamName,
        gosc.logo AS awayTeamCrest,
        m.wynik_gospodarz AS homeTeamScore,
        m.wynik_gosc AS awayTeamScore,
        m.sedzia as referee,
        l.nazwa_ligi as leagueName,
        m.status as status
    FROM Mecze m
    INNER JOIN Druzyny gospodarz ON m.id_gospodarza = gospodarz.druzyna_id
    INNER JOIN Druzyny gosc ON m.id_goscia = gosc.druzyna_id
    INNER JOIN Ligi l ON m.liga_id = l.liga_id
    WHERE m.mecz_id = :matchId
""")
    fun getMatchByMatchId(matchId: Int): Flow<MatchWithTeams>

    @Query("DELETE FROM Mecze WHERE kod_ligi = :leagueCode AND status = :status")
    suspend fun truncateStatusMatchesForLeague(leagueCode: String, status: String)
}
