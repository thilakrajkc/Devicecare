package com.lge.devicecare.service

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.lge.devicecare.constants.Constants
import com.lge.devicecare.serialportmanager.serialporthandlers.SerialPortManager
import com.lge.devicecare.utils.SLog
import com.lge.devicecare.workmanager.WorkmanagerForService
import info.mqtt.android.service.Ack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

object MQTTManager {

    var mqttAndroidClient: info.mqtt.android.service.MqttAndroidClient? = null


    var corutineScope = CoroutineScope(Dispatchers.IO)
    var connectionStatusFlag = false


    fun init(applicationContext: Context) {

        Constants.listofBoardData = ArrayList()

        Constants.listofBoardData!!.add("lightFront: 45000")
        Constants.listofBoardData!!.add("tempChargeIn1: 0.8℃")
        Constants.listofBoardData!!.add("energy: 2Wh")
        Constants.listofBoardData!!.add("maxPowerOutput: 0.1kW")
        Constants.listofBoardData!!.add("isCharging: off")
        Constants.listofBoardData!!.add("powerOutput: 5W")
        Constants.listofBoardData!!.add("tempConn: 0.1℃")
        Constants.listofBoardData!!.add("cpDuty: 0.1%")
        Constants.listofBoardData!!.add("cpVoltage:  0.1V")

        //  handler = Handler()


        mqttAndroidClient = info.mqtt.android.service.MqttAndroidClient(
            applicationContext,
            Constants.SERVER_URI,
            Constants.CLIENT_ID,
            Ack.AUTO_ACK
        )

        mqttAndroidClient!!.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    //  addToHistory("Reconnected to : $serverURI")
                    // Because Clean Session is true, we need to re-subscribe

                    /*
                                        corutineScope.launch {
                                            delay(20000)
                                            publishMessage(applicationContext)
                                           // subscribeToTopic(applicationContext)
                                        }*/





                    Toast.makeText(applicationContext, "Reconnected !!", Toast.LENGTH_SHORT).show()
                    connectionStatusFlag = true
                    corutineScope.launch(Dispatchers.IO) {
                        publishHandlers(applicationContext)
                    }


                } else {
                    // addToHistory("Connected to: $serverURI")
                    /* connectionStatusFlag = true
                     corutineScope.launch {
                         publishHandlers(applicationContext)
                     }*/

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(applicationContext, "Connected !!", Toast.LENGTH_SHORT)
                            .show()
                    }


                }
            }

            override fun connectionLost(cause: Throwable) {
                //  addToHistory("The Connection was lost.")
                connectionStatusFlag = false
                /* corutineScope.launch {
                     publishHandlers(applicationContext)
                 }*/
                Toast.makeText(applicationContext, "Connection Lost", Toast.LENGTH_SHORT).show()

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                //  addToHistory("Incoming message: " + kotlin.String(message.payload))
                // var msg = URLDecoder.decode(message.payload.toString(), "UTF-8");


                /* Toast.makeText(
                     applicationContext,
                     "Incoming message:" + message.payload.decodeToString(),
                     Toast.LENGTH_SHORT
                 ).show()*/

            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                /* Toast.makeText(
                     applicationContext,
                     "Delivery MSG Token -> :" + token.messageId,
                     Toast.LENGTH_SHORT
                 ).show()*/

                Constants.SPOW_PROGRESS = token.messageId

            }
        })

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = true
        mqttConnectOptions.maxReconnectDelay = 10000


        try {
            Thread.sleep(10000)
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient!!.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    //   disconnectedBufferOptions.isBufferEnabled = true
                    //  disconnectedBufferOptions.bufferSize = 100
                    // disconnectedBufferOptions.isPersistBuffer = false
                    // disconnectedBufferOptions.isDeleteOldestMessages = false
                    // mqttAndroidClient!!.setBufferOpts(disconnectedBufferOptions)
                    Toast.makeText(applicationContext, "Connection Successful", Toast.LENGTH_SHORT)
                        .show()
                    // subscribeToTopic()
                    connectionStatusFlag = true
                    corutineScope.launch(Dispatchers.IO) {
                        publishHandlers(applicationContext)
                    }

                    /*  corutineScope.launch {
                          delay(20000)
                          publishMessage(applicationContext)
                          subscribeToTopic(applicationContext)
                      }*/

                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // addToHistory("Failed to connect to: $serverUri")

                    Log.e("Erorlog", exception.toString())

                    Toast.makeText(
                        applicationContext,
                        "Trying to establish connection..",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    connectionStatusFlag = false
                    /*corutineScope.launch {
                        publishHandlers(applicationContext)
                    }*/

                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }


    }


    fun publishHandlers(applicationContext: Context) {

        if (connectionStatusFlag) {


            publishMessage(applicationContext)


        }

    }


    fun publishMessage(applicationContext: Context) {
        try {
            Thread.sleep(3000)
            val messageFetch = Constants.listofBoardData!!.random()

            SerialPortManager.getRxDATA().plugCheck


            val message = MqttMessage()
            message.payload = messageFetch.toByteArray()
            mqttAndroidClient!!.publish(
                Constants.PUBLISH_TOPIC,
                message,
                applicationContext,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        // addToHistory("Subscribed!")

                        Toast.makeText(applicationContext, "Message Published", Toast.LENGTH_SHORT)
                            .show()
                        connectionStatusFlag = true
                        corutineScope.launch(Dispatchers.IO) {
                            delay(Constants.PUBLISH_DELAY)
                            publishHandlers(applicationContext)
                        }


                        /* corutineScope.launch {
                             delay(30000)

                         }*/


                        /* mqttAndroidClient!!.subscribe(
                             subscriptionTopic, 0
                         ) { topic, message -> // message Arrived!
                             println("Message: " + topic + " : " + message.payload)
                             Toast.makeText(
                                 applicationContext,
                                 "Subscribed  --> " + message.payload,
                                 Toast.LENGTH_SHORT
                             )
                                 .show()
                         }*/


                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {


                        //  addToHistory("Failed to subscribe")

                        Toast.makeText(
                            applicationContext,
                            "Failed to Publish",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })
            // addToHistory("Message Published")


            /*if (!mqttAndroidClient!!.isConnected) {
                //  addToHistory(mqttAndroidClient!!.bufferedMessageCount.toString() + " messages in buffer.")
                Toast.makeText(
                    applicationContext,
                    mqttAndroidClient!!.getBufferedMessageCount()
                        .toString() + " messages in buffer.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }*/
        } catch (e: MqttException) {
            System.err.println("Error Publishing: " + e.message)
            e.printStackTrace()
        }
    }


    fun subscribeToTopic(applicationContext: Context) {
        try {
            mqttAndroidClient!!.subscribe(
                Constants.SUBSCRIPTION_TOPIC,
                0,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        // addToHistory("Subscribed!")

                        Toast.makeText(applicationContext, "Publish subscribed", Toast.LENGTH_SHORT)
                            .show()

                        /* mqttAndroidClient!!.subscribe(
                             subscriptionTopic, 0
                         ) { topic, message -> // message Arrived!
                             println("Message: " + topic + " : " + message.payload)
                             Toast.makeText(
                                 applicationContext,
                                 "Subscribed  --> " + message.payload,
                                 Toast.LENGTH_SHORT
                             )
                                 .show()
                         }*/


                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {


                        //  addToHistory("Failed to subscribe")

                        Toast.makeText(
                            applicationContext,
                            "Failed to subscribe",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })

            // THIS DOES NOT WORK!
            /* mqttAndroidClient!!.subscribe(
                 subscriptionTopic, 0
             ) { topic, message -> // message Arrived!
                 println("Message: " + topic + " : " + message.payload)
             }*/
        } catch (ex: MqttException) {
            System.err.println("Exception whilst subscribing")
            ex.printStackTrace()
        }
    }

    fun unsubscribeToTopic(applicationContext: Context) {
        try {
            mqttAndroidClient!!.unsubscribe(
                Constants.SUBSCRIPTION_TOPIC,
                applicationContext,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        // addToHistory("Subscribed!")

                        Toast.makeText(applicationContext, "Unsubscribed!!", Toast.LENGTH_SHORT)
                            .show()

                        /* mqttAndroidClient!!.subscribe(
                             subscriptionTopic, 0
                         ) { topic, message -> // message Arrived!
                             println("Message: " + topic + " : " + message.payload)
                             Toast.makeText(
                                 applicationContext,
                                 "Subscribed  --> " + message.payload,
                                 Toast.LENGTH_SHORT
                             )
                                 .show()
                         }*/


                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {


                        //  addToHistory("Failed to subscribe")

                        Toast.makeText(
                            applicationContext,
                            "Failed to UnSubscribe",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })

            // THIS DOES NOT WORK!
            /* mqttAndroidClient!!.subscribe(
                 subscriptionTopic, 0
             ) { topic, message -> // message Arrived!
                 println("Message: " + topic + " : " + message.payload)
             }*/
        } catch (ex: MqttException) {
            System.err.println("Exception whilst subscribing")
            ex.printStackTrace()
        }
    }


    fun disconnect(applicationContext: Context) {

        try {
            mqttAndroidClient!!.disconnect(applicationContext, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Toast.makeText(
                        applicationContext,
                        "Disconnected!! -->${asyncActionToken.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Toast.makeText(
                        applicationContext,
                        "Disconnection Failed !!--> $exception",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            })

        } catch (e: Exception) {
            SLog.LogE("$e")
        }

    }


}