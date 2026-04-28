package com.example.pitchside.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Tutaj wymieniamy wszystkie klasy encji, które stworzyliśmy
@Database(
    entities = [
        User::class,
        League::class,
        Team::class,
        LeagueTable::class,
        Match::class,
        MatchEvent::class,
        LeagueScorer::class,
        Favorite::class
    ],
    version = 1, // Zwiększamy tę liczbę, gdy zmienimy strukturę tabel
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // 2. Tutaj  dodamy abstrakcyjne metody do DAO


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 3. Singleton - zapewnia, że w całej aplikacji istnieje tylko jedna instancja bazy
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pitchside_database"
                )
                    .fallbackToDestructiveMigration() // Czyści bazę przy zmianie wersji (dobre na start projektu)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}