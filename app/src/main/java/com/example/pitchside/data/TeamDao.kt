package com.example.pitchside.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE

@Dao
interface TeamDao {
    @Insert(onConflict = IGNORE)
    suspend fun insertTeams(teams: List<Team>)
}