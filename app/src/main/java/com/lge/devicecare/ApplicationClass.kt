package com.lge.devicecare

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.lge.devicecare.constants.Constants
import com.lge.devicecare.constants.Constants.isWorkScheduled
import com.lge.devicecare.quicksettingtile.QuickSettingTile
import com.lge.devicecare.quicksettingtile.RequestResult
import com.lge.devicecare.service.BackgroundServiceManager
import com.lge.devicecare.ui.MainActivity
import com.lge.devicecare.utils.SLog
import com.lge.devicecare.workmanager.WorkmanagerForService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class ApplicationClass : Application() {
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
    }



    override fun onCreate() {
        super.onCreate()


        SLog.enableFileLogging()
        SLog.init(getExternalFilesDir(null)?.absolutePath + "/log")
        checkPermission(
            android.Manifest.permission.POST_NOTIFICATIONS,
            CAMERA_PERMISSION_CODE
        )


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusBarManager: StatusBarManager = getSystemService(StatusBarManager::class.java)

            statusBarManager.requestAddTileService(
                ComponentName(
                    this,
                    QuickSettingTile::class.java
                ),
                getString(R.string.app_name),
                Icon.createWithResource(this, R.drawable.img_devicecare_icon),
                {}
            ) { resultCodeFailure ->
                Log.d(
                    ContentValues.TAG,
                    "requestAddTileService failure: resultCodeFailure: $resultCodeFailure"
                )
                val resultFailureText =
                    when (val ret = RequestResult.findByCode(resultCodeFailure)) {
                        RequestResult.TILE_ADD_REQUEST_ERROR_APP_NOT_IN_FOREGROUND,
                        RequestResult.TILE_ADD_REQUEST_ERROR_BAD_COMPONENT,
                        RequestResult.TILE_ADD_REQUEST_ERROR_MISMATCHED_PACKAGE,
                        RequestResult.TILE_ADD_REQUEST_ERROR_NOT_CURRENT_USER,
                        RequestResult.TILE_ADD_REQUEST_ERROR_NO_STATUS_BAR_SERVICE,
                        RequestResult.TILE_ADD_REQUEST_ERROR_REQUEST_IN_PROGRESS,
                        RequestResult.TILE_ADD_REQUEST_RESULT_TILE_ADDED,
                        RequestResult.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED,
                        RequestResult.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> {
                            ret.name
                        }

                        null -> {
                            "unknown resultCodeFailure: $resultCodeFailure"
                        }
                    }

            }
        }

        //  createNotificationChannel()

        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             startForegroundService(
                 Intent(
                     applicationContext,
                     BackgroundServiceManager::class.java
                 )
             )
         } else {
             startService(Intent(applicationContext, BackgroundServiceManager::class.java))
         }
 */

     /*   if (isWorkScheduled(Constants.TAG_WORKMANAGER)) {
            Toast.makeText(applicationContext, "Workmanager is active !!", Toast.LENGTH_SHORT)
                .show()
            SLog.LogD("============================ location:  Work-manager is Active !!")

        } else {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequest
                .Builder(WorkmanagerForService::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            Log.d("DTAG", "scheduled: " + periodicWorkRequest.id.toString().substring(0, 3));



            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                Constants.TAG_WORKMANAGER,
                ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest

            )
            SLog.LogD("============================ location:  Work-manager Scheduled !!")

        }*/

        // WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)


    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                Constants.CHANNEL_ID,
                Constants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {

            // Requesting the permission
            ActivityCompat.requestPermissions(
                applicationContext as Activity,
                arrayOf(permission),
                requestCode
            )
        } else {
            Toast.makeText(applicationContext, "Permission already granted", Toast.LENGTH_SHORT)
                .show()
        }
    }
}