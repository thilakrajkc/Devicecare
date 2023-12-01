package com.lge.devicecare.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lge.devicecare.constants.Constants
import com.lge.devicecare.workmanager.WorkmanagerForService
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


class AutoStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {

/*
        Toast.makeText(context, "" + p1!!.action, Toast.LENGTH_SHORT).show()*/

        Log.d("DTAGboot", "scheduled: " + Constants.isWorkScheduled(Constants.TAG_WORKMANAGER));
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequest
            .Builder(WorkmanagerForService::class.java, 16, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context!!).enqueue(periodicWorkRequest)


    }


}