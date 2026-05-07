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

    // Usuwanie konkretnego obiektu (meczu lub ligi)
    @Query("DELETE FROM Ulubione WHERE uzytkownik_id = :uId AND typ_obiektu = :typ AND obiekt_id = :oId")
    suspend fun usunZUlubionych(uId: Int, typ: String, oId: Int)

    // Sprawdzenie czy dany obiekt już jest w ulubionych
    @Query("SELECT EXISTS(SELECT 1 FROM Ulubione WHERE uzytkownik_id = :uId AND typ_obiektu = :typ AND obiekt_id = :oId)")
    suspend fun czyUlubiony(uId: Int, typ: String, oId: Int): Boolean

    // Pobieranie wszystkich ulubionych dla danego użytkownika (używane w HomeViewModel do filtrowania)
    @Query("SELECT * FROM Ulubione WHERE uzytkownik_id = :uId")
    fun pobierzWszystkieUlubione(uId: Int): Flow<List<Favorite>>

    // Pobieranie tylko ulubionych MECZÓW
    @Query("SELECT * FROM Ulubione WHERE uzytkownik_id = :uId AND typ_obiektu = 'MECZ'")
    fun pobierzUlubioneMecze(uId: Int): Flow<List<Favorite>>

    // DODANO: Pobieranie tylko ulubionych LIG
    @Query("SELECT * FROM Ulubione WHERE uzytkownik_id = :uId AND typ_obiektu = 'LIGA'")
    fun pobierzUlubioneLigi(uId: Int): Flow<List<Favorite>>
}