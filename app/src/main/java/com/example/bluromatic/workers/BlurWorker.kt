package com.example.bluromatic.workers

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context

import com.example.bluromatic.R

import com.example.bluromatic.DELAY_TIME_MILLIS
import kotlinx.coroutines.delay

//для використання в логуванні помилок
private const val TAG = "com.example.bluromatic.workers.BlurWorker"

//відповідає за змутнення вхідного зображення та збереження його у тимчасовому файлі
class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    @SuppressLint("LongLogTag")
    //виконує змутнення зображення, зберігає його у тимчасовому файлі
    override suspend fun doWork(): Result {
        //викликає створення повідомлення про статус
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        return withContext(Dispatchers.IO) {
            return@withContext try {

                //затримує виконання поточного потоку
                delay(DELAY_TIME_MILLIS)

                //декодує ресурси зображення в Bitmap
                val picture = BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.drawable.android_cupcake
                )

                //змутнює вхідне зображення з заданою кількістю пікселів
                val output = blurBitmap(picture, 1)

                //зберігає зображення в тимчасовому файлі та повертає Uri файлу
                val outputUri = writeBitmapToFile(applicationContext, output)

                makeStatusNotification(
                    "Output is $outputUri",
                    applicationContext
                )

                Result.success()
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )

                Result.failure()
            }
        }
    }
}