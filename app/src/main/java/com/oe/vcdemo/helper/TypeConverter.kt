package com.oe.vcdemo.helper

import kotlin.experimental.or

class TypeConverter internal constructor() {
    /*
     *
     * Converts a byte[2] to a short, in LITTLE_ENDIAN format
     *
     */
    fun getShort(argB1: Byte, argB2: Byte): Short {
        var sTest = 0x0000.toShort()
        sTest = (0x00ff and argB1.toInt()).toShort()
        sTest = (sTest or (0xff00 and (argB2.toInt() shl 8)).toShort()).toShort()
        return sTest
    }

    fun getInt(argB1: Byte, argB2: Byte, argB3: Byte, argB4: Byte): Int {
        var iInt = 0x00000000
        iInt = (0x000000ff and argB1.toInt())
        iInt = (iInt or (0x0000ff00 and (argB2.toInt() shl 8)))
        iInt = (iInt or (0x00ff0000 and (argB3.toInt() shl 16)))
        iInt = (iInt or (-0x1000000 and (argB4.toInt() shl 24)))
        return iInt
    }
}