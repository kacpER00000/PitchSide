package com.example.pitchside.api.dao

import com.example.pitchside.api.responses.CompetitionResponse
import com.example.pitchside.api.responses.CompetitionsResponse
import retrofit2.Response
import retrofit2.http.GET

interface CompetitionAPI {
    @GET("competitions")
    suspend fun getAllCompetitions(): Response<CompetitionsResponse>
}