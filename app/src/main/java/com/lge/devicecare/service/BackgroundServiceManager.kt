package com.lge.devicecare.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import android_serialport_api.SerialPortFinder
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.lge.devicecare.R
import com.lge.devicecare.constants.Constants
import com.lge.devicecare.serialportmanager.serialporthandlers.SerialPortManager
import com.lge.devicecare.ui.MainActivity
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import kotlin.concurrent.schedule


class BackgroundServiceManager : Service() {

    var mqttAndroidClient: MqttAndroidClient? = null

    companion object {
        var isServiceStarted = false
    }

    var successStatus: Boolean? = false

    private var serialPortFinder: SerialPortFinder? = null
    // private var serialHelper: SerialHelper? = null

    @Nullable
    @Override
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    @Override
    override fun onCreate() {
        isServiceStarted = true
        serialPortFinder = SerialPortFinder()
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Override
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Running Notification


        if (Build.VERSION.SDK_INT < 33) {
            createNotificationChannel()
            startNotification()
        } else {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                createNotificationChannel()
                startNotification()
            }
        }


        /*   serialHelper = object : SerialHelper("dev/ttyS4", 115200) {
               override fun onDataReceived(comBean: ComBean) {

                   var data = (comBean.bRec)
                   Log.i("SerialPortData", comBean.bRec.toString())
               }
           }*/

        /*
                val serialportTask = GlobalScope.launch(Dispatchers.IO) {
                    initSerialPort()
                }
                runBlocking {
                    serialportTask.join()
                    SerialPortManager.startThread()


                }*/






        CoroutineScope(Dispatchers.IO).launch {
            MQTTManager.init(applicationContext)
        }


        /* Timer().schedule(10000) {

             GlobalScope.launch(Dispatchers.Main) {
                 publishMQTTMessage()
             }

         }*/
        // publishMQTTMessage()

        /* Toast.makeText(applicationContext, "$successStatus", Toast.LENGTH_SHORT).show()
         if (successStatus!!) {
             publishMQTTMessage()
         }
 */
        /* val task = GlobalScope.launch(Dispatchers.IO) {
         }*/
        /* runBlocking {
             task.join()
             if (successStatus!!) {
                 publishMQTTMessage()
             }
         }*/



        return START_NOT_STICKY
    }

    private fun publishMQTTMessage() {

        //TODO check status of the publish message and iterate the publish message

        MQTTManager.publishMessage(applicationContext)
        MQTTManager.subscribeToTopic(applicationContext)

        Timer().schedule(10000) {
            GlobalScope.launch(Dispatchers.Main) {
                publishMQTTMessage()
            }
        }

    }


    @Override
    override fun onDestroy() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
        Toast.makeText(applicationContext, "Service Destroyed", Toast.LENGTH_SHORT).show()
        MQTTManager.unsubscribeToTopic(applicationContext)

        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                Constants.CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntentnn = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val drawable = ContextCompat.getDrawable(this, R.drawable.img_devicecare_icon)

        val bitmap = (drawable as BitmapDrawable?)!!.bitmap
        val notification: Notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setContentTitle("Device-Care application running..")
            .setContentText("Monitoring EV Charger")
            .setSmallIcon(R.drawable.img_devicecare_icon)
            .setLargeIcon(bitmap)
            .setColor(Color.CYAN)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
    }


    private suspend fun initSerialPort() = withContext(Dispatchers.IO) {

        val port = "4"

        if (!port.isNullOrEmpty()) {
            SerialPortManager.init(port)
        }

        //  SerialPortManager.setSerialListener(applicationContext)
    }


}