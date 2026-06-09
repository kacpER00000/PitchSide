package com.example.pitchside.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Uzytkownicy")
data class User(
    @PrimaryKey(autoGenerate = true) val uzytkownik_id: Int = 0,
    val nazwa_uzytkownika: String,
    val email: String,
    val haslo: String
)

@Entity(tableName = "Ligi")
data class League(
    @PrimaryKey val liga_id: Int,
    val nazwa_ligi: String,
    val kod_ligi: String?,
    val emblemat_ligi: String?,
    val kraj: String?,
    val flaga_kraju: String?,

)

@Entity(tableName = "Druzyny")
data class Team(
    @PrimaryKey val druzyna_id: Int,
    val pelna_nazwa: String?,
    val skrocona_nazwa: String?,
    val kod_druzyny: String?,
    val logo: String?
)

@Entity(
    tableName = "Tabela_Ligowa",
    primaryKeys = ["liga_id", "druzyna_id"],
    foreignKeys = [
        ForeignKey(entity = League::class, parentColumns = ["liga_id"], childColumns = ["liga_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Team::class, parentColumns = ["druzyna_id"], childColumns = ["druzyna_id"], onDelete = ForeignKey.CASCADE)
    ]
)
data class LeagueTable(
    val liga_id: Int,
    val kod_ligi: String,
    val druzyna_id: Int,
    val grupa: String?,
    val pozycja: Int,
    val mecze_rozegrane: Int = 0,
    val wygrane: Int = 0,
    val remisy: Int = 0,
    val porazki: Int = 0,
    val punkty: Int = 0,
    val bramki_zdobyte: Int = 0,
    val bramki_stracone: Int = 0
)

@Entity(
    tableName = "Mecze",
    foreignKeys = [
        ForeignKey(entity = League::class, parentColumns = ["liga_id"], childColumns = ["liga_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Team::class, parentColumns = ["druzyna_id"], childColumns = ["id_gospodarza"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Team::class, parentColumns = ["druzyna_id"], childColumns = ["id_goscia"], onDelete = ForeignKey.CASCADE)
    ]
)
data class Match(
    @PrimaryKey val mecz_id: Int,
    val liga_id: Int,
    val kod_ligi: String?,
    val id_gospodarza: Int,
    val id_goscia: Int,
    val data_meczu: String?,
    val status: String,
    val wynik_gospodarz: Int?,
    val wynik_gosc: Int?,
    val kolejka: Int?,
    val faza: String?,
    val sedzia: String?
)

@Entity(
    tableName = "Wydarzenia_Meczowe",
    foreignKeys = [
        ForeignKey(entity = Match::class, parentColumns = ["mecz_id"], childColumns = ["mecz_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Team::class, parentColumns = ["druzyna_id"], childColumns = ["druzyna_id"])
    ]
)
data class MatchEvent(
    @PrimaryKey(autoGenerate = true) val wydarzenie_id: Int = 0,
    val mecz_id: Int,
    val typ_wydarzenia: String,
    val minuta: Int,
    val zawodnik_glowny: String,
    val zawodnik_asystujacy: String?,
    val druzyna_id: Int
)

@Entity(
    tableName = "Strzelcy_Ligi",
    indices = [Index(value = ["liga_id", "nazwisko_zawodnika"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = League::class, parentColumns = ["liga_id"], childColumns = ["liga_id"]),
        ForeignKey(entity = Team::class, parentColumns = ["druzyna_id"], childColumns = ["druzyna_id"])
    ]
)
data class LeagueScorer(
    @PrimaryKey(autoGenerate = true) val strzelec_id: Int = 0,
    val liga_id: Int,
    val kod_ligi: String?,
    val druzyna_id: Int,
    val nazwisko_zawodnika: String,
    val liczba_goli: Int = 0,
    val liczba_asyst: Int = 0
)

@Entity(
    tableName = "Ulubione",
    indices = [Index(value = ["uzytkownik_id", "typ_obiektu", "obiekt_id"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uzytkownik_id"],
            childColumns = ["uzytkownik_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Favorite(
    @PrimaryKey(autoGenerate = true) val ulubione_id: Int = 0,
    val uzytkownik_id: Int,
    val typ_obiektu: String,
    val obiekt_id: Int,
    val nazwa_gospodarza: String? = null,
    val skrot_gospodarza: String? = null,
    val herb_gospodarza: String? = null,
    val nazwa_goscia: String? = null,
    val skrot_goscia: String? = null,
    val herb_goscia: String? = null,
    val nazwa_ligi: String? = null,
    val emblem_ligi: String? = null,
    val kod_ligi: String? = null
)