package com.lge.devicecare.constants

import android.app.NotificationManager
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lge.devicecare.R
import com.lge.devicecare.workmanager.WorkmanagerForService
import java.util.concurrent.ExecutionException

object Constants {

   // const val SERVER_URI = "tcp://public.mqtthq.com:1883"
    const val SERVER_URI = "tcp://13.234.119.129:1883"
   // const val SERVER_URI = "tcp://10.221.44.198:1883"
 //   const val SERVER_URI = "mqtts//10.221.44.198:1883/"
    //const val SERVER_URI = "localhost:1883"
    const val CLIENT_ID = "mqttx_7d57719a"
    const val USERNAME = ""
    const val PASSWORD = ""
    const val SUBSCRIPTION_TOPIC = "mqttHQ-client-test/ev"
    const val PUBLISH_TOPIC = "test/topic"
   // const val PUBLISH_TOPIC = "mqttHQ-client-test/ev"
    const val MESSAGE_TOBE_PUBLISHED = "This is json dataa"

    const val CHANNEL_ID = "autoStartServiceChannel"
    const val CHANNEL_NAME = "Auto Start Service Channel"
    const val TAG_WORKMANAGER = "serviceWork"
    const val PUBLISH_DELAY: Long =20000
    var SPOW_PROGRESS : Int =0


    var listofBoardData: ArrayList<String>? = null


    public fun isWorkScheduled(tag: String): Boolean {
        val instance = WorkManager.getInstance()
        val statuses = instance.getWorkInfosByTag(tag)
        return try {
            var running = false
            val workInfoList = statuses.get()
            for (workInfo in workInfoList) {
                val state = workInfo.state
                running = (state == WorkInfo.State.RUNNING) or (state == WorkInfo.State.ENQUEUED)
            }
            running
        } catch (e: ExecutionException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }


}