package com.example.notificationapp.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(val context: Context, val workerParams: WorkerParameters) : Worker(context,
    workerParams) {
    override fun doWork(): Result {

        NotificationHelper(context).createNotification(
            inputData.getString("title").toString()!!,
            inputData.getString("text").toString()!!,
            inputData.getInt("id",1)
        )

        return Result.success()
    }

}