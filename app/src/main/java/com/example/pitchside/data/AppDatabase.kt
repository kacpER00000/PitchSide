package com.example.pitchside.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class, League::class, Team::class, LeagueTable::class,
        Match::class, MatchEvent::class, LeagueScorer::class, Favorite::class
    ],
    version = 9
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun leagueDao(): LeagueDao
    abstract fun matchDao(): MatchDao
    abstract fun teamDao(): TeamDao
    abstract fun leagueTableDao(): LeagueTableDao
    abstract fun leagueScorerDao(): LeagueScorerDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "pitchside_database"
                    )
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                prepopulateDatabase(context)
                            }
                        })
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        private fun prepopulateDatabase(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).leagueDao()
                val leaguesToInsert = listOf(
                    League(2013, "Campeonato Brasileiro Série A", "BSA", "https://crests.football-data.org/bsa.png", "Brazil", "https://crests.football-data.org/764.svg"),
                    League(2016, "Championship", "ELC", "https://crests.football-data.org/ELC.png", "England", "https://crests.football-data.org/770.svg"),
                    League(2021, "Premier League", "PL", "https://crests.football-data.org/PL.png", "England", "https://crests.football-data.org/770.svg"),
                    League(2001, "UEFA Champions League", "CL", "https://crests.football-data.org/CL.png", "Europe", "https://crests.football-data.org/EUR.svg"),
                    League(2018, "European Championship", "EC", "https://crests.football-data.org/ec.png", "Europe", "https://crests.football-data.org/EUR.svg"),
                    League(2015, "Ligue 1", "FL1", "https://crests.football-data.org/FL1.png", "France", "https://crests.football-data.org/773.svg"),
                    League(2002, "Bundesliga", "BL1", "https://crests.football-data.org/BL1.png", "Germany", "https://crests.football-data.org/759.svg"),
                    League(2019, "Serie A", "SA", "https://crests.football-data.org/c111.png", "Italy", "https://crests.football-data.org/784.svg"),
                    League(2003, "Eredivisie", "DED", "https://crests.football-data.org/ED.png", "Netherlands", "https://crests.football-data.org/8601.svg"),
                    League(2017, "Primeira Liga", "PPL", "https://crests.football-data.org/PPL.png", "Portugal", "https://crests.football-data.org/765.svg"),
                    League(2152, "Copa Libertadores", "CLI", "https://crests.football-data.org/CLI.svg", "South America", "https://crests.football-data.org/CLI.svg"),
                    League(2014, "Primera Division", "PD", "https://crests.football-data.org/laliga.png", "Spain", "https://crests.football-data.org/760.svg"),
                    League(2000, "FIFA World Cup", "WC", "https://crests.football-data.org/wm26.png", "World", null)
                )
                dao.insertLeagues(leaguesToInsert)
            }
        }
    }
}