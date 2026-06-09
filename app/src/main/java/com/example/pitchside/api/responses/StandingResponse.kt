package com.example.pitchside.api.responses

data class StandingResponse(val area: AreaResponse? = null, val competition: CompetitionResponse? = null, val season: SeasonResponse? = null, val standings: List<Standing> = emptyList())
