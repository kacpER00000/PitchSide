package com.example.pitchside.api.responses

data class MatchResponse(val competition: CompetitionResponse?, val id: Int?, val utcDate: String?, val status: String?, val lastUpdated: String?, val homeTeam: TeamResponse, val awayTeam: TeamResponse, val score: ScoreResponse?, val referees: List<RefereeResponse>?)
