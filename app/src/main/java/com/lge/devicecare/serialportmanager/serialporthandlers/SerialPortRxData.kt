package com.lge.ev.micom

import kotlin.experimental.and
import kotlin.experimental.or

class SerialPortRxData {
    private var rawData = Array<Short>(Reg.TOTAL_MH_REG.num) { 0 }
    var startAddress: Int = 0
    var totalReg: Int = 0

    fun init() {
        startAddress = Reg.START_REG_ADD.num
        totalReg = Reg.TOTAL_MH_REG.num
        rawData = Array(totalReg) { 0 }
    }

    fun rxDataHandler(data: ByteArray) {
        val newRawData = Array<Short>(Reg.TOTAL_MH_REG.num) { 0 }
        val realDataSize = (data.size - 5) / 2
        for (i in 0 until realDataSize) {
            newRawData[i] = ((data[2 * i + 3].toInt() and 0xff) shl 8).toShort()
            newRawData[i] = newRawData[i] or (data[2 * i + 4].toShort() and 0x00ff)
        }

        rawData = newRawData
    }

    val statusAll get() = rawData[Reg.STATUS.num]
    val ready get() = getBit(statusAll, 0)
    val plugCheck get() = getBit(statusAll, 2)
    val chargingGunCheck get() = getBit(statusAll, 3)
    val start get() = getBit(statusAll, 4)
    val stop get() = getBit(statusAll, 5)
    val fault get() = getBit(statusAll, 6)
    val powerOff get() = getBit(statusAll, 15)

    val isFault: Boolean
        get() {
            return fault or (fault1.toInt() != 0) or (fault2.toInt() != 0)
        }

    // emg fault 별도 추가
    val isEmgFault get() = getBit(rawData[Reg.FAULT1.num], 0)

    val micomVersion: String
        get() {
            return "%x.%02x".format(
                ((rawData[Reg.FW_VERSION.num].toInt() shr 8) and 0xff),
                (rawData[Reg.FW_VERSION.num].toInt() and 0xff)
            )
        }

    val protocolVersion: String
        get() {
            return "%x.%02x".format(
                ((rawData[Reg.PROTOCOL_VERSION.num].toInt() shr 8) and 0xff),
                (rawData[Reg.PROTOCOL_VERSION.num].toInt() and 0xff)
            )
        }

    val runCount get() = rawData[Reg.RUN_COUNT.num].toInt() and 0xffff

    /**
     * Unit : 0.1V, Range : -15V ~ +15V
     */
    val cpVoltage get() = rawData[Reg.CP_VOLTAGE.num].toString()

    /**
     * Unit : 0.1%
     */
    val cpDuty get() = rawData[Reg.CP_DUTY.num].toString()

    val stopCode get() = rawData[Reg.STOP_CODE.num]

    /**
     * Unit : 1Wh
     */
    val energy: Int
        get() {
            return (((rawData[Reg.POWER_METER_H.num].toInt() shl 16) and 0xffff0000.toInt()) or
                    (rawData[Reg.POWER_METER_L.num].toInt() and 0xffff))
        }

    /**
     * Unit : 0.1V
     */
    val vOut get() = rawData[Reg.AC_VOLTAGE.num]

    /**
     * Unit : 0.1A
     */
    val iOut get() = rawData[Reg.AC_CURRENT.num]

    val fault1 get() = rawData[Reg.FAULT1.num]

    /**
     * Unit : 0.1℃
     */
    val tempConn get() = rawData[Reg.CONNECTOR_TEMP.num]

    /**
     * Unit : 0.1℃
     */
    val tempChargeIn1 get() = rawData[Reg.CHARGER_IN_TEMP_1.num]

    /**
     * Unit : 0.1℃
     */
    val tempChargeIn2 get() = rawData[Reg.CHARGER_IN_TEMP_2.num]

    val chargerStatus get() = rawData[Reg.CHARGER_STATUS.num]
    val chargerSeqStep get() = rawData[Reg.CHARGER_SEQ_STEP.num]
    val warning1 get() = rawData[Reg.WARNING_1.num]

    /**
     * Range : 0 ~ 65535
     */
    val lightFront get() = rawData[Reg.LIGHT_FRONT.num].toInt() and 0xffff

