package com.example.pitchside.api.responses

data class Table(val position: Int, val team: TeamResponse, val playedGames: Int, val won: Int, val draw: Int, val lost: Int, val points: Int, val goalsFor: Int, val goalsAgainst: Int, val goalDifference: Int)
