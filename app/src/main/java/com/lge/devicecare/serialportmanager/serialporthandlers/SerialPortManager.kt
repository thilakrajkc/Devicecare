package com.lge.devicecare.serialportmanager.serialporthandlers

import android.util.Log
import android_serialport_api.SerialPort
import android_serialport_api.SerialPortFinder
import com.lge.devicecare.serialportmanager.listners.SerialPortListners
import com.lge.ev.micom.SerialPortRxData
import com.lge.ev.micom.SerialPortTxData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Timer
import kotlin.concurrent.schedule

object SerialPortManager {

    private val SERIAL_BAUD = 38400

    private var serialPort: SerialPort? = null
    private var thread: Thread? = null

    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private var mListener: SerialPortListners? = null

    private var micomTxData = SerialPortTxData()
    private var micomRxData = SerialPortRxData()

    fun getRxDATA()= micomRxData


    private var sendMode = 0
    private var commandErrorCnt = 0
    private var threadCnt = 0




    private val isAvailableMutableStateFlow = MutableStateFlow(true)
    val isAvailableStateFlow = isAvailableMutableStateFlow.asStateFlow()

    fun init(port: String) {

        Log.i("Init","InitCAlled")


        micomTxData.init()
        micomRxData.init()

        openSerialPort("ttyS$port")

        val inputStream = serialPort?.inputStream
        val outputStream = serialPort?.outputStream

        if (inputStream == null) {
            Log.d("MSG", "can't open input stream")
            return

        } else if (outputStream == null) {
            Log.d("MSG", "can't open output stream")
            return
        }

        SerialPortManager.inputStream = inputStream
        SerialPortManager.outputStream = outputStream

    }


    fun close() {
        serialPort?.close()
        serialPort = null

        thread?.interrupt()
        thread = null
    }

    fun setSerialListener(listener: SerialPortListners?) {
        mListener = listener
    }

    fun getTx(): SerialPortTxData {
        return micomTxData
    }

    fun getRx(): SerialPortRxData {
        return micomRxData
    }

    fun commandChargeStandby() {
        micomTxData.ready = true
        micomTxData.start = false
        micomTxData.stop = false
    }

    fun commandChargeStart() {
        micomTxData.ready = false
        micomTxData.start = true
        micomTxData.stop = false
    }

    fun commandChargeStop() {
        micomTxData.ready = false
        micomTxData.start = false
        micomTxData.stop = true
    }


    private fun openSerialPort(name: String) {
        try {
            val serialPortFinder = SerialPortFinder()
            val deviceList = serialPortFinder.allDevices.toList()
            val devicePathList = serialPortFinder.allDevicesPath.toList()

            for ((i, device) in deviceList.withIndex()) {
                if (!device.contains(name, true)) {
                    continue
                }

                serialPort = SerialPort(
                    File(devicePathList[i]),
                    SERIAL_BAUD,
                    0
                )
                break
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("MSG", "Exception : ${e.stackTraceToString()}")
        }
    }

    fun startThread() {
        thread = Thread {
            Log.d("MSG", "Thread Start!")

            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(20000)

                    checkCommand()

                    if (inputStream.available() > 0) {
                        val buffer = ByteArray(1024)
                        val size = inputStream.read(buffer)
                        onReceivedData(buffer, size)


                    }

                    if (sendMode == 0) {
                        sendReadRequest(outputStream)

                    } else {
                        sendWriteRequest(outputStream)
                    }

                    sendMode++
                    if (sendMode % 2 == 0) {
                        sendMode = 0
                    }

                    // 1초에 한번씩 전달
                    if (threadCnt % 4 == 0) {
                        mListener?.onCommunication(micomTxData, micomRxData)
                        threadCnt = 0
                    }

                    threadCnt++

                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    Log.d("MSG", "Thread Interrupted!")
                    break

                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("MSG", "Thread IOException : ${e.stackTraceToString()}")

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("MSG", "Thread Exception : ${e.stackTraceToString()}")
                }
            }

            Log.d("MSG", "Thread End!")
        }

