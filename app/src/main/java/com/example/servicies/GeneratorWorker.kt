package com.example.servicies
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random


class GeneratorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val file = File(applicationContext.cacheDir, "data.txt")

            val numbers = List(10) { Random.nextInt(1, 101) }
            file.writeText(numbers.joinToString("\n"))

            delay(2000)

            Notifications.showNotification(
                applicationContext,
                1,
                "Генерация завершена",
                "Создано 10 случайных чисел"
            )
            Result.success()
        } catch (e: Exception) {
            Log.e("Worker", "Ошибка в GeneratorWorker", e)
            Result.failure()
        }
    }
}