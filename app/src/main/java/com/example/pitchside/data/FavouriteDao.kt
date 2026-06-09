package com.example.pitchside.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dodajDoUlubionych(favorite: Favorite)

    // Removes a specific object (match or league).
    @Query("DELETE FROM Ulubione WHERE uzytkownik_id = :uId AND typ_obiektu = :typ AND obiekt_id = :oId")
    suspend fun usunZUlubionych(uId: Int, typ: String, oId: Int)

    // Checks whether the object is already in favorites.
    @Query("SELECT EXISTS(SELECT 1 FROM Ulubione WHERE uzytkownik_id = :uId AND typ_obiektu = :typ AND obiekt_id = :oId)")
    suspend fun czyUlubiony(uId: Int, typ: String, oId: Int): Boolean

    // Gets all favorites for a user (used by HomeViewModel for filtering).
    @Query("SELECT * FROM Ulubione WHERE uzytkownik_id = :uId")
    fun pobierzWszystkieUlubione(uId: Int): Flow<List<Favorite>>

    // Gets only favorite matches.
    @Query("SELECT * FROM Ulubione WHERE uzytkownik_id = :uId AND typ_obiektu = 'MECZ'")
    fun pobierzUlubioneMecze(uId: Int): Flow<List<Favorite>>

    // Gets only favorite leagues.
    @Query("SELECT * FROM Ulubione WHERE uzytkownik_id = :uId AND typ_obiektu = 'LIGA'")
    fun pobierzUlubioneLigi(uId: Int): Flow<List<Favorite>>
}
