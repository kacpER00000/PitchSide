package com.example.pitchside.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Tutaj wymieniamy wszystkie klasy encji, które stworzyliśmy
@Database(
    entities = [
        User::class, League::class, Team::class, LeagueTable::class,
        Match::class, MatchEvent::class, LeagueScorer::class, Favorite::class
    ],
    version = 2 // Zmiana numeru przy kazdej zmianie bazy danych
)
abstract class AppDatabase : RoomDatabase() {
    // Twoje abstrakcyjne metody do DAO (np. abstract fun userDao(): UserDao)
    abstract fun userDao(): UserDao // To pozwala nam używać metod z UserDao

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