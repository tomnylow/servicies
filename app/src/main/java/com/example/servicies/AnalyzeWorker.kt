package com.example.servicies

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.withContext
import java.io.File

class AnalyzerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val cacheDir = applicationContext.cacheDir
            val file = File(cacheDir, "data.txt")

            if (!file.exists()) return Result.failure()

            val numbers = file.readLines().mapNotNull { it.toIntOrNull() }

            val average = if (numbers.isEmpty()) 0.0 else numbers.average()
            val message = "Среднее значение: ${"%.2f".format(average)}"

            Notifications.showNotification(
                applicationContext,
                3,
                "Анализ завершён",
                message
            )

            file.delete()

            Log.d("Worker", message)
            Result.success()
        } catch (e: Exception) {
            Log.e("Worker", "Ошибка в AnalyzerWorker", e)
            Result.failure()
        }
    }
}