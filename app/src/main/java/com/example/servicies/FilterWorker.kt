package com.example.servicies

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import java.io.File

class FilterWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val file = File(applicationContext.cacheDir, "data.txt")

            if (!file.exists()) return Result.failure()

            val evens = file.readLines().mapNotNull { it.toIntOrNull() }.filter { it % 2 == 0 }
            file.writeText(evens.joinToString("\n"))
            delay(3000)

            Notifications.showNotification(
                applicationContext,
                2,
                "Фильтрация завершена",
                "Осталось ${evens.size} чётных чисел"
            )
            Result.success()
        } catch (e: Exception) {
            Log.e("Worker", "Ошибка в FilterWorker", e)
            Result.failure()
        }
    }
}