package com.example.pitchside.api.dao

import com.example.pitchside.api.responses.CompetitionsResponse
import com.example.pitchside.api.responses.ScorersResponse
import com.example.pitchside.api.responses.StandingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CompetitionAPI {
    @GET("competitions")
    suspend fun getAllCompetitions(): Response<CompetitionsResponse>
    @GET("competitions/{code}/standings")
    suspend fun getStandingForCompetition(@Path("code") code: String): Response<StandingResponse>
    @GET("competitions/{code}/scorers")
    suspend fun getCompetitionTopScorers(@Path("code") code: String): Response<ScorersResponse>
}