package com.example.bluromatic.workers

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import android.net.Uri
import androidx.work.workDataOf

import com.example.bluromatic.R

import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import kotlinx.coroutines.delay

//для використання в логуванні помилок
private const val TAG = "com.example.bluromatic.workers.BlurWorker"

//відповідає за змутнення вхідного зображення та збереження його у тимчасовому файлі
class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    @SuppressLint("LongLogTag")
    //виконує змутнення зображення, зберігає його у тимчасовому файлі
    override suspend fun doWork(): Result {

        // ADD THESE LINES
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        //викликає створення повідомлення про статус
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        return withContext(Dispatchers.IO) {
            return@withContext try {
                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage =
                        applicationContext.resources.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                    errorMessage
                }
                val resolver = applicationContext.contentResolver

                //затримує виконання поточного потоку
                delay(DELAY_TIME_MILLIS)

                //декодує ресурси зображення в Bitmap
                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
                )
                //змутнює вхідне зображення з заданою кількістю пікселів
                val output = blurBitmap(picture, blurLevel)

                //зберігає зображення в тимчасовому файлі та повертає Uri файлу
                val outputUri = writeBitmapToFile(applicationContext, output)
                val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

                Result.success(outputData)
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