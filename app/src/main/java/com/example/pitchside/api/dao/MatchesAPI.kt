package com.example.pitchside.api.dao

import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.api.responses.MatchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MatchesAPI {
    @GET("matches?status=SCHEDULED")
    suspend fun getScheduledMatches(): Response<MatchResponse>

    @GET("competitions/{code}/matches?status=SCHEDULED")
    suspend fun getScheduledMatchesForCompetition(@Path("code") code: String): Response<MatchResponse>

    @GET("competitions/{code}/matches?status=FINISHED")
    suspend fun getFinishedMatchesForCompetition(@Path("code") code: String): Response<MatchResponse>

    @GET("matches/{id}")
    suspend fun getMatchById(
        @Path("id") matchId: Int
    ): Response<MatchEntry>
}