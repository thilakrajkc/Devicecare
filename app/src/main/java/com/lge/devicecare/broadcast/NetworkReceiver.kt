package com.lge.devicecare.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.util.Log


class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {


        Log.i("Network status", "Networkkk")

/*        val info: NetworkInfo = intent!!.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)!!
        if (info != null && info.isConnected) {
            // Do your work.

            Log.i("Network status", "Networkkk")

            // e.g. To check the Network Name or other info:
            val wifiManager = context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.ssid
        }*/
    }

    fun isConnected(context: Context): Boolean {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null &&
                activeNetwork.isConnected
    }
}