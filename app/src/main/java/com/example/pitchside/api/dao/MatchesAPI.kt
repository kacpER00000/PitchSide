package com.example.pitchside.api.dao

import com.example.pitchside.api.responses.ScheduledResponse
import retrofit2.Response
import retrofit2.http.GET

interface MatchesAPI {
    @GET("matches?status=SCHEDULED")
    suspend fun getScheduledMatches(): Response<ScheduledResponse>
}