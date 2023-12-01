package com.lge.ev.micom


import android.util.Log
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class SerialPortTxData {
    private var rawData = Array<Short>(Reg.TOTAL_MH_REG.num) { 0 }
    var startAddress: Int = 0
    var totalReg: Int = 0

    fun init() {
        startAddress = Reg.START_REG_ADD.num
        totalReg = Reg.TOTAL_MH_REG.num
        rawData = Array(totalReg) { 0 }
    }

    fun getSendTx(): Array<Short> {
        rawData[Reg.RUN_COUNT.num]++
        return rawData
    }

    var statusAll: Short = 0
        get() = rawData[Reg.STATUS.num]
        set(value) {
            rawData[Reg.STATUS.num] = value
            field = value
        }

    var ready: Boolean = false
        get() {
            return getBit(statusAll, 0)
        }
        set(value) {
            setBit(0, value)
            field = value
        }

    var chargingReady: Boolean = false
        get() {
            return getBit(statusAll, 2)
        }
        set(value) {
            setBit(2, value)
            field = value
        }

    var start: Boolean = false
        get() {
            return getBit(statusAll, 3)
        }
        set(value) {
            setBit(3, value)
            field = value
        }

    var stop: Boolean = false
        get() {
            return getBit(statusAll, 4)
        }
        set(value) {
            setBit(4, value)
            field = value
        }

    var fault: Boolean = false
        get() {
            return getBit(statusAll, 6)
        }
        set(value) {
            setBit(6, value)
            field = value
        }

    var reset: Boolean = false
        get() {
            return getBit(statusAll, 7)
        }
        set(value) {
            setBit(7, value)
            field = value
        }

    var update: Boolean = false
        get() {
            return getBit(statusAll, 11)
        }
        set(value) {
            setBit(11, value)
            field = value
        }

    var maxPowerSet: Boolean = false
        get() {
            return getBit(statusAll, 12)
        }
        set(value) {
            setBit(12, value)
            field = value
        }

    var useDR: Boolean = false
        get() {
            return getBit(statusAll, 14)
        }
        set(value) {
            setBit(14, value)
            field = value
        }

    var powerOffAck: Boolean = false
        get() {
            return getBit(statusAll, 15)
        }
        set(value) {
            Log.d("MAG", "PowerOff")
            setBit(15, value)
            field = value
        }

    val runCount get() = rawData[Reg.RUN_COUNT.num].toInt() and 0xffff

    var cableType: Short = 0
        get() = rawData[Reg.CABLE_TYPE.num]
        set(value) {
            rawData[Reg.CABLE_TYPE.num] = value
            field = value
        }

    var opMode: Short = 0
        get() = rawData[Reg.TEST_MODE.num]
        set(value) {
            rawData[Reg.TEST_MODE.num] = value
            field = value
        }

    /**
     * Unit : 0.1V
     */
    var vOutCmd: Short = 0
        get() = rawData[Reg.V_OUT_CMD.num]
        set(value) {
            rawData[Reg.V_OUT_CMD.num] = value
            field = value
        }

    /**
     * Unit : 0.1A
     */
    var iOutCmd: Short = 0
        get() = rawData[Reg.I_OUT_CMD.num]
        set(value) {
            rawData[Reg.I_OUT_CMD.num] = value
            field = value
        }

    /**
     * Unit : 1A or 0.1kW
     */
    var dr: Short = 0
        get() = rawData[Reg.DR.num]
        set(value) {
            rawData[Reg.DR.num] = value
            field = value
        }

    var testO1: Short = 0
        get() = rawData[Reg.GP01_TEST.num]
        set(value) {
            rawData[Reg.GP01_TEST.num] = value
            field = value
        }

    var testO2: Short = 0
        get() = rawData[Reg.GP02_TEST.num]
        set(value) {
            rawData[Reg.GP02_TEST.num] = value
            field = value
        }

    /**
     * Unit : 0.1kW
     */
    var maxPowerSetting: Short = 0
        get() = rawData[Reg.MAX_POWER_SETTING.num]
        set(value) {
            rawData[Reg.MAX_POWER_SETTING.num] = value
            field = value
        }

    var hmiCurrentPage: Short = 0
        get() = rawData[Reg.HMI_CURRENT_PAGE.num]
        set(value) {
            rawData[Reg.HMI_CURRENT_PAGE.num] = value
            field = value
        }

    private fun getBit(status: Short, bitOffset: Int): Boolean {
        return (status.toInt() shr bitOffset) and 0x01 > 0
    }

    private fun setBit(offset: Int, value: Boolean) {
        val bitSet = 1

        statusAll = if (value) {
            val data = (bitSet shl offset).toShort()
            statusAll or data

        } else {
            val data = (bitSet shl offset).toShort()
            statusAll and data.inv()
        }
    }

    fun setBitTestO1(offset: Int, value: Boolean) {
        val bitSet = 1

        testO1 = if (value) {
            val data = (bitSet shl offset).toShort()
            testO1 or data

        } else {
            val data = (bitSet shl offset).toShort()
            testO1 and data.inv()
        }
    }

    fun setBitTestO2(offset: Int, value: Boolean) {
        val bitSet = 1

        testO2 = if (value) {
            val data = (bitSet shl offset).toShort()
            testO2 or data

        } else {
            val data = (bitSet shl offset).toShort()
            testO2 and data.inv()
        }
    }

    enum class Reg(val num: Int) {
        STATUS(0),
        CABLE_TYPE(1),
        RUN_COUNT(2),
        TEST_MODE(3),
        V_OUT_CMD(4),
        I_OUT_CMD(5),
        DR(6),
        GP01_TEST(7),
        GP02_TEST(8),
        MAX_POWER_SETTING(9),
        HMI_CURRENT_PAGE(10),

        TOTAL_MH_REG(11),
        START_REG_ADD(200)

    }

    enum class GP01Test {
        CP_RY1,       // 0
        LED_R1,       // 1
        LED_G1,       // 2
        LED_B1,       // 3
        LED_R2A,      // 4
        LED_G2Buz,    // 5
        LED_B2,       // 6
        RY1,          // 7
        RY2,          // 8
        RY3,          // 9
        RY4,          // 10
        RY5,          // 11
        RY6,          // 12
        RY7,          // 13
        FAN1,         // 14
        FAN2          // 15
    }

    enum class GP02Test {
        Door1,          // 0
        Door2,          // 1
        Door3,          // 2
        MC1,            // 3
        MC2_MainAC,     // 4
        MC3_SubAC,      // 5
        Solenoid,       // 6
        D1Sw,           // 7
        D2Sw,           // 8
        OutputMode,     // 9
        Enable1_Power,  // 10
        Enable2_LED,    // 11
        Leakage_Test,   // 12
        Acm_RST,        // 13
        CP_OUT          // 14
    }


    enum class HMICurrentPage(val num: Int) {
        NONE(0),
        WAITING(1),
        AUTHORIZE(2),
        CONNECTOR_PLUG(3),
        CONNECTOR_COMMUNICATE(4),
        CHARGING(5),
        CHARGE_FINISHING(6),
        CHARGE_FINISH(7),
        CONNECTOR_REMOVE(8),
        ERROR(9),
        ERROR_RESTORE(10),
        ERROR_EMERGENCY(11),
        ERROR_EMERGENCY_RESTORE(12),
        UNAVAILABLE(13),
        OTHER(14)
    }
}