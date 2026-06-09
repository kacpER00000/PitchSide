package com.example.pitchside.api.responses

data class ScoreResponse(val winner: String?,val duration: String, val fullTime: HalfScore, val halfTime: HalfScore)