    /**
     * Range : 0 ~ 65535
     */
    val proximityFront get() = rawData[Reg.PROXIMITY_FRONT.num].toInt() and 0xffff

    /**
     * Range : 0 ~ 65535
     */
    val proximityHolster get() = rawData[Reg.HOLSTER.num].toInt() and 0xffff

    val accelerationX get() = rawData[Reg.ACCELERATOR_X.num].toInt() and 0xffff
    val accelerationY get() = rawData[Reg.ACCELERATOR_Y.num].toInt() and 0xffff
    val accelerationZ get() = rawData[Reg.ACCELERATOR_Z.num].toInt() and 0xffff

    /**
     * Unit : 1W, Range : 0W ~ 65535W
     */
    val powerOutput: Int get() = rawData[Reg.POWER_OUTPUT.num].toInt() and 0xffff

    val fault2 get() = rawData[Reg.FAULT2.num]
    // val doorOpen get() = BitControl.getBit(fault2, Fault2.DOOR_OPEN.ordinal)

    /**
     * Unit : 0.1kW, 0일 경우 기본값
     */
    val maxPowerOutput: Int get() = rawData[Reg.MAX_POWER_OUTPUT.num].toInt() and 0xffff

    val gpi1ForTest get() = rawData[Reg.GPI1_FOR_TEST.num]

    private enum class Reg(val num: Int) {
        STATUS(0),
        FW_VERSION(1),
        PROTOCOL_VERSION(2),
        RUN_COUNT(3),
        CP_VOLTAGE(4),
        CP_DUTY(5),
        STOP_CODE(6),
        POWER_METER_H(7),
        POWER_METER_L(8),
        AC_VOLTAGE(9),
        AC_CURRENT(10),
        FAULT1(11),
        CONNECTOR_TEMP(12),     // 안 쓰임
        CHARGER_IN_TEMP_1(13),
        CHARGER_IN_TEMP_2(14),  // 안 쓰임
        CHARGER_STATUS(15),
        CHARGER_SEQ_STEP(16),
        WARNING_1(17),
        LIGHT_FRONT(18),
        PROXIMITY_FRONT(20),
        HOLSTER(21),
        ACCELERATOR_X(22),
        ACCELERATOR_Y(23),
        ACCELERATOR_Z(24),
        POWER_OUTPUT(25),
        FAULT2(26),
        MAX_POWER_OUTPUT(27),
        GPI1_FOR_TEST(28),

        TOTAL_MH_REG(29),
        START_REG_ADD(300)
    }

    enum class StopCode {
        ESTOP_FINISH_NONE,
        ESTOP_UI_STOP,      // 사용자 정지버튼
        ESTOP_EMG_PUSHED,   // 비상정지 버튼
        ESTOP_CP_ABNORMAL,  // CP 상태 오류
        ESTOP_CHG_FAULT,    // 충전기 Fault
        ESTOP_ETC           // 기타
    }

    enum class Fault1 {
        EMERGENCY_SWITCH,
        MAIN_BREAKER_TRIP,
        RELAY_1,
        RELAY_2,
        AC_OV,
        AC_UV,
        AC_OC,
        CRASH,
        UNUSED_1,
        CP_STATUS,
        FLOODING,
        DM1_COMM,
        INNER_OT_1,
        INNER_OT_2,
        UI_COMM,
        ETC
    }

    enum class Fault2 {
        DOOR_OPEN,
        AC_LEAKAGE,
        DC_LEAKAGE,
        LEAKAGE_MODULE_ERROR,
        LIGHT_FRONT_SENSOR_ERROR,
        HOLSTER_SENSOR_ERROR,
        GROUND_ERROR,
    }

    enum class GPI1 {
        FLOOD,
        DOOR,
        EMG,
        RELAY_L,
        RELAY_N,
        RELAY_LN,
        AC_LEAKAGE,
        DC_LEAKAGE,
        GROUND,
        FAN_ALARM,
        EYE,
        GUN_EYE
    }

    private fun getBit(status: Short, bitOffset: Int): Boolean {
        return (status.toInt() shr bitOffset) and 0x01 > 0
    }
}