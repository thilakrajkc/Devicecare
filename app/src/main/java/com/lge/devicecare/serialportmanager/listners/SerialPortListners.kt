package com.lge.devicecare.serialportmanager.listners

import com.lge.ev.micom.SerialPortRxData
import com.lge.ev.micom.SerialPortTxData

interface SerialPortListners {

    fun onCommunication(micomTxData: SerialPortTxData, micomRxData: SerialPortRxData)
    fun onConnectionError(faultList: List<Short>)
}