package com.example.pitchside.api.responses

data class MatchEntry(val competition: CompetitionResponse?, val id: Int?, val utcDate: String?, val status: String?, val matchday: Int?, val lastUpdated: String?, val homeTeam: TeamResponse, val awayTeam: TeamResponse, val score: ScoreResponse?, val referees: List<RefereeResponse>?)
