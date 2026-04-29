package com.example.pitchside.api.responses

data class StandingResponse(val area: AreaResponse, val competition: CompetitionResponse?, val season: SeasonResponse?, val standings: List<Standing>)
