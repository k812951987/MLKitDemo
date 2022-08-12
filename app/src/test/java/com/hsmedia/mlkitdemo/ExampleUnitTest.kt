package com.hsmedia.mlkitdemo

import android.text.TextUtils
import android.util.Log
import org.junit.Test

import org.junit.Assert.*
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)


        println("测试：${getPhoneNumber("。雅草15555553793-456")}")
    }

    private val PHONE =//|(1\*{6}\d{4})|(1\d{2}\*{4}\d{4})
        "(\\d{7,13}-\\d{2,6}|1\\*{6}\\d{4}|1\\d{2}\\*{4}\\d{4}|1[3-9]\\d{9})\$"

    private fun getPhoneNumber(num: String?): String? {
        val p =
            Pattern.compile(PHONE)
        val m = p.matcher(num)
        if (m.find()) {
            return m.group()
        }
        return "未知手机号"
    }

}