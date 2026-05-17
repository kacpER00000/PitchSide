package com.example.pitchside.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pitchside.api.dao.MatchesAPI
import com.example.pitchside.data.AppDatabase
import com.example.pitchside.managers.RetrofitManager
import com.example.pitchside.repositories.MatchRepository
import kotlinx.coroutines.delay

class DatabaseSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val api = RetrofitManager.create<MatchesAPI>()
            val db = AppDatabase.getDatabase(applicationContext)
            val matchRepository = MatchRepository(db.matchDao(), db.teamDao(), api)
            matchRepository.refreshData()
            Result.success()
        } catch (e: Exception) {
            delay(60_000)
            Result.retry()
        }
    }
}