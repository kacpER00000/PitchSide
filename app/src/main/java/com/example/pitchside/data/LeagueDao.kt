package com.example.pitchside.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeagueDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertLeagues(leagues: List<League>)

    @Query("SELECT * FROM Ligi WHERE kod_ligi = :code LIMIT 1")
    fun getLeagueByCode(code: String): Flow<League>

    @Query("SELECT * FROM Ligi")
    fun getAllLeagues(): Flow<List<League>>

}