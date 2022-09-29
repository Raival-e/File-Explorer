package com.raival.fileexplorer.tab.file.misc.md5

object StringUtils {

    private val DIGITS_LOWER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    private val DIGITS_UPPER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    fun encodeHex(data: ByteArray, toLowerCase: Boolean): CharArray {
        return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
    }

    fun encodeHex(data: ByteArray, toDigits: CharArray): CharArray {
        val l = data.size
        val out = CharArray(l shl 1)
        // two characters form the hex value.
        var i = 0
        var j = 0
        while (i < l) {
            out[j++] = toDigits[0xF0 and data[i].toInt() ushr 4]
            out[j++] = toDigits[0x0F and data[i].toInt()]
            i++
        }
        return out
    }

}
