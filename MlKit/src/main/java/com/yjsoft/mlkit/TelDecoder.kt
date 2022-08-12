package com.yjsoft.mlkit

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.util.regex.Pattern

class TelDecoder : Decoder {
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun onDecoder(imageProxy: ImageProxy): Data? {
        val mediaImage = imageProxy.image
        val image = mediaImage?.let {
            InputImage.fromMediaImage(
                it,
                imageProxy.imageInfo.rotationDegrees
            )
        }
        return handlePhone(image)
    }

    override fun getType(): ScanType {
        return ScanType.RESULT_TEL
    }

    class ElementData(var data: Text.Element, var phone: String)

    private var tempTel: Data? = null

    private fun handlePhone(bit: InputImage?): Data? {
        if (bit == null) return null
        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        val task = recognizer.process(bit)
        val result = Tasks.await(task)
        val phoneList = ArrayList<ElementData>()
        for (block in result.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val elementText = element.text
                    val num = getPhoneNumber(elementText)
                    num?.run {
                        phoneList.add(ElementData(element, this))
                    }
                }
            }
        }
        val matrixPhone = matrixPhone(phoneList)
//        if (!checkPhone(matrixPhone)) {
//            return null
//        }
        return matrixPhone
    }

    private fun checkPhone(matrixPhone: Data?): Boolean {
        matrixPhone?.run {
            if (tempTel != null && tempTel!!.content == matrixPhone.content) {
                return true
            } else {
                tempTel = matrixPhone
            }
        }
        return false
    }

    private fun matrixPhone(phoneList: ArrayList<ElementData>): Data? {
        var topElement: ElementData? = null
        var leftElement: ElementData? = null
        if (phoneList.isNullOrEmpty()) return null
        phoneList.forEach {
            topElement?.run {
                if (data.boundingBox != null && it.data.boundingBox != null && data.boundingBox!!.centerY() > it.data.boundingBox!!.centerY()) {
                    topElement = it
                }
            }
            leftElement?.run {
                if (data.boundingBox != null && it.data.boundingBox != null && data.boundingBox!!.centerX() < it.data.boundingBox!!.centerX()) {
                    leftElement = it
                }
            }
            if (topElement == null) topElement = it
            if (leftElement == null) leftElement = it
        }
        var isTrust = false
        var data: Data? = null
        topElement?.run {
            if (topElement == leftElement) {
                isTrust = true
            }
            data = Data(getType(), phone, isTrust, this.data.boundingBox)
        }
        return data
    }

    private val PHONE =
        "(\\d{7,13}-\\d{2,6}|1\\*{6}\\d{4}|1\\d{2}\\*{4}\\d{4}|1[3-9]\\d{9})\$"
    private val nums = arrayListOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    private val STO =
        "^(401|770|220)[1-9]\\d{8}\$|^(221|888|588|229|336|968|688|402|335|868|227|118|403)\\d{9}\$|^400[0-8]\\d{8}\$|^7700[1-9]\\d{7}\$|^(11001|40000|22600)\\d{7}\$|^(4010714|4010713)\\d{5}\$|^(40107809|40107808)\\d{4}\$|^(334|335|336|221|588|583|553|888|401|402)\\d{10}\$|^770[1-9]\\d{9}\$|^7700[1-9]\\d{8}\$|^(11000|11001)\\d{8}\$|^77000[7-9]\\d{7}\$|^(33|37|66|77)\\d{11}\$|^\\d{12}[A-Z]\$|^(11|22|88)[0-9]{10}\$|^(55|66)[0-9]{11}\$|^(4)[0-9]{11}\$|^(77)[0-9]{13}\$"
    private val YTO =
        "^(80|81|82|[A-Za-z][0-9])\\d{10}\$|^(700|888|618|500|578|710|600|100|800)\\d{9}\$|^(200(?!0).|889(?!5).)\\d{8}\$|^(DD|dd|JY)\\d{9}\$|^(88|99|80|78)\\d{16}\$|^(600|800)\\d{15}\$|^7\\d{17}\$|^(DD|dd|JY)\\d{15}\$|^YT\\d{13}\$|^(R02TYT|R02ZYT)\\d{2,}\$|^[6-8][0-9]{17}\$|^[D^G][0-9]{17}\$|^[D^G][A-Z0-9]{11}\$"
    private val ZTO =
        "^(75|76|78)[0-9]{12}\$|^((768|765|778|518|528|688|010|880|660|805|718|728|761|762|763|701|757|719|751|100|118|128|689|738|779)[0-9]{9})\$|^((7380|1180|2013|2010|1000|1010)[0-9]{8})\$|^((8010|8021|8831|8013)[0-9]{6})\$|^((53|91|94|96)[0-9]{10})\$|^((a|b|h)[0-9]{13})\$|^((90|80|60)[0-9]{7})\$|^((80|81)[0-9]{6})\$|^120[0-9]{9}\$|^780[0-9]{9}\$|^881[0-9]{9}\$|^882[0-9]{9}\$|^91[0-9]{10}\$|^54[0-9]{10}\$|^63[0-9]{10}\$|^64[0-9]{10}\$|^72[0-9]{10}\$|^(220|221|223|224|225|226|227|228|229)[0-9]{7}\$|^731[0-9]{11}\$|^771[0-9]{11}\$|^733000[0-9]{8}\$|^781[0-9]{11}\$|^734[0-9]{11}\$|^75210[0-9]{9}\$|^758[0-9]{11}\$|^73223[0-9]{9}\$|^73222[0-9]{9}\$|^73210[0-9]{9}\$|^741[0-9]{11}\$|^73224[0-9]{9}\$|^73211[0-9]{9}\$|^7777[0-9]{10}\$|^777600[0-9]{8}\$|^77761[0-9]{9}\$|^73225[0-9]{9}\$|^754[0-9]{11}\$|^755[0-9]{11}\$|^73220[0-9]{9}\$|^73228[0-9]{9}\$|^782[0-9]{11}\$|^756[0-9]{11}\$|^759[0-9]{11}\$"
    private val YUNDA =
        "^((20|3[0-9]|44|45|47|48|50|51|53|57|58|60|66|68|75|76|78|80|85|88|90)(\\d{11})|(31|42|43|46)(\\d{11}|\\d{13})|(77000|77001)(\\d{8})|(779)(\\d{10})|(7746)(\\d{9})|(YD)(\\d{13}|\\d{15}))\$"
    private val EMS =
        "^(10|11|12)\\d{11,12}\$|^1000\\d{9,10}\$|^2000190\\d{5}\$|^(00|SA|pq|pa|ph|PH|PQ|sa|sb)\\d{11}\$|^A0000\\d{8}\$|^(20)[0-9]{11}\$|^[A-Z]{1}[0-9]{11}\$"
    private val TTKDEX =
        "^(668|669|886|995|383|116|998|776|561|023|580|550|024)\\d{9}\$|^560[7-9]\\d{8}\$|^TT\\d{13}\$"
    private val SFEXP =
        "^(03|07|13|44|16|95|24|25|43|17|93|97|04|92)\\d{10}\$|^(61(?!8).|66(?!8).|50(?!0).|058|266|278|283|284|420|456|438|457|586|609|620|789|221|153|930|964|687|869|870|367|261|820|767|824|804|074|072|155|631|086|054|784|264|358|181|821|90[1-9])\\d{9}\$|^(765(?!3).|880[0-15-9]|8895|7083|900[13-9])\\d{8}\$|^SF\\d{13}\$|^99(90[1-9]|9[1-9]0|[0-8]00|9[1-9][1-9]|[0-8]0[1-9]|[0-8][1-9]0|[0-8][1-9][1-9])\\d{7}\$"
    private val ZGYZ = "^(98|99|95|97|96|91|92|60)\\d{11,12}\$"
    private val JD = "^JD.*\$"
    private val DBKD = "^DPK\\d{0,}\$"
    private val FWEXP =
        "^09[1-7][0-9]{9}\$|^273[0-9]{9}\$|^18[2-7][0-9]{9}\$|^189[0-9]{9}\$|^190[0-9]{9}\$|^99[0-9]{13}\$"
    private val JTKD =
        "^JT[0-9]{13}\$|^6[0-9]{13}\$|^((55|56|58|59)[0-9]{14})\$|^((6|7)[0-9]{8,9})\$|^((55)[0-9]{13})\$"
    private val REGEX_ZH = "^[\u4E00-\u9FA5]$" //中文+字母+数字

    //是否快递公司
    private fun isExpress(num: String): Boolean {
        return Pattern.matches(STO, num) ||
                Pattern.matches(YTO, num) ||
                Pattern.matches(ZTO, num) ||
                Pattern.matches(YUNDA, num) ||
                Pattern.matches(EMS, num) ||
                Pattern.matches(TTKDEX, num) ||
                Pattern.matches(SFEXP, num) ||
                Pattern.matches(ZGYZ, num) ||
                Pattern.matches(JD, num) ||
                Pattern.matches(DBKD, num) ||
                Pattern.matches(FWEXP, num) ||
                Pattern.matches(JTKD, num)
    }

    //匹配手机号
    private fun getPhoneNumber(num: String?): String? {
        num?.run {
            if (isExpress(this)) return null
            val p =
                Pattern.compile(PHONE)
            val m = p.matcher(this)
            if (m.find()) {
                val str = m.group()
                val index = indexOf(str)
                if (index > 0 && nums.contains(this[index - 1].toString())) {
                    return null
                }

                if (index > 0 && index + str.length + 1 < length && nums.contains(this[index + str.length + 1].toString())) {
                    return null
                }

                return m.group()
            }
        }
        return null
    }
}