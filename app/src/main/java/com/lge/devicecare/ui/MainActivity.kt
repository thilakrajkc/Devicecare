package com.lge.devicecare.ui

import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lge.devicecare.R
import com.lge.devicecare.constants.Constants
import com.lge.devicecare.quicksettingtile.QuickSettingTile
import com.lge.devicecare.quicksettingtile.RequestResult
import com.lge.devicecare.service.MQTTManager
import com.lge.devicecare.utils.SLog
import com.lge.devicecare.workmanager.WorkmanagerForService
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (Build.VERSION.SDK_INT >= 34) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Notification permission granted",
                    Toast.LENGTH_SHORT
                ).show()


                Log.d("DTAG", "scheduled: " + Constants.isWorkScheduled(Constants.TAG_WORKMANAGER));
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val periodicWorkRequest = PeriodicWorkRequest
                    .Builder(WorkmanagerForService::class.java, 16, TimeUnit.MINUTES)
                    .build()

                WorkManager.getInstance(applicationContext!!).enqueueUniquePeriodicWork(
                    Constants.TAG_WORKMANAGER,
                    ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest
                )


            }
        }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setCancelable(false)
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .show()
    }


    private fun showNotificationPermissionRationale() {

        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Alert")
            .setCancelable(false)
            .setMessage("Notification permission is required, to show notification")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 34) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .show()
    }

    var hasNotificationPermissionGranted = false


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val statusBarManager: StatusBarManager = getSystemService(StatusBarManager::class.java)

        SLog.LogI("Application Started")

        Toast.makeText(applicationContext, "Hello", Toast.LENGTH_SHORT).show()

        val btn_enable = findViewById<Button>(R.id.btn_enable)
        val btn_unsbscibe = findViewById<Button>(R.id.btn_unsbscibe)

        val const_one = findViewById<ConstraintLayout>(R.id.const_one)
        val const_two = findViewById<ConstraintLayout>(R.id.const_two)
        const_one.visibility = View.GONE
        const_two.visibility = View.GONE


        btn_unsbscibe.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                        TAG,
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
            //  MQTTManager.unsubscribeToTopic(applicationContext)
        }

        if (Build.VERSION.SDK_INT < 33) {
            const_one.visibility = View.VISIBLE
            const_two.visibility = View.GONE


            Toast.makeText(
                applicationContext,
                "Notification permission enabled for this device !!",
                Toast.LENGTH_SHORT
            ).show()

            Log.d("DTAG", "scheduled: " + Constants.isWorkScheduled(Constants.TAG_WORKMANAGER));
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequest
                .Builder(WorkmanagerForService::class.java, 16, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            // WorkManager.getInstance(applicationContext!!).enqueue(periodicWorkRequest)

            WorkManager.getInstance(applicationContext!!).enqueueUniquePeriodicWork(
                Constants.TAG_WORKMANAGER,
                ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest
            )


            /*    Handler().postDelayed({
                    val p = packageManager
                    val componentName = ComponentName(this, MainActivity::class.java)
                    p.setComponentEnabledSetting(
                        componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }, 10000)*/


        } else {
            const_one.visibility = View.GONE
            const_two.visibility = View.VISIBLE
        }



        btn_enable.setOnClickListener {

            if (Build.VERSION.SDK_INT >= 33) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                hasNotificationPermissionGranted = true
            }
        }


        /*        Log.d("DTAG", "scheduled: " + Constants.isWorkScheduled(Constants.TAG_WORKMANAGER));
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val periodicWorkRequest = PeriodicWorkRequest
                    .Builder(WorkmanagerForService::class.java, 16, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
                *//*
                  WorkManager.getInstance(applicationContext!!).enqueueUniquePeriodicWork(
                      Constants.TAG_WORKMANAGER,
                      ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest
                  )*//*
        WorkManager.getInstance(applicationContext!!).enqueue(periodicWorkRequest)*/


        /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              startForegroundService(Intent(this, BackgroundServiceManager::class.java))
          } else {
              startService(Intent(this, BackgroundServiceManager::class.java))
          }*/
    }


}