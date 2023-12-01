package com.lge.devicecare.workmanager

import android.annotation.TargetApi
import android.app.Notification
import android.app.Notification.Builder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lge.devicecare.R
import com.lge.devicecare.constants.Constants
import com.lge.devicecare.service.BackgroundServiceManager
import com.lge.devicecare.service.MQTTManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Suppress("UNREACHABLE_CODE")
class WorkmanagerForService(val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    private val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        .setSmallIcon(R.drawable.img_devicecare_icon)
        .setContentTitle("DeviceCare-WorkTask in progress")

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {


        // createForegroundInfo("Service Running")
        //createNotificationChannel()
        //startNotification()

        GlobalScope.launch(Dispatchers.Default) {
            createNotificationChannel()
            val notification = notificationBuilder.build()
            val foregroundInfo =
                ForegroundInfo(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
            setForeground(foregroundInfo)
            showProgress(Constants.SPOW_PROGRESS)
        }



        try {


            //  setProgress(workDataOf())
            // update the notification progress
            //  showProgress(0)
            /*for (i in 0..100) {
                // we need it to get progress in UI
                setProgress(workDataOf(ARG_PROGRESS to i))
                // update the notification progress
                showProgress(i)
                delay(DELAY_DURATION)
            }*/

            /*CoroutineScope(Dispatchers.IO).launch {
                Toast.makeText(context, "Workmanaer call", Toast.LENGTH_SHORT).show()

            }*/

            CoroutineScope(Dispatchers.IO).launch {
                MQTTManager.init(applicationContext)

            }
            return Result.success()
        } catch (throwable: Throwable) {
            return Result.failure()
        }


        /*  Log.d("DTAG", "scheduled: " + Constants.isWorkScheduled(Constants.TAG_WORKMANAGER));
          Log.i("Work Status->", "workmanager starteddd")
          val serviceStarted = BackgroundServiceManager.isServiceStarted
          if (!serviceStarted) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  context.startForegroundService(
                      Intent(
                          context,
                          BackgroundServiceManager::class.java
                      )
                  )
              } else {
                  context.startService(Intent(context, BackgroundServiceManager::class.java))
              }
              Log.i("Service Status->", "Service Started..")
              return Result.success()
          } else {
              Log.i("Service Status->", "Running..")
              return Result.success()
          }
          Log.i("Service Status->", "WorkManager failed to start service..")
          Result.failure()*/
    }




     suspend fun showProgress(progress: Int) {
        val notification = notificationBuilder
            .build()
        val foregroundInfo =
            ForegroundInfo(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        setForeground(foregroundInfo)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = notificationManager?.getNotificationChannel(CHANNEL_ID)
            if (notificationChannel == null) {
                notificationManager?.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
        }
    }

    companion object {

        const val TAG = "ForegroundWorker"
        const val NOTIFICATION_ID = 42
        const val CHANNEL_ID = "Job progress"
        const val ARG_PROGRESS = "Progress"
        private const val DELAY_DURATION = 100L // ms
    }
}


