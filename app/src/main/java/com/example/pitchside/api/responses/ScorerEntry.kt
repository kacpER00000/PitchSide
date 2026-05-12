package com.example.pitchside.api.responses

data class ScorerEntry(val player: PlayerEntry, val team: TeamResponse, val goals: Int, val assists: Int) {
}