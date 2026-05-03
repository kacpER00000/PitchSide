package com.example.pitchside.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    // LOGOWANIE I REJESTRACJA

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun zarejestruj(user: User)

    @Query("SELECT * FROM Uzytkownicy WHERE nazwa_uzytkownika = :login AND haslo = :haslo LIMIT 1")
    suspend fun zaloguj(login: String, haslo: String): User?

    @Query("SELECT * FROM Uzytkownicy WHERE uzytkownik_id = :id")
    suspend fun pobierzUzytkownikaPoId(id: Int): User?

    //  ULUBIONE

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dodajDoUlubionych(favorite: Favorite)

    @Query("DELETE FROM Ulubione WHERE uzytkownik_id = :userId AND obiekt_id = :objId AND typ_obiektu = :type")
    suspend fun usunZUlubionych(userId: Int, objId: Int, type: String)

    @Query("SELECT * FROM Ulubione WHERE uzytkownik_id = :userId")
    suspend fun pobierzWszystkieUlubione(userId: Int): List<Favorite>

    @Query("SELECT EXISTS(SELECT 1 FROM Ulubione WHERE uzytkownik_id = :userId AND obiekt_id = :objId AND typ_obiektu = :type)")
    suspend fun czyJestUlubione(userId: Int, objId: Int, type: String): Boolean

    @Update
    suspend fun aktualizuj(user: User)
}