        thread?.start()
    }

    private fun checkCommand() {
        if (commandErrorCnt < Int.MAX_VALUE) {
            commandErrorCnt++
        }

        isAvailableMutableStateFlow.value = if (commandErrorCnt > (5 * 30)) {
            //To do
            /* onConnectionError()*/
            false

        } else {
            true
        }
    }


    private fun onReceivedData(buffer: ByteArray, size: Int) {

        Log.d("MSG", "Entered Receivedata")

        if (size <= 5) {
            Log.d("MSG", "Invalid Data!")
            return
        }

        val receiveData = buffer.sliceArray(0 until size)
        val slaveAddress = receiveData[0].toInt()
        if (slaveAddress != 1 && slaveAddress != 2) {
            Log.d("MSG", "Invalid Slave Address!")
            return
        }

        val crc = getCRC16(receiveData, 0, receiveData.size - 2)
        if ((receiveData[receiveData.lastIndex - 1] == crc[0]) && (receiveData.last() == crc[1])) {
            Log.d("Receiveddata", "receive data : ${receiveData}")
            Log.d("Receiveddata--->", "PlugCheck : ${getRxDATA().plugCheck}")


            if (receiveData[1].toInt() == 0x04) {
                micomRxData.rxDataHandler(receiveData)
                commandErrorCnt = 0

                if (micomRxData.powerOff) {
                    Timer().schedule(5000) {
                        micomTxData.powerOffAck = true
                    }
                }
            }

        } else {

            Log.d("MSG", "CRC Error - calculation : " + bytesToHex(crc))
            //  Log.d("MSG", receiveData.toHexString("-"))
        }
    }

    private fun sendWriteRequest(outputStream: OutputStream) {
        try {
            outputStream.flush()

            val rawData = micomTxData.getSendTx()
            val data = ByteArray(rawData.size * 2 + 9)

            var idx = 0
            data[idx++] = (1).toByte()
            data[idx++] = 0x10

            data[idx++] = (micomTxData.startAddress shr 8).toByte()
            data[idx++] = micomTxData.startAddress.toByte()

            val totalReg = micomTxData.totalReg
            data[idx++] = (totalReg shr 8).toByte()
            data[idx++] = totalReg.toByte()
            data[idx++] = (totalReg * 2).toByte()

            for (i in 0 until totalReg) {
                data[idx++] = (rawData[i].toInt() shr 8).toByte()
                data[idx++] = rawData[i].toByte()
            }

            val crc = getCRC16(data, 0, data.size - 2)
            data[idx++] = crc[0]
            data[idx] = crc[1]

            outputStream.write(data)
//            SLog.LogV("send data : ${data.toHexString("-")}, status : ${rawData[MicomTxData.Reg.STATUS.num].toUShort()}")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("MSG", "Exception : ${e.stackTraceToString()}")
        }
    }

    private fun sendReadRequest(outputStream: OutputStream) {
        try {
            outputStream.flush()

            val data = ByteArray(8)

            var idx = 0
            data[idx++] = (1).toByte()
            data[idx++] = 0x04

            data[idx++] = (micomRxData.startAddress shr 8).toByte()
            data[idx++] = micomRxData.startAddress.toByte()

            val totalReg = micomRxData.totalReg
            data[idx++] = (totalReg shr 8).toByte()
            data[idx++] = totalReg.toByte()

            val crc = getCRC16(data, 0, data.size - 2)
            data[idx++] = crc[0]
            data[idx] = crc[1]

            outputStream.write(data)
//            SLog.LogV("send data : ${data.toHexString("-")}")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("MSG", "Exception : ${e.stackTraceToString()}")
        }
    }


    private fun getCRC16(data: ByteArray, startIdx: Int, endIdx: Int): ByteArray {
        var cal = 0xFFFF

        for (i in startIdx until endIdx) {
            cal = (cal shr 8) xor crcTable[(cal xor data[i].toInt()) and 0xff]
        }

        val retVal = ByteArray(2)

        retVal[0] = cal.toByte()
        retVal[1] = (cal shr 8).toByte()

        return retVal
    }

    private val crcTable = intArrayOf(
        0X0000, 0XC0C1, 0XC181, 0X0140, 0XC301, 0X03C0, 0X0280, 0XC241,
        0XC601, 0X06C0, 0X0780, 0XC741, 0X0500, 0XC5C1, 0XC481, 0X0440,
        0XCC01, 0X0CC0, 0X0D80, 0XCD41, 0X0F00, 0XCFC1, 0XCE81, 0X0E40,
        0X0A00, 0XCAC1, 0XCB81, 0X0B40, 0XC901, 0X09C0, 0X0880, 0XC841,
        0XD801, 0X18C0, 0X1980, 0XD941, 0X1B00, 0XDBC1, 0XDA81, 0X1A40,
        0X1E00, 0XDEC1, 0XDF81, 0X1F40, 0XDD01, 0X1DC0, 0X1C80, 0XDC41,
        0X1400, 0XD4C1, 0XD581, 0X1540, 0XD701, 0X17C0, 0X1680, 0XD641,
        0XD201, 0X12C0, 0X1380, 0XD341, 0X1100, 0XD1C1, 0XD081, 0X1040,
        0XF001, 0X30C0, 0X3180, 0XF141, 0X3300, 0XF3C1, 0XF281, 0X3240,
        0X3600, 0XF6C1, 0XF781, 0X3740, 0XF501, 0X35C0, 0X3480, 0XF441,
        0X3C00, 0XFCC1, 0XFD81, 0X3D40, 0XFF01, 0X3FC0, 0X3E80, 0XFE41,
        0XFA01, 0X3AC0, 0X3B80, 0XFB41, 0X3900, 0XF9C1, 0XF881, 0X3840,
        0X2800, 0XE8C1, 0XE981, 0X2940, 0XEB01, 0X2BC0, 0X2A80, 0XEA41,
        0XEE01, 0X2EC0, 0X2F80, 0XEF41, 0X2D00, 0XEDC1, 0XEC81, 0X2C40,
        0XE401, 0X24C0, 0X2580, 0XE541, 0X2700, 0XE7C1, 0XE681, 0X2640,
        0X2200, 0XE2C1, 0XE381, 0X2340, 0XE101, 0X21C0, 0X2080, 0XE041,
        0XA001, 0X60C0, 0X6180, 0XA141, 0X6300, 0XA3C1, 0XA281, 0X6240,
        0X6600, 0XA6C1, 0XA781, 0X6740, 0XA501, 0X65C0, 0X6480, 0XA441,
        0X6C00, 0XACC1, 0XAD81, 0X6D40, 0XAF01, 0X6FC0, 0X6E80, 0XAE41,
        0XAA01, 0X6AC0, 0X6B80, 0XAB41, 0X6900, 0XA9C1, 0XA881, 0X6840,
        0X7800, 0XB8C1, 0XB981, 0X7940, 0XBB01, 0X7BC0, 0X7A80, 0XBA41,
        0XBE01, 0X7EC0, 0X7F80, 0XBF41, 0X7D00, 0XBDC1, 0XBC81, 0X7C40,
        0XB401, 0X74C0, 0X7580, 0XB541, 0X7700, 0XB7C1, 0XB681, 0X7640,
        0X7200, 0XB2C1, 0XB381, 0X7340, 0XB101, 0X71C0, 0X7080, 0XB041,
        0X5000, 0X90C1, 0X9181, 0X5140, 0X9301, 0X53C0, 0X5280, 0X9241,
        0X9601, 0X56C0, 0X5780, 0X9741, 0X5500, 0X95C1, 0X9481, 0X5440,
        0X9C01, 0X5CC0, 0X5D80, 0X9D41, 0X5F00, 0X9FC1, 0X9E81, 0X5E40,
        0X5A00, 0X9AC1, 0X9B81, 0X5B40, 0X9901, 0X59C0, 0X5880, 0X9841,
        0X8801, 0X48C0, 0X4980, 0X8941, 0X4B00, 0X8BC1, 0X8A81, 0X4A40,
        0X4E00, 0X8EC1, 0X8F81, 0X4F40, 0X8D01, 0X4DC0, 0X4C80, 0X8C41,
        0X4400, 0X84C1, 0X8581, 0X4540, 0X8701, 0X47C0, 0X4680, 0X8641,
        0X8201, 0X42C0, 0X4380, 0X8341, 0X4100, 0X81C1, 0X8081, 0X4040
    )


    private val digits = "0123456789ABCDEF"
    private fun bytesToHex(byteArray: ByteArray): String {
        val hexChars = CharArray(byteArray.size * 2)

        for (i in byteArray.indices) {
            val v = byteArray[i].toInt() and 0xff
            hexChars[i * 2] = digits[v shr 4]
            hexChars[i * 2 + 1] = digits[v and 0xf]
        }

        return String(hexChars)
    }

